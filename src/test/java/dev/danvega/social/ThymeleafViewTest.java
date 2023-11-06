package dev.danvega.social;

import dev.danvega.social.controller.HomeController;
import dev.danvega.social.model.GitHubRepository;
import dev.danvega.social.service.GitHubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(SpringExtension.class)
@WebMvcTest(HomeController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable Spring Security filters
public class ThymeleafViewTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GitHubService gitHubService;

    @MockBean
    private OAuth2AuthorizedClientService authorizedClientService;

    @BeforeEach
    public void setUp() {
        // Assuming GitHubRepository is the correct type you expect to be returned
        List<GitHubRepository> repositoriesList = new ArrayList<>();

        // Mock the reactive call to return an empty list inside a Mono
        given(gitHubService.getRepositories(any(Authentication.class)))
                .willReturn(Mono.just(repositoriesList));
    }

    @Test
    @WithMockUser
    public void whenNoRepositories_thenDisplayNoRepositoriesFoundMessage() throws Exception {
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "mocked_token_value", Instant.now(), Instant.now().plus(Duration.ofDays(1)));

        String registrationId = "github";
        String principalName = "user";

        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(registrationId);
        when(authentication.getName()).thenReturn(principalName);
        when(authorizedClientService.loadAuthorizedClient(eq(registrationId), eq(principalName)))
                .thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);

        when(gitHubService.getRepositories(authentication)).thenReturn(Mono.just(Collections.emptyList())); // Simulate no repositories

        mockMvc.perform(get("/") // This should be the path defined in your @GetMapping
                        .principal(authentication)) // This sets the mock authentication as the current user
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No repositories found."))); // Check for the message
    }
}
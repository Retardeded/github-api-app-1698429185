package dev.danvega.social;

import dev.danvega.social.controller.HomeController;
import dev.danvega.social.model.GitHubRepository;
import dev.danvega.social.repository.BlocklistRepository;
import dev.danvega.social.service.GitHubService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@SpringBootTest
@AutoConfigureMockMvc
public class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GitHubService gitHubService;

    @Autowired
    private HomeController homeController;

    @MockBean
    private BlocklistRepository blocklistRepository;

    @Test
    public void testSecuredPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().
                        string(containsString("Login with OAuth 2.0")));
    }

    @Test
    @WithMockUser
    public void testSearch() throws Exception {
        List<GitHubRepository> mockRepositories = new ArrayList<>();
        mockRepositories.add(new GitHubRepository("Spring Boot", 100));
        mockRepositories.add(new GitHubRepository("Spring Cloud", 150));

        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getName()).thenReturn("user");

        Mockito.when(blocklistRepository.findByUserId(Mockito.anyInt())).thenReturn(List.of());

        // Use the mocked Authentication object in your service call
        Mockito.when(gitHubService.searchRepositories(Mockito.eq("Spring"), Mockito.any(Authentication.class)))
                .thenReturn(mockRepositories);

        mockMvc.perform(get("/search")
                        .param("queryText", "Spring")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name", is("Spring Boot")))
                .andExpect(jsonPath("$[0].stargazersCount", is(100)))
                .andExpect(jsonPath("$[1].name", is("Spring Cloud")))
                .andExpect(jsonPath("$[1].stargazersCount", is(150)));
    }
}

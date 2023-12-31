package dev.danvega.social;

import dev.danvega.social.model.BlockedGithubRepo;
import dev.danvega.social.model.GitHubRepository;
import dev.danvega.social.repository.BlocklistRepository;
import dev.danvega.social.service.GitHubService;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.reactive.function.client.WebClient;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class GitHubServiceTest {
    private GitHubService gitHubService;
    private BlocklistRepository blocklistRepository;
    private OAuth2AuthorizedClientService authorizedClientService;
    private WebClient webClient;
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        // Mock the dependencies
        authorizedClientService = mock(OAuth2AuthorizedClientService.class);
        webClient = mock(WebClient.class);
        blocklistRepository = mock(BlocklistRepository.class);
        requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);

        gitHubService = new GitHubService(authorizedClientService, blocklistRepository) {
            @Override
            public WebClient buildWebClient(OAuth2AccessToken accessToken) {
                return webClient; // Return the mock webClient instead of a real instance
            }
        };

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void getRepositories_ValidAuthentication_ReturnsRepositories() {
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);
        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);

        String registrationId = "github";
        String principalName = "user";

        String jsonResponse = "[{\"name\":\"mock-repo-1\",\"stargazers_count\":10},{\"name\":\"mock-repo-2\",\"stargazers_count\":5}]";

        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(registrationId);
        when(authentication.getName()).thenReturn(principalName);
        when(authorizedClientService.loadAuthorizedClient(eq(registrationId), eq(principalName)))
                .thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn("mocked_token_value");

        List<GitHubRepository> mockRepositoriesList = Arrays.asList(
                new GitHubRepository("mock-repo-1", 10),
                new GitHubRepository("mock-repo-2", 5)
        );

        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(jsonResponse));

        Mono<List<GitHubRepository>> repositoriesMono = gitHubService.getRepositories(authentication);

        StepVerifier.create(repositoriesMono)
                .assertNext(repositories -> {
                    // Verify the size of the list
                    Assertions.assertEquals(mockRepositoriesList.size(), repositories.size());

                    // Verify each RepositoryInfo in the list (you could do more complex assertions here)
                    for(int i = 0; i < mockRepositoriesList.size(); i++) {
                        GitHubRepository expected = mockRepositoriesList.get(i);
                        GitHubRepository actual = repositories.get(i);
                        Assertions.assertEquals(expected.getName(), actual.getName());
                        Assertions.assertEquals(expected.getStargazersCount(), actual.getStargazersCount());
                    }
                })
                .verifyComplete();
    }

    @Test
    void searchRepositories_WithBlocklist_ExcludesBlockedRepos() {
        OAuth2User principal = mock(OAuth2User.class);
        Authentication authentication = mock(OAuth2AuthenticationToken.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(principal.getAttribute("id")).thenReturn(1);

        List<String> blockedRepoNames = List.of("mock-repo-1");
        List<BlockedGithubRepo> blockedRepos = blockedRepoNames.stream()
                .map(name -> new BlockedGithubRepo(name, 1))
                .collect(Collectors.toList());

        when(blocklistRepository.findByUserId(1)).thenReturn(blockedRepos);

        List<GitHubRepository> userRepositoriesCache = Arrays.asList(
                new GitHubRepository("mock-repo-1", 10),
                new GitHubRepository("mock-repo-2", 5)
        );

        gitHubService.setUserRepositoriesCache(userRepositoriesCache); // Assuming there's a setter

        List<GitHubRepository> searchResults = gitHubService.searchRepositories("mock", authentication);

        Assertions.assertEquals(1, searchResults.size(), "Expected to find only non-blocked repositories");
        Assertions.assertEquals("mock-repo-2", searchResults.get(0).getName(), "Expected to find only non-blocked repository names");
    }
}




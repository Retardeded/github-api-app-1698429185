package dev.danvega.social.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.danvega.social.model.GitHubRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GitHubService {

    private static final Logger logger = LoggerFactory.getLogger(GitHubService.class);

    private final OAuth2AuthorizedClientService authorizedClientService;

    private List<GitHubRepository> userRepositoriesCache = new ArrayList<>();

    public GitHubService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    public Mono<List<GitHubRepository>> getRepositories(Authentication authentication) {
        OAuth2AccessToken accessToken = extractAccessToken(authentication);
        WebClient webClient = buildWebClient(accessToken);

        return webClient
                .get()
                .uri("/user/repos?sort=updated&direction=desc")
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractRepositoryInfo)
                .doOnNext(repos -> this.userRepositoriesCache = repos) // Correctly update the cache
                .doOnError(error -> logger.error("GitHub API Request Failed: {}", error.getMessage()));
    }

    private List<GitHubRepository> extractRepositoryInfo(String jsonResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<Map<String, Object>> repos = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<>() {
                    }
            );
            return repos.stream().map(repo -> {
                String name = (String) repo.get("name");
                Integer stargazersCount = (Integer) repo.get("stargazers_count");
                return new GitHubRepository(name, stargazersCount);
            }).collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            logger.error("Failed to process JSON", e);
            return Collections.emptyList();
        }
    }

    public WebClient buildWebClient(OAuth2AccessToken accessToken) {
        return WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Authorization", "Bearer " + accessToken.getTokenValue())
                .build();
    }

    private OAuth2AccessToken extractAccessToken(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                    ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId(),
                    authentication.getName());
            return authorizedClient.getAccessToken();
        }
        logger.warn("Access Token not found or invalid.");
        return null; // Handle this according to your application's requirements
    }

    public List<GitHubRepository> searchRepositories(String query) {
        return userRepositoriesCache.stream()
                .filter(repo -> repo.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }
}
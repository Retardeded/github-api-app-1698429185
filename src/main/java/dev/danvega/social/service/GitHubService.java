package dev.danvega.social.service;

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
import reactor.core.publisher.Flux;

@Service
public class GitHubService {

    private static final Logger logger = LoggerFactory.getLogger(GitHubService.class);

    private final OAuth2AuthorizedClientService authorizedClientService;

    public GitHubService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    public Flux<GitHubRepository> getRepositories(Authentication authentication) {
        OAuth2AccessToken accessToken = extractAccessToken(authentication);
        WebClient webClient = WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Authorization", "Bearer " + accessToken.getTokenValue())
                .build();

        return webClient
                .get()
                .uri("/user/repos?sort=updated&direction=desc")
                .retrieve()
                .bodyToFlux(GitHubRepository.class)
                .doOnError(error -> {
                    logger.error("GitHub API Request Failed: {}", error.getMessage());
                });
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
}
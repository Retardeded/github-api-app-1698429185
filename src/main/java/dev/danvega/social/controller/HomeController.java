package dev.danvega.social.controller;

import dev.danvega.social.model.BlockedGithubRepo;
import dev.danvega.social.model.GitHubRepository;
import dev.danvega.social.service.GitHubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final GitHubService gitHubService;
    private  final BlocklistController blocklistController;
    private List<GitHubRepository> userRepositories = new ArrayList<>();


    @Autowired
    public HomeController(GitHubService gitHubService, BlocklistController blocklistController) {
        this.gitHubService = gitHubService;
        this.blocklistController = blocklistController;
    }

    @GetMapping("/")
    public String page(Model model, Authentication authentication) {
        Flux<GitHubRepository> repositories = gitHubService.getRepositories(authentication);
        List<GitHubRepository> repositoriesList = repositories.collectList().block();
        userRepositories = repositoriesList;
        assert repositoriesList != null;
        GitHubRepository mostRecentRepository = repositoriesList.isEmpty() ? null : repositoriesList.get(0);
        model.addAttribute("mostRecentRepository", mostRecentRepository);

        return "page";
    }

    @GetMapping("/search")
    @ResponseBody
    public List<GitHubRepository> search(@RequestParam String queryText, Authentication authentication) {
        List<GitHubRepository> matchingRepositories = new ArrayList<>();

        if (queryText.length() >= 1) {
            ResponseEntity<List<BlockedGithubRepo>> responseEntity = blocklistController.getBlocklist(authentication);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                List<BlockedGithubRepo> userBlockedRepositories = responseEntity.getBody();
                Set<String> blockedRepoNames = userBlockedRepositories.stream()
                        .map(BlockedGithubRepo::getRepositoryName)
                        .collect(Collectors.toSet());

                for (GitHubRepository repository : userRepositories) {
                    if (!blockedRepoNames.contains(repository.getName()) &&
                            repository.getName().toLowerCase().contains(queryText.toLowerCase())) {
                        matchingRepositories.add(repository);
                    }
                }
            }
        }
        return matchingRepositories;
    }

    @GetMapping("/login")
    public String secured() {
        return "Hello, Secured!";
    }

}

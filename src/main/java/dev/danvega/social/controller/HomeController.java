package dev.danvega.social.controller;

import dev.danvega.social.model.GitHubRepository;
import dev.danvega.social.service.GitHubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    private final GitHubService gitHubService;
    private List<GitHubRepository> userRepositories = new ArrayList<>();

    @Autowired
    public HomeController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @GetMapping("/")
    public String page(Model model, Authentication authentication) {
        List<GitHubRepository> repositoriesList = gitHubService.getRepositories(authentication).block();
        userRepositories = repositoriesList;
        GitHubRepository mostRecentRepository = repositoriesList.isEmpty() ? null : repositoriesList.get(0);
        model.addAttribute("mostRecentRepository", mostRecentRepository);

        return "page";
    }
    @RequestMapping("/login")
    public String customLogin() {
        return "login"; // Name of the Thymeleaf template for the login page
    }
    @GetMapping("/search")
    @ResponseBody
    public List<GitHubRepository> search(@RequestParam(name = "q") String query) {
        List<GitHubRepository> matchingRepositories = new ArrayList<>();

        if (query.length() >= 1) {
            for (GitHubRepository repository : userRepositories) {
                if (repository.getName().toLowerCase().contains(query.toLowerCase())) {
                    matchingRepositories.add(repository);
                }
            }
        }

        return matchingRepositories;
    }
}

package dev.danvega.social.controller;

import dev.danvega.social.model.BlockedGithubRepo;
import dev.danvega.social.repository.BlocklistRepository;
import dev.danvega.social.service.GitHubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/blocklist")
public class BlocklistController {

    private final BlocklistRepository blocklistRepository;
    private final GitHubService gitHubService;

    @Autowired
    public BlocklistController(BlocklistRepository blocklistRepository, GitHubService gitHubService) {
        this.blocklistRepository = blocklistRepository;
        this.gitHubService = gitHubService;
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<?> blocklist(@RequestParam String repositoryName, Authentication authentication) {
        Integer userId = gitHubService.getUserIdFromAuthentication(authentication);
        if (blocklistRepository.findByRepositoryNameAndUserId(repositoryName, userId) != null) {
            return ResponseEntity.badRequest().body("Repository is already blocked.");
        }
        BlockedGithubRepo blockedGithubRepo = new BlockedGithubRepo(repositoryName, userId);
        blocklistRepository.save(blockedGithubRepo);
        return ResponseEntity.ok(blockedGithubRepo);
    }

    @DeleteMapping("/{repositoryName}")
    @ResponseBody
    public ResponseEntity<String> removeFromBlocklist(@PathVariable String repositoryName, Authentication authentication) {
        Integer userId = gitHubService.getUserIdFromAuthentication(authentication);

        BlockedGithubRepo blockedGithubRepo = blocklistRepository.findByRepositoryNameAndUserId(repositoryName, userId);
        if (blockedGithubRepo == null) {
            return ResponseEntity.notFound().build();
        }

        blocklistRepository.delete(blockedGithubRepo);

        return ResponseEntity.ok("Repository has been removed from the blocklist.");
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<List<BlockedGithubRepo>> getBlocklist(Authentication authentication) {
        Integer userId = gitHubService.getUserIdFromAuthentication(authentication);
        List<BlockedGithubRepo> blockedRepos = blocklistRepository.findByUserId(userId);

        if (blockedRepos.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(blockedRepos);
    }
}
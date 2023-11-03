package dev.danvega.social.controller;

import dev.danvega.social.model.BlockedGithubRepo;
import dev.danvega.social.repository.BlocklistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/blocklist")
public class BlocklistController {

    private final BlocklistRepository blocklistRepository;

    @Autowired
    public BlocklistController(BlocklistRepository blocklistRepository) {
        this.blocklistRepository = blocklistRepository;
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<?> blocklist(@RequestParam String repositoryName, Authentication authentication) {
        Integer userId = getUserIdFromAuthentication(authentication);
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
        Integer userId = getUserIdFromAuthentication(authentication);

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
        Integer userId = getUserIdFromAuthentication(authentication);
        List<BlockedGithubRepo> blockedRepos = blocklistRepository.findByUserId(userId);

        if (blockedRepos.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(blockedRepos);
    }

    public Integer getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oAuth2User) {
            return oAuth2User.getAttribute("id");
        }
        return null;
    }
}
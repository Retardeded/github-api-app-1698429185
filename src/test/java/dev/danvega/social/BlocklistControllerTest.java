package dev.danvega.social;

import dev.danvega.social.model.BlockedGithubRepo;
import dev.danvega.social.repository.BlocklistRepository;
import dev.danvega.social.service.GitHubService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class BlocklistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GitHubService gitHubService;

    @MockBean
    private BlocklistRepository blocklistRepository;

    @Test
    @WithMockUser
    public void testBlocklistRepository() throws Exception {
        String repoName = "Spring Boot";
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getName()).thenReturn("user");
        Mockito.when(gitHubService.getUserIdFromAuthentication(authentication)).thenReturn(1);

        Mockito.when(blocklistRepository.findByRepositoryNameAndUserId(repoName, 1)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/blocklist")
                        .param("repositoryName", repoName))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user")
    public void testRemoveFromBlocklist() throws Exception {
        String repoName = "SpringBoot";
        Integer userId = 1;
        BlockedGithubRepo blockedRepo = new BlockedGithubRepo(repoName, userId);

        Mockito.when(gitHubService.getUserIdFromAuthentication(Mockito.any(Authentication.class))).thenReturn(userId);

        Mockito.when(blocklistRepository.findByRepositoryNameAndUserId(Mockito.eq(repoName), Mockito.eq(userId))).thenReturn(blockedRepo);

        mockMvc.perform(MockMvcRequestBuilders.delete("/blocklist/{repositoryName}", URLEncoder.encode(repoName, StandardCharsets.UTF_8.toString())))
                .andExpect(status().isOk());

        Mockito.verify(blocklistRepository).delete(Mockito.any(BlockedGithubRepo.class));
    }

    @Test
    @WithMockUser(username = "user")
    public void testGetBlocklist() throws Exception {
        Integer userId = 1;
        List<BlockedGithubRepo> blockedRepos = List.of(
                new BlockedGithubRepo("Spring Boot", userId),
                new BlockedGithubRepo("Spring Cloud", userId)
        );

        Mockito.when(gitHubService.getUserIdFromAuthentication(Mockito.any(Authentication.class))).thenReturn(userId);

        Mockito.when(blocklistRepository.findByUserId(userId)).thenReturn(blockedRepos);

        mockMvc.perform(MockMvcRequestBuilders.get("/blocklist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].repositoryName", is("Spring Boot")))
                .andExpect(jsonPath("$[1].repositoryName", is("Spring Cloud")));
    }
}
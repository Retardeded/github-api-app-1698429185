package dev.danvega.social;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testUnauthenticatedAccessToLoginPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
    @Test
    public void testUnauthenticatedAccessToProtectedPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/any-protected-url"))
                .andExpect(MockMvcResultMatchers.status().isFound()) // Expect a 302 status
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("**/login")); // Using redirectedUrlPattern for wildcard matching
    }
    @Test
    public void testOAuth2AuthorizationEndpointAccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/oauth2/authorization/github"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection()); // Expect a redirect status (typically 302)
    }
}
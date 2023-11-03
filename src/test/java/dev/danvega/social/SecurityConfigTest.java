package dev.danvega.social;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;

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
    public void testAuthenticatedAccessToProtectedPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/any-protected-url"))
                .andExpect(MockMvcResultMatchers.status().isFound()) // Expect a 302 status
                .andExpect(MockMvcResultMatchers.redirectedUrl("http://localhost/oauth2/authorization/github")); // Adjust the URL as needed
    }

    @Test
    public void testAuthenticationRedirect() throws Exception {
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken("user", "password", "ROLE_USER");
        authenticationToken.setAuthenticated(true); // Set as authenticated

        mockMvc.perform(MockMvcRequestBuilders.get("/oauth2/authorization/github")
                        .with(request -> {
                            request.setRemoteUser(authenticationToken.getName());
                            request.setUserPrincipal(authenticationToken);
                            return request;
                        }))
                .andExpect(MockMvcResultMatchers.status().isFound()) // Expect a redirection status (302)
                .andExpect(result -> {
                    String redirectedUrl = result.getResponse().getRedirectedUrl();
                    if (redirectedUrl == null || !redirectedUrl.contains("https://github.com/login/oauth/authorize?response_type=code&client_id=")) {
                        throw new AssertionError("Redirected URL does not match the expected pattern");
                    }
                });
    }
}
package br.com.dkzit.project.post;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc
public class PostControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockBean
    PostRepository postRepository;

    List<Post> posts = new ArrayList<>();

    @BeforeEach
    void setUp() {
        posts = List.of(
                new Post(1, 1, "Hello, World!", "This is my first post.", null),
                new Post(2, 1, "Second Post", "This is my second post.", null)
        );
    }

    @Test
    void shouldFindAllPosts() throws Exception {

        String jsonResponse = """
                
                [
                    {
                        "id" : 1,
                        "userId": 1,
                        "title": "Hello, World!",
                        "body":"This is my first post.",
                        "version": null
                    },
                    {
                        "id" : 2,
                        "userId": 1,
                        "title": "Second Post",
                        "body":"This is my second post.",
                        "version": null
                    }
                
                ]
                
                """;

        when(postRepository.findAll()).thenReturn(posts);

        mockMvc.perform(get("/api/posts"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(jsonResponse));
    }


    @Test
    void shouldFindPostWhenGivenValidId() throws Exception {
        when(postRepository.findById(1)).thenReturn(Optional.of(posts.get(0)));

        var post = posts.get(0);

        var json = STR."""
                    {
                        "id": \{post.id()},
                        "userId": \{post.userId()},
                        "title":"\{post.title()}",
                        "body":"\{post.body()}",
                        "version":null
                    }
                """;

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(json));
    }


    @Test
    void shouldNotFindPostWhenGivenInvalidId() throws Exception {
        when(postRepository.findById(999)).thenThrow(PostNotFoundException.class);

        mockMvc.perform(get("/api/posts/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreatedNewPostWhenPostIsValid() throws Exception {
        var post = new Post(3, 1, "NEW TITLE", "NEW BODY", null);

        var json = STR."""
                    {
                        "id": \{post.id()},
                        "userId": \{post.userId()},
                        "title":"\{post.title()}",
                        "body":"\{post.body()}",
                        "version":null
                    }
                """;

        when(postRepository.save(post)).thenReturn(post);

        mockMvc.perform(post("/api/posts")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldNotCreatedPostWhenPostIsInvalid() throws Exception {
        var post = new Post(3, 1, "", "", null);

        var json = STR."""
                    {
                        "id": \{post.id()},
                        "userId": \{post.userId()},
                        "title":"\{post.title()}",
                        "body":"\{post.body()}",
                        "version":null
                    }
                """;

        when(postRepository.save(post)).thenReturn(post);

        mockMvc.perform(post("/api/posts")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdatePostWhenGivenValidPost() throws Exception {
        var postUpdated = new Post(1, 1, "This is a new title", "This is a new body", 1);

        when(postRepository.save(postUpdated)).thenReturn(postUpdated);
        when(postRepository.findById(1)).thenReturn(Optional.of(postUpdated));

        var requestBody = STR."""
                    {
                        "id": \{postUpdated.id()},
                        "userId": \{postUpdated.userId()},
                        "title":"\{postUpdated.title()}",
                        "body":"\{postUpdated.body()}",
                        "version":\{postUpdated.version()}
                    }
                """;

        mockMvc.perform(put("/api/posts/1")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk());

    }

    @Test
    void shouldDeletePostWhenGivenValidId() throws Exception {
        doNothing().when(postRepository).deleteById(1);

        mockMvc.perform(delete("/api/posts/1"))
                .andExpect(status().isNoContent());

        verify(postRepository, times(1)).deleteById(1);
    }


}

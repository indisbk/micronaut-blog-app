package my.blog.controllers;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.RxStreamingHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import my.blog.errors.CustomHttpResponseError;
import my.blog.models.Post;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class PostControllerTest {

    private static Map<String, Post> testData;

    @Inject
    @Client("/")
    RxStreamingHttpClient client;

    @BeforeAll
    static void prepareTestData() {
        testData = Map.of(
                "one",  Post.builder()
                        .id(1L)
                        .title("First post")
                        .text("Text of first post")
                        .author("Victor")
                        .dateTime(LocalDateTime.of(2020, 10, 10, 10, 10))
                        .build(),
                "two", Post.builder()
                        .id(2L)
                        .title("Second post")
                        .text("Text of second post")
                        .author("Gregory")
                        .dateTime(LocalDateTime.of(2020, 11, 11, 11, 11))
                        .build(),
                "three", Post.builder()
                        .id(3L)
                        .title("Third post")
                        .text("Text of third post")
                        .author("Kobayashi")
                        .dateTime(LocalDateTime.of(2020, 12, 12, 12, 12))
                        .build()
        );
    }

    @Test
    void returnsListOfPosts() {
        var request = HttpRequest.GET("/posts");
        HttpResponse<List<Post>> response = client.toBlocking().exchange(request, Argument.listOf(Post.class));

        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());

        List<Post> posts = response.body();
        assertThat(posts).containsExactly(testData.get("one"), testData.get("two"), testData.get("three"));
    }

    @Test
    void findPostById() {
        HttpResponse<Post> response = client.toBlocking()
                .exchange(HttpRequest.GET("/posts/1"), Post.class);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());

        assertEquals(testData.get("one"), response.body());
    }

    @Test
    void notFoundPostById() {
        var id = "4";
        try {
            client.toBlocking().exchange(HttpRequest.GET("/posts/" + id),
                    Argument.of(Post.class),
                    Argument.of(CustomHttpResponseError.class));
        } catch (HttpClientResponseException ex) {
            Optional<CustomHttpResponseError> error = ex.getResponse().getBody(CustomHttpResponseError.class);
            assertTrue(error.isPresent());
            assertEquals(HttpStatus.NOT_FOUND.getCode(), error.get().getStatus());
            assertEquals(HttpStatus.NOT_FOUND.name(), error.get().getError());
            assertEquals("Not found post with id: " + id, error.get().getMessage());
        }
    }

}

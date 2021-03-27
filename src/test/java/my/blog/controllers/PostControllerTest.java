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
import org.junit.jupiter.api.*;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostControllerTest {

    private static Map<String, Post> testData;

    private final long UNSUPPORTED_ID = 4L;

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
    @Order(1)
    void returnsListOfPosts() {
        HttpResponse<List<Post>> response = client.toBlocking().exchange(HttpRequest.GET("/posts"), Argument.listOf(Post.class));

        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());

        List<Post> posts = response.body();
        assertThat(posts).containsExactly(testData.get("one"), testData.get("two"), testData.get("three"));
    }

    @Test
    @Order(2)
    void findPostById() {
        HttpResponse<Post> response = client.toBlocking()
                .exchange(HttpRequest.GET("/posts/1"), Post.class);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());

        assertEquals(testData.get("one"), response.body());
    }

    @Test
    @Order(3)
    void notFoundPostById() {
        try {
            client.toBlocking().exchange(HttpRequest.GET("/posts/" + UNSUPPORTED_ID),
                    Argument.of(Post.class),
                    Argument.of(CustomHttpResponseError.class));
        } catch (HttpClientResponseException ex) {
            Optional<CustomHttpResponseError> error = ex.getResponse().getBody(CustomHttpResponseError.class);
            assertTrue(error.isPresent());
            assertEquals(HttpStatus.NOT_FOUND.getCode(), error.get().getStatus());
            assertEquals(HttpStatus.NOT_FOUND.name(), error.get().getError());
            assertEquals("Not found post with id: " + UNSUPPORTED_ID, error.get().getMessage());
        }
    }

    @Test
    @Order(4)
    void canUpdatePost() {
        var changedTitle = "New title";
        var changedText = "New text";
        HttpResponse<Post> response = client.toBlocking().exchange(HttpRequest.PUT("/posts",
                Post.builder()
                .id(1L)
                .title(changedTitle)
                .text(changedText)
                .build()),
                Post.class
        );

        assertEquals(HttpStatus.OK, response.getStatus());

        var updatedPost = response.body();
        assertNotNull(updatedPost);
        assertEquals(changedTitle, updatedPost.getTitle());
        assertEquals(changedText, updatedPost.getText());
    }

    @Test
    @Order(5)
    void failureUpdatePost() {
        try {
            client.toBlocking().exchange(HttpRequest.PUT("/posts", Post.builder().id(UNSUPPORTED_ID).build()),
                    Argument.of(Post.class),
                    Argument.of(CustomHttpResponseError.class));
        } catch (HttpClientResponseException ex) {
            Optional<CustomHttpResponseError> error = ex.getResponse().getBody(CustomHttpResponseError.class);
            assertTrue(error.isPresent());
            assertEquals(HttpStatus.NOT_ACCEPTABLE.getCode(), error.get().getStatus());
            assertEquals(HttpStatus.NOT_ACCEPTABLE.name(), error.get().getError());
            assertEquals("Failure update post with id: " + UNSUPPORTED_ID, error.get().getMessage());
        }
    }

    @Test
    @Order(6)
    void canRemovePostById() {
        HttpResponse<Object> responseOfDel = client.toBlocking().exchange(HttpRequest.DELETE("/posts/1"));
        assertEquals(HttpStatus.OK, responseOfDel.getStatus());

        HttpResponse<List<Post>> responseOfGetAll = client.toBlocking().exchange(HttpRequest.GET("/posts"), Argument.listOf(Post.class));
        List<Post> posts = responseOfGetAll.body();
        assertNotNull(posts);
        assertEquals(2, posts.size());
    }

    @Test
    @Order(7)
    void failureRemovePost() {
        try {
            client.toBlocking().exchange(HttpRequest.DELETE("/posts/" + UNSUPPORTED_ID), String.class);
        } catch (HttpClientResponseException ex) {
            HttpResponse<?> response = ex.getResponse();
            assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatus());
            Optional<String> message = response.getBody(String.class);
            assertTrue(message.isPresent());
            assertEquals("Failure update post with id: " + UNSUPPORTED_ID, message.get());
        }
    }

}

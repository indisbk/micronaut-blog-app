package my.blog.controllers;

import io.micronaut.core.type.Argument;
import io.micronaut.http.*;
import io.micronaut.http.client.RxStreamingHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken;
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
        var response = client.toBlocking().exchange(HttpRequest.GET("/posts"), Argument.listOf(Post.class));

        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());

        List<Post> posts = response.body();
        assertThat(posts).containsExactly(testData.get("one"), testData.get("two"), testData.get("three"));
    }

    @Test
    @Order(2)
    void findPostById() {
        var response = client.toBlocking().exchange(HttpRequest.GET("/posts/1"), Post.class);

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
        var token = givenTestUserIsLoggedIn();

        var changedTitle = "New title";
        var changedText = "New text";

        var updateRqst = HttpRequest.PUT("/posts/update", Post.builder().id(1L).title(changedTitle).text(changedText).build())
                .accept(MediaType.APPLICATION_JSON)
                .bearerAuth(token.getAccessToken());
        HttpResponse<Post> response = client.toBlocking().exchange(updateRqst, Post.class);

        assertEquals(HttpStatus.OK, response.getStatus());

        var updatedPost = response.body();
        assertNotNull(updatedPost);
        assertEquals(changedTitle, updatedPost.getTitle());
        assertEquals(changedText, updatedPost.getText());
    }

    @Test
    @Order(5)
    void failureUpdatePost() {
        var token = givenTestUserIsLoggedIn();
        try {
            var rqst = HttpRequest.PUT("/posts/update", Post.builder().id(UNSUPPORTED_ID).build())
                    .accept(MediaType.APPLICATION_JSON)
                    .bearerAuth(token.getAccessToken());
            client.toBlocking().exchange(rqst, Argument.of(Post.class), Argument.of(CustomHttpResponseError.class));
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
        var token = givenTestUserIsLoggedIn();
        var rqst = HttpRequest.DELETE("/posts/1")
                .accept(MediaType.APPLICATION_JSON)
                .bearerAuth(token.getAccessToken());
        var responseOfDel = client.toBlocking().exchange(rqst);
        assertEquals(HttpStatus.OK, responseOfDel.getStatus());

        var responseOfGetAll = client.toBlocking().exchange(HttpRequest.GET("/posts"), Argument.listOf(Post.class));
        List<Post> posts = responseOfGetAll.body();
        assertNotNull(posts);
        assertEquals(2, posts.size());
    }

    @Test
    @Order(7)
    void failureRemovePost() {
        var token = givenTestUserIsLoggedIn();
        try {
            var rqst = HttpRequest.DELETE("/posts/" + UNSUPPORTED_ID)
                    .accept(MediaType.APPLICATION_JSON)
                    .bearerAuth(token.getAccessToken());
            client.toBlocking().exchange(rqst, String.class);
        } catch (HttpClientResponseException ex) {
            HttpResponse<?> response = ex.getResponse();
            assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatus());
            Optional<String> message = response.getBody(String.class);
            assertTrue(message.isPresent());
            assertEquals("Failure update post with id: " + UNSUPPORTED_ID, message.get());
        }
    }

    @Test
    @Order(8)
    void canCreatePost() {
        var token = givenTestUserIsLoggedIn();
        String createdTitle = "New title";
        String createdText = "New text";
        String postAuthor = "Brandon";
        var newPost = Post.builder().title(createdTitle).text(createdText).author(postAuthor).build();
        var rqst = HttpRequest.PUT("/posts/create", newPost)
                .accept(MediaType.APPLICATION_JSON)
                .bearerAuth(token.getAccessToken());
        var response = client.toBlocking().exchange(rqst, Post.class);

        assertEquals(HttpStatus.OK, response.getStatus());

        var createdPost = response.body();
        assertNotNull(createdPost);
        assertEquals(createdTitle, createdPost.getTitle());
        assertEquals(createdText, createdPost.getText());
        assertEquals(postAuthor, createdPost.getAuthor());
    }

    @Test
    @Order(9)
    void failCreateIfPostHasId() {
        var token = givenTestUserIsLoggedIn();
        try {
            var rqst = HttpRequest.PUT("/posts/create", Post.builder().id(UNSUPPORTED_ID).build())
                    .accept(MediaType.APPLICATION_JSON)
                    .bearerAuth(token.getAccessToken());
            client.toBlocking().exchange(rqst, Argument.of(Post.class), Argument.of(CustomHttpResponseError.class));
        } catch (HttpClientResponseException ex) {
            Optional<CustomHttpResponseError> error = ex.getResponse().getBody(CustomHttpResponseError.class);
            assertTrue(error.isPresent());
            assertEquals(HttpStatus.NOT_ACCEPTABLE.getCode(), error.get().getStatus());
            assertEquals(HttpStatus.NOT_ACCEPTABLE.name(), error.get().getError());
            assertEquals("Identifier of new post must be 0 or null!", error.get().getMessage());
        }
    }

    private BearerAccessRefreshToken givenTestUserIsLoggedIn() {
        var username = "blog@gmail.net";
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, "123456");
        var request = HttpRequest.POST("/login", credentials);
        var loginRsp = client.toBlocking().exchange(request, BearerAccessRefreshToken.class);
        assertEquals(HttpStatus.OK, loginRsp.getStatus());
        BearerAccessRefreshToken token = loginRsp.body();
        assertNotNull(token);
        assertEquals(username, token.getUsername());
        return token;
    }
}

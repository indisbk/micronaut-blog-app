package my.blog.controllers;

import io.micronaut.context.annotation.Property;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.RxStreamingHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import my.blog.errors.CustomHttpResponseError;
import my.blog.models.Post;
import my.blog.repositories.MemoryStorage;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostControllerMemoryTest {

    private final long UNSUPPORTED_ID = 4L;

    @Inject
    @Client("/")
    RxStreamingHttpClient client;

    @Inject
    MemoryStorage memoryStorage;

    @Test
    @Order(1)
    void returnsListOfPosts() {
        var response = client.toBlocking().exchange(HttpRequest.GET("/posts"), Argument.listOf(Post.class));

        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());

        List<Post> posts = response.body();
        assertThat(posts).containsExactlyInAnyOrderElementsOf(memoryStorage.getPosts());
    }

    @Test
    @Order(2)
    void findPostById() {
        var response = client.toBlocking().exchange(HttpRequest.GET("/posts/1"), Post.class);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());

        assertEquals(memoryStorage.getPosts().get(0), response.body());
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

        Post post1 = Post.builder().title(changedTitle).text(changedText).build();
        post1.setId(1L);
        var updateRqst = HttpRequest.PUT("/posts/update", post1)
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
            Post unknownPost = Post.builder().build();
            unknownPost.setId(UNSUPPORTED_ID);
            var rqst = HttpRequest.PUT("/posts/update", unknownPost)
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
            Post newFailedPost = Post.builder().build();
            newFailedPost.setId(UNSUPPORTED_ID);
            var rqst = HttpRequest.PUT("/posts/create", newFailedPost)
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

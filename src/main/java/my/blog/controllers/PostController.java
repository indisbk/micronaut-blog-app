package my.blog.controllers;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import my.blog.errors.CustomHttpResponseError;
import my.blog.models.Post;
import my.blog.services.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@Controller("/posts")
public class PostController {

    Logger logger = LoggerFactory.getLogger(PostController.class);

    @Inject
    private PostService service;

    @Get
    public List<Post> getAllPosts() {
        return service.getAllPosts();
    }

    @Get("/{id}")
    public HttpResponse getById(@PathVariable long id) {
        Optional<Post> foundPost = service.getById(id);
        if (foundPost.isEmpty()) {
            var errorMsg = "Not found post with id: " + id;
            logger.error(errorMsg);
            return HttpResponse.notFound(CustomHttpResponseError.builder()
                    .status(HttpStatus.NOT_FOUND.getCode())
                    .error(HttpStatus.NOT_FOUND.name())
                    .message(errorMsg)
                    .build());
        }
        return HttpResponse.ok(foundPost.get());
    }

    @Put(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public HttpResponse updatePost(@Body Post post) {
        var updatedPost = service.updatePost(post);
        if (updatedPost.isEmpty()) {
            var errorMsg = "Failure update post with id: " + post.getId();
            logger.error(errorMsg);
            return HttpResponse.status(HttpStatus.NOT_ACCEPTABLE).body(CustomHttpResponseError.builder()
                    .status(HttpStatus.NOT_ACCEPTABLE.getCode())
                    .error(HttpStatus.NOT_ACCEPTABLE.name())
                    .message(errorMsg)
                    .build());
        }
        return HttpResponse.ok(updatedPost.get());
    }

    @Delete(value = "/{id}", consumes = MediaType.APPLICATION_JSON)
    public HttpResponse<String> removePostById(@PathVariable long id) {
        return service.deletePostById(id) ? HttpResponse.ok() : HttpResponse.status(HttpStatus.NOT_ACCEPTABLE).body("Failure update post with id: " + id);
    }
}

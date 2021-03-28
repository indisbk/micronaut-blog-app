package my.blog.controllers;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @Operation(summary = "Returns all public posts")
    @ApiResponse(
            content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    @Tag(name = "posts")
    @Get
    public List<Post> getAllPosts() {
        return service.getAllPosts();
    }

    @Operation(summary = "Return post by given identifier")
    @ApiResponse(
            content = @Content(mediaType = MediaType.APPLICATION_JSON)

    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid given identifier"
    )
    @Tag(name = "post_by_id")
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

    @Operation(summary = "Update post")
    @ApiResponse(
            content = @Content(mediaType = MediaType.APPLICATION_JSON)

    )
    @ApiResponse(
            responseCode = "406",
            description = "Failure update post with given id"
    )
    @Tag(name = "update_post")
    @Put(value = "/update", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
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

    @Operation(summary = "Delete post by id")
    @ApiResponse(
            content = @Content(mediaType = MediaType.APPLICATION_JSON)

    )
    @ApiResponse(
            responseCode = "406",
            description = "Failure update post by id"
    )
    @Tag(name = "delete_post_by_id")
    @Delete(value = "/{id}", consumes = MediaType.APPLICATION_JSON)
    public HttpResponse<String> removePostById(@PathVariable long id) {
        return service.deletePostById(id) ? HttpResponse.ok() : HttpResponse.status(HttpStatus.NOT_ACCEPTABLE).body("Failure update post with id: " + id);
    }

    @Operation(summary = "Creating a new post")
    @ApiResponse(
            content = @Content(mediaType = MediaType.APPLICATION_JSON)

    )
    @ApiResponse(
            responseCode = "406",
            description = "Identifier of new post must be 0 or null!"
    )
    @Tag(name = "create_post")
    @Put(value = "create", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public HttpResponse createNewPost(@Body Post post) {
        if (post.getId() > 0) {
            var errorMsg = "Identifier of new post must be 0 or null!";
            logger.error(errorMsg);
            return HttpResponse.status(HttpStatus.NOT_ACCEPTABLE).body(CustomHttpResponseError.builder()
                    .status(HttpStatus.NOT_ACCEPTABLE.getCode())
                    .error(HttpStatus.NOT_ACCEPTABLE.name())
                    .message(errorMsg)
                    .build());
        }
        return HttpResponse.ok(service.createPost(post));
    }
}

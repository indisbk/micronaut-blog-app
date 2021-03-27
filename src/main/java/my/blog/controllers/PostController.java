package my.blog.controllers;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
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
}

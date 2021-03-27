package my.blog.controllers;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import my.blog.models.Post;
import my.blog.services.PostService;

import javax.inject.Inject;
import java.util.List;

@Controller("/posts")
public class PostController {

    @Inject
    private PostService service;

    @Get
    public List<Post> getAllPosts() {
        return service.getAllPosts();
    }
}

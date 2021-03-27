package my.blog.repositories;

import my.blog.models.Post;

import java.util.List;

public interface PostRepository {

    List<Post> findAllPosts();
}

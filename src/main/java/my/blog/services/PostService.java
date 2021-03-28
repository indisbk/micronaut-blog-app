package my.blog.services;

import my.blog.models.Post;

import java.util.List;
import java.util.Optional;

public interface PostService {

    List<Post> getAllPosts();

    Optional<Post> getById(long id);

    Optional<Post> updatePost(Post post);

    boolean deletePostById(long id);

    Post createPost(Post post);
}

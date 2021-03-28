package my.blog.repositories;

import my.blog.models.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository {

    List<Post> findAllPosts();

    Optional<Post> findById(long id);

    Optional<Post> updatePost(Post post);

    boolean deletePostById(long id);

    Post createPost(Post post);
}

package my.blog.repositories;

import my.blog.models.Post;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Singleton
public class PostMemoryRepo implements PostRepository {

    @Inject
    private final MemoryStorage memoryStorage;

    public PostMemoryRepo(MemoryStorage memoryStorage) {
        this.memoryStorage = memoryStorage;
    }

    @Override
    public List<Post> findAllPosts() {
        return memoryStorage.getPosts();
    }

    @Override
    public Optional<Post> findById(long id) {
        return memoryStorage.getPosts().stream()
                .filter(post -> post.getId() == id)
                .findFirst();
    }

    @Override
    public Optional<Post> updatePost(Post post) {
        var postFound = findById(post.getId());
        if (postFound.isPresent()) {
            var oldPost = postFound.get();
            oldPost.setTitle(post.getTitle());
            oldPost.setText(post.getText());
        }
        return postFound;
    }

    @Override
    public boolean deletePostById(long id) {
        var postFound = findById(id);
        if (postFound.isPresent()) {
            memoryStorage.getPosts().remove(postFound.get());
            return true;
        } else return false;
    }

    @Override
    public Post createPost(Post post) {
        post.setId(memoryStorage.incrementAndGetId());
        post.setCreateDate(LocalDateTime.now());
        memoryStorage.getPosts().add(post);
        return post;
    }
}

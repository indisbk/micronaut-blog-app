package my.blog.repositories;

import my.blog.models.Post;

import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class PostMemoryRepo implements PostRepository {

    private final List<Post> posts;

    private long FIRST_NUM_FOR_ID = 1L;

    public PostMemoryRepo() {
        this.posts = Stream.of(
                Post.builder()
                        .id(incrementAndGetId())
                        .title("First post")
                        .text("Text of first post")
                        .author("Victor")
                        .dateTime(LocalDateTime.of(2020, 10, 10, 10, 10))
                        .build(),
                Post.builder()
                        .id(incrementAndGetId())
                        .title("Second post")
                        .text("Text of second post")
                        .author("Gregory")
                        .dateTime(LocalDateTime.of(2020, 11, 11, 11, 11))
                        .build(),
                Post.builder()
                        .id(incrementAndGetId())
                        .title("Third post")
                        .text("Text of third post")
                        .author("Kobayashi")
                        .dateTime(LocalDateTime.of(2020, 12, 12, 12, 12))
                        .build()
        ).collect(Collectors.toList());
    }

    @Override
    public List<Post> findAllPosts() {
        return this.posts;
    }

    @Override
    public Optional<Post> findById(long id) {
        return posts.stream()
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
            this.posts.remove(postFound.get());
            return true;
        } else return false;
    }

    @Override
    public Post createPost(Post post) {
        post.setId(incrementAndGetId());
        post.setDateTime(LocalDateTime.now());
        this.posts.add(post);
        return post;
    }

    private long incrementAndGetId() {
        return FIRST_NUM_FOR_ID++;
    }
}

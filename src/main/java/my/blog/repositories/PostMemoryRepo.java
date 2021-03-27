package my.blog.repositories;

import my.blog.models.Post;

import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Singleton
public class PostMemoryRepo implements PostRepository {

    private final List<Post> posts;

    private long FIRST_NUM_FOR_ID = 1L;

    public PostMemoryRepo() {
        this.posts = List.of(
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
        );
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

    private long incrementAndGetId() {
        return FIRST_NUM_FOR_ID++;
    }
}

package my.blog.repositories;

import my.blog.models.Post;

import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class MemoryStorage {

    private final List<Post> posts;

    private long FIRST_NUM_FOR_ID = 1L;

    public MemoryStorage() {
        Post post1 = Post.builder()
                .title("First post")
                .text("Text of first post")
                .author("Victor")
                .createDate(LocalDateTime.of(2020, 10, 10, 10, 10))
                .build();
        Post post2 = Post.builder()
                .title("Second post")
                .text("Text of second post")
                .author("Gregory")
                .createDate(LocalDateTime.of(2020, 11, 11, 11, 11))
                .build();
        Post post3 = Post.builder()
                .title("Third post")
                .text("Text of third post")
                .author("Kobayashi")
                .createDate(LocalDateTime.of(2020, 12, 12, 12, 12))
                .build();
        post1.setId(incrementAndGetId());
        post1.setGuid(UUID.randomUUID().toString());

        post2.setId(incrementAndGetId());
        post2.setGuid(UUID.randomUUID().toString());

        post3.setId(incrementAndGetId());
        post3.setGuid(UUID.randomUUID().toString());

        this.posts = Stream.of(post1, post2, post3).collect(Collectors.toList());
    }

    public long incrementAndGetId() {
        return FIRST_NUM_FOR_ID++;
    }

    public List<Post> getPosts() {
        return posts;
    }
}

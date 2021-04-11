package my.blog.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Schema(
        name = "Post",
        description = "Informational text about everything that is interesting to the author of the post"
)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post extends BaseModel {

    @Schema(description = "Post header", required = true)
    private String title;

    @Schema(description = "Main text of post", required = true)
    private String text;

    @Schema(description = "Post author", required = true)
    private String author;

    @Schema(description = "Creation date of post", required = true)
    private LocalDateTime createDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Post)) return false;
        Post post = (Post) o;
        return super.getId() == post.getId() &&
                super.getGuid().equals(post.getGuid()) &&
                title.equals(post.title) &&
                text.equals(post.text) &&
                author.equals(post.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.getId(), super.getGuid(), title, text, author);
    }

    @Override
    public String toString() {
        return "Post{" +
                "id='" + super.getId() + '\'' +
                ", guid='" + super.getGuid() + '\'' +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", author='" + author + '\'' +
                ", createDate=" + createDate +
                '}';
    }
}

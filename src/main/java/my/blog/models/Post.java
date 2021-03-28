package my.blog.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(
        name = "Post",
        description = "Informational text about everything that is interesting to the author of the post"
)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Schema(description = "Sequence identifier", hidden = true)
    private long id;

    @Schema(description = "Post header", required = true)
    private String title;

    @Schema(description = "Main text of post", required = true)
    private String text;

    @Schema(description = "Post author", required = true)
    private String author;

    @Schema(description = "Creation date of post", required = true)
    private LocalDateTime dateTime;
}

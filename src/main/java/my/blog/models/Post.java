package my.blog.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    private long id;

    private String title;

    private String text;

    private String author;

    private LocalDateTime dateTime;
}

package my.blog.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(
        name = "BaseModel",
        description = "Abstract class for all entities classes"
)
@Getter
@Setter
public abstract class BaseModel {

    @Schema(description = "Sequence identifier", hidden = true)
    private long id;

    @Schema(description = "Global unique identifier", hidden = true)
    private String guid;
}

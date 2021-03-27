package my.blog.errors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.blog.controllers.responses.CustomHttpResponse;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomHttpResponseError implements CustomHttpResponse {

    private int status;

    private String error;

    private String message;
}

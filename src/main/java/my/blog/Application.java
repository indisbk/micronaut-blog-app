package my.blog;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;

@OpenAPIDefinition(
        info = @Info (
                title = "web-blog",
                version = "0.0.1",
                description = "Server side of blog application by Micronaut"
        )
)
public class Application {
    public static void main(String[] args) {
        final Logger logger = LoggerFactory.getLogger(Application.class);
        final Collection<String> expected = Arrays.asList("PG_USERNAME", "PG_PASSWORD");

        System.getenv().entrySet().stream()
                .filter(e -> expected.contains(e.getKey()))
                .forEach(e -> logger.debug("{}={}", e.getKey(), e.getValue()));
        Micronaut.run(Application.class, args);
    }
}

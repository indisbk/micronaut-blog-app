package my.blog.jwt;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.*;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.ArrayList;

@Singleton
public class AuthProvider implements AuthenticationProvider {

    private final Logger logger = LoggerFactory.getLogger(AuthProvider.class);

    @Override
    public Publisher<AuthenticationResponse> authenticate(@Nullable HttpRequest<?> httpRequest, AuthenticationRequest<?, ?> authenticationRequest) {
        return Flowable.create(emitter -> {
            Object login = authenticationRequest.getIdentity();
            logger.debug("User {} tries to login...", login);
            if (login.equals("blog@gmail.net") &&
                    authenticationRequest.getSecret().equals("123456")) {
                emitter.onNext(new UserDetails((String) login, new ArrayList<>()));
                emitter.onComplete();
            } else {
                emitter.onError(new AuthenticationException(new AuthenticationFailed("Wrong username or password")));
            }
        }, BackpressureStrategy.ERROR);
    }
}

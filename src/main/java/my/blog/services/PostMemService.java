package my.blog.services;

import my.blog.models.Post;
import my.blog.repositories.PostRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

@Singleton
public class PostMemService implements PostService {

    @Inject
    private PostRepository memoryRepo;

    public List<Post> getAllPosts() {
        return memoryRepo.findAllPosts();
    }

    @Override
    public Optional<Post> getById(long id) {
        return memoryRepo.findById(id);
    }
}

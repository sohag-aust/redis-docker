package com.app.service;

import com.app.entity.Author;
import com.app.entity.Post;
import com.app.repository.AuthRepository;
import com.app.repository.PostRepository;
import com.app.request.PostRequest;
import com.app.request.RegistrationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppService {

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private PostRepository postRepository;

    //private RedisTemplate<String, Post> redisTemplate;

    private static int k = 0;

    //private HashOperations<> hashOperations; // redis data directly get kora jaina tai hashOperations use korte hoi

    //private HashOperations<String, Integer, Post> hashOperations;

//    public AppService(RedisTemplate<String, Post> redisTemplate) {
//        this.redisTemplate = redisTemplate;
//        hashOperations = redisTemplate.opsForHash();
//    }

    public Author register(RegistrationRequest registrationRequest) {
        Author author = new Author();
        author.setUsername(registrationRequest.getUsername());
        author.setPassword(registrationRequest.getPassword());
        return authRepository.save(author);
    }

    public Post createPost(PostRequest postRequest) {
        Post post = new Post();
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());

       // hashOperations.put("POST", (++k) , post);

        return postRepository.save(post);
    }

    public List<Post> getAllPosts() {
//        if(k>0){
//            return (List<Post>) hashOperations.entries("POST");
//        }

        return postRepository.findAll();


//        String temp = "";
//
//        for(Post p: posts){
//            temp = temp.concat(String.valueOf(p.getId()) + p.getTitle() + p.getContent() + "$");
//        }



        //return posts;
    }

    public Post getPostById(Integer id) {
        return postRepository.findById(id).orElse(null);
    }

    public Post updatePostById(Post newPost, Integer postId) {
        Post oldPost = postRepository.findById(postId) .orElse(null);

        oldPost.setTitle(newPost.getTitle());
        oldPost.setContent(newPost.getContent());

        return postRepository.save(oldPost);
    }

    public void deletePostById(Integer id) {
        postRepository.deleteById(id);
    }
}

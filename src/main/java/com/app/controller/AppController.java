package com.app.controller;

import com.app.entity.Post;
import com.app.request.LoginRequest;
import com.app.request.PostRequest;
import com.app.request.RegistrationRequest;
import com.app.response.AuthenticationResponse;
import com.app.service.AppService;
import com.app.service.MyUserDetailsService;
import com.app.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@RestController
public class AppController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private AppService appService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PostMapping("/signup")
    public ResponseEntity register(@RequestBody RegistrationRequest registerRequest) {
        appService.register(registerRequest);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequest authenticationRequest) throws Exception{

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        }catch(BadCredentialsException e) {
            throw new Exception("Incorrect Username or password" , e);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }

    @PostMapping("/post")
    public Post createPost(@RequestBody PostRequest postRequest){
        clearCache();
        return appService.createPost(postRequest);
    }

    @GetMapping("/posts")
    public String getAllPosts() throws IOException {

        if(redisTemplate.opsForValue().get("blog") == null){
            System.out.println("Fetching data from DB...");
            List<Post> list = appService.getAllPosts();

            ObjectMapper mapper = new ObjectMapper();
            String res = mapper.writeValueAsString(list);
            redisTemplate.opsForValue().set("blog", res);
        }

        return redisTemplate.opsForValue().get("blog");
    }

    @GetMapping("/post/{id}")
    public Post getPostById(@PathVariable("id") Integer id){
        System.out.println("Value Received from id: " + id);
        return appService.getPostById(id);
    }

    @PutMapping("/post/{id}")
    public Post updatePostById(@RequestBody Post post, @PathVariable("id") Integer id){
        clearCache();
        return appService.updatePostById(post, id);
    }

    @DeleteMapping("/post/{id}")
    public void deletePostById(@PathVariable("id") Integer id){
        clearCache();
        appService.deletePostById(id);
    }

    private void clearCache(){
        redisTemplate.delete("blog");
    }

}

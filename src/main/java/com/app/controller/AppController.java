package com.app.controller;

import com.app.entity.Post;
import com.app.request.LoginRequest;
import com.app.request.PostRequest;
import com.app.request.RegistrationRequest;
import com.app.response.AuthenticationResponse;
import com.app.service.AppService;
import com.app.service.MyUserDetailsService;
import com.app.util.JwtUtil;
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

import java.util.List;
import java.util.Map;

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
    private RedisTemplate<Integer, Post> redisTemplate;

    private HashOperations hashOperations;

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
        //deleteAllEntries();

        Post p = appService.createPost(postRequest);
        return p;
    }

    //@Cacheable(value = "POST")
    @GetMapping("/posts")
    public Map<Integer, Post> getAllPosts(){

        //List<Integer> l = appService.getLast();
        //int lastRow = l.get(0);
        int lastRow = appService.getLast();
        System.out.println("last id: " + lastRow);

        hashOperations = redisTemplate.opsForHash();

        Map<Integer, Post> mp = hashOperations.entries("blog");
        System.out.println("size of redis: " + mp.size());

        if(mp.size() < lastRow){

            System.out.println("Fetching Data from DB.....");
            List<Post> list = appService.getAllPosts();

            for(Integer i=mp.size();i<list.size();i++){
            //for(Post myPost : list){
                //hashOperations.delete("blog", myPost.getId());

//                if(hashOperations.get("blog", myPost.getId()) == null){
//                    hashOperations.put("blog", myPost.getId(), myPost);
//                }

                if(hashOperations.get("blog", i) == null){
                    hashOperations.put("blog", i, list.get(i));
                }

                //hashOperations.put("blog", myPost.getId(), myPost);
            }
        }

        return mp;
    }

    //@Cacheable(value = "POST", key = "#root.args[0]")
    @GetMapping("/post/{id}")
    public Post getPostById(@PathVariable("id") Integer id){
        System.out.println("Value Received from id: " + id);
        return appService.getPostById(id);
    }

//    @CacheEvict(value = "POST", allEntries = true)
//    @DeleteMapping("/deleteAll")
//    public void deleteAllEntries(){
//        System.out.println("Deleted ALL Entries..");
//    }

}

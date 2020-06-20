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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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

    @Autowired
    private RedisTemplate<String, Map<String, List<String> > >  redisTemplateJWT;

    @Autowired
    private RedisTemplate<String, String>  redisTemplate1;

    private HashOperations hashOperations;

    private static List<String> lst = new ArrayList<>();
    private static Map<String, List<String> > jwtListOfUser = new HashMap<>();

    @Value("${security.jwt.token.expire-length}")
    private long validityInMilliseconds;

    @Value("${security.jwt.refresh-token.expire-length}")
    private long refreshValidityInMilliseconds;

    @PostMapping("/signUp")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest registerRequest) {
        appService.register(registerRequest);
        return ResponseEntity.ok("Registration Completed !!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequest authenticationRequest) throws Exception{

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        }catch(BadCredentialsException e) {
            throw new Exception("Incorrect Username or password" , e);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());

        final String jwt = jwtTokenUtil.generateToken(userDetails,"access");
        final String refreshJwt = jwtTokenUtil.generateToken(userDetails, "refresh");

        // Save All Jwt Of individual User
        hashOperations = redisTemplateJWT.opsForHash();

        if(jwtListOfUser.get(authenticationRequest.getUsername()) != null) {
            lst = jwtListOfUser.get(authenticationRequest.getUsername());
        }else{
            lst.clear();
        }
        lst.add(jwt);
        System.out.println("list: " + lst);
        jwtListOfUser.put(authenticationRequest.getUsername(), lst);

        hashOperations.put("jwt", authenticationRequest.getUsername(), jwtListOfUser);
        // Save done


        //access token
        redisTemplate1.opsForValue().set(jwtTokenUtil.getJtiFromJwt(jwt), authenticationRequest.getUsername());
        redisTemplate1.expire(jwtTokenUtil.getJtiFromJwt(jwt), validityInMilliseconds, TimeUnit.MILLISECONDS);

        //refresh token
        redisTemplate1.opsForValue().set(jwtTokenUtil.getJtiFromJwt(refreshJwt), authenticationRequest.getUsername());
        redisTemplate1.expire(jwtTokenUtil.getJtiFromJwt(refreshJwt), refreshValidityInMilliseconds, TimeUnit.MILLISECONDS);

        System.out.println("end redis");

        Map<Object, Object> model = new HashMap<>();
        model.put("token", jwt);
        model.put("refresh-token", refreshJwt);

        return ResponseEntity.ok(model);
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logUserOut(HttpServletRequest request) {
        Map<String, String> model = new HashMap<>();
        String redisKey = jwtTokenUtil.getJtiFromJwt(jwtTokenUtil.getJwtFromRequest(request));
        redisTemplate.delete(redisKey);
        model.put("message", "successfully logged out user!");

        return ResponseEntity.ok(model);
    }

    @PostMapping(value = "/post", consumes = "application/json", produces = "application/json")
    public Post createPost(@RequestBody PostRequest postRequest){
        clearCache();
        return appService.createPost(postRequest);
    }

    @GetMapping(value = "/posts", consumes = "application/json", produces = "application/json")
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

    @GetMapping(value = "/post/{id}", consumes = "application/json", produces = "application/json")
    public Post getPostById(@PathVariable("id") Integer id){
        System.out.println("Value Received from id: " + id);
        return appService.getPostById(id);
    }

    @PutMapping(value = "/post/{id}", consumes = "application/json", produces = "application/json")
    public Post updatePostById(@RequestBody Post post, @PathVariable("id") Integer id){
        clearCache();
        return appService.updatePostById(post, id);
    }

    @DeleteMapping(value = "/post/{id}", consumes = "application/json", produces = "application/json")
    public void deletePostById(@PathVariable("id") Integer id){
        clearCache();
        appService.deletePostById(id);
    }

    @GetMapping(value = "/jwt/{name}", produces = "application/json")
    public List<String> getJwtByName(@PathVariable("name") String name){
        List<String> jwts = new ArrayList<>();
        if(hashOperations.get("jwt", name) != null){
            Map<String, List<String> > jwtFromHash = (Map<String, List<String> >) hashOperations.get("jwt", name);
            jwts = jwtFromHash.get(name);
            System.out.println("inside jwt by name: " + jwts);
        }
        return jwts;
    }

    private void clearCache(){
        redisTemplate.delete("blog");
    }
}

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
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return appService.createPost(postRequest);
    }

    @Cacheable(value = "POST")
    @GetMapping("/posts")
    public List<Post> getAllPosts(){
        System.out.println("DB Operation");
        return appService.getAllPosts();
    }

    //@Cacheable(value = "POST", key = "#root.args[0]")
    @GetMapping("/post/{id}")
    public Post getPostById(@PathVariable("id") Integer id){
        System.out.println("Value Received from id: " + id);
        return appService.getPostById(id);
    }

}

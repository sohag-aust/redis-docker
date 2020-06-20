package com.app.repository;

import com.app.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer>, CrudRepository<Post, Integer> {

    //@Query(value = "SELECT id FROM Post p ORDER BY p.id DESC LIMIT 1", nativeQuery = true)
    //Integer getLastValue();
    //@Query(value = "SELECT id FROM Post p ORDER BY p.id DESC")
    //@Query(value = "SELECT count(*) FROM Post p")
    //Integer getLastValue();


}

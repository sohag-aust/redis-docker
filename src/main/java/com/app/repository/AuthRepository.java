package com.app.repository;

import com.app.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<Author, Integer>, CrudRepository<Author, Integer> {
    Optional<Author> findByUsername(String username);
}

package com.Server.repo;

import com.Server.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;

import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String  email);

    Page<User> findAll(Pageable pageable);
}

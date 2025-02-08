package com.Server.repo;

import com.Server.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    boolean existsByEmail(String email);
    Optional<User> findByEmail(String  email);
}

package com.wisdom.auth.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.wisdom.auth.model.User;

public interface UserRepository extends MongoRepository<User, String> {

    User findByUsername(String username);

}

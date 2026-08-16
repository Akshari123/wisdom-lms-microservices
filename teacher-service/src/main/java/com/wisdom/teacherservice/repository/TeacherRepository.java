package com.wisdom.teacherservice.repository;

import com.wisdom.teacherservice.entity.Teacher;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends MongoRepository<Teacher, String> {

    Optional<Teacher> findByEmail(String email);

    boolean existsByEmail(String email);
}

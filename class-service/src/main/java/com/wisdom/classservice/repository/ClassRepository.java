package com.wisdom.classservice.repository;

import com.wisdom.classservice.entity.EducationClass;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassRepository extends MongoRepository<EducationClass, String> {
}
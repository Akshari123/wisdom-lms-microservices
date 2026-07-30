package com.wisdom.classservice.repository;

import com.wisdom.classservice.entity.EducationClass;
import org.springframework.data.jpa.repository.JpaRepository;



public interface ClassRepository extends JpaRepository<EducationClass, Long> {
}
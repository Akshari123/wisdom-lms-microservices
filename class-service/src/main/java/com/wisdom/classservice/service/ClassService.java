package com.wisdom.classservice.service;

import com.wisdom.classservice.entity.EducationClass;
import com.wisdom.classservice.exception.ResourceNotFoundException;
import com.wisdom.classservice.repository.ClassRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassService {

    private final ClassRepository classRepository;

    public ClassService(ClassRepository classRepository) {
        this.classRepository = classRepository;
    }

    public List<EducationClass> getAllClasses() {
        return classRepository.findAll();
    }

    public EducationClass getClassById(Long id) {
        return classRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Class not found with id: " + id
                        )
                );
    }

    public EducationClass createClass(EducationClass educationClass) {
        educationClass.setId(null);
        return classRepository.save(educationClass);
    }

    public EducationClass updateClass(
            Long id,
            EducationClass updatedClass) {

        EducationClass existingClass = getClassById(id);

        existingClass.setSubject(updatedClass.getSubject());
        existingClass.setTeacherId(updatedClass.getTeacherId());
        existingClass.setYear(updatedClass.getYear());
        existingClass.setClassDate(updatedClass.getClassDate());
        existingClass.setClassTime(updatedClass.getClassTime());

        return classRepository.save(existingClass);
    }

    public void deleteClass(Long id) {
        EducationClass existingClass = getClassById(id);
        classRepository.delete(existingClass);
    }
}
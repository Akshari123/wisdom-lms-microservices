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

    public EducationClass getClassById(String id) {
        return classRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Class not found with id: " + id));
    }

    public EducationClass createClass(EducationClass educationClass) {
        return classRepository.save(educationClass);
    }

    public EducationClass updateClass(String id, EducationClass classDetails) {

        EducationClass existingClass = classRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Class not found with id: " + id));

        existingClass.setSubject(classDetails.getSubject());
        existingClass.setTeacherId(classDetails.getTeacherId());
        existingClass.setYear(classDetails.getYear());
        existingClass.setClassDate(classDetails.getClassDate());
        existingClass.setClassTime(classDetails.getClassTime());

        return classRepository.save(existingClass);
    }

    public void deleteClass(String id) {

        EducationClass existingClass = classRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Class not found with id: " + id));

        classRepository.delete(existingClass);
    }
}
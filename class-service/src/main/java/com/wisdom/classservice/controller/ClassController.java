package com.wisdom.classservice.controller;

import com.wisdom.classservice.entity.EducationClass;
import com.wisdom.classservice.service.ClassService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
public class ClassController {

    private final ClassService classService;

    public ClassController(ClassService classService) {
        this.classService = classService;
    }

    // Get all classes
    @GetMapping
    public ResponseEntity<List<EducationClass>> getAllClasses() {
        return ResponseEntity.ok(classService.getAllClasses());
    }

    // Get one class by ID
    @GetMapping("/{id}")
    public ResponseEntity<EducationClass> getClassById(
            @PathVariable Long id) {

        return ResponseEntity.ok(classService.getClassById(id));
    }

    // Create a new class
    @PostMapping
    public ResponseEntity<EducationClass> createClass(
            @RequestBody EducationClass educationClass) {

        EducationClass createdClass =
                classService.createClass(educationClass);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdClass);
    }

    // Update an existing class
    @PutMapping("/{id}")
    public ResponseEntity<EducationClass> updateClass(
            @PathVariable Long id,
            @RequestBody EducationClass educationClass) {

        return ResponseEntity.ok(
                classService.updateClass(id, educationClass)
        );
    }

    // Delete a class
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClass(
            @PathVariable Long id) {

        classService.deleteClass(id);
        return ResponseEntity.noContent().build();
    }
}
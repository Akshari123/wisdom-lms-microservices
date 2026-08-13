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

    @GetMapping
    public ResponseEntity<List<EducationClass>> getAllClasses() {
        return ResponseEntity.ok(classService.getAllClasses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EducationClass> getClassById(@PathVariable String id) {
        return ResponseEntity.ok(classService.getClassById(id));
    }

    @PostMapping
    public ResponseEntity<EducationClass> createClass(
            @RequestBody EducationClass educationClass) {

        EducationClass createdClass = classService.createClass(educationClass);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdClass);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EducationClass> updateClass(
            @PathVariable String id,
            @RequestBody EducationClass classDetails) {

        return ResponseEntity.ok(
                classService.updateClass(id, classDetails)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClass(@PathVariable String id) {
        classService.deleteClass(id);
        return ResponseEntity.noContent().build();
    }
}
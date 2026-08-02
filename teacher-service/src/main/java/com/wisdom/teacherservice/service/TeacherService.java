package com.wisdom.teacherservice.service;

import com.wisdom.teacherservice.entity.Teacher;
import com.wisdom.teacherservice.repository.TeacherRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public TeacherService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    public Teacher getTeacherById(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Teacher not found with id: " + id
                ));
    }

    public Teacher createTeacher(Teacher teacher) {
        if (teacherRepository.existsByEmail(teacher.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Teacher email already exists"
            );
        }

        return teacherRepository.save(teacher);
    }

    public Teacher updateTeacher(Long id, Teacher updatedTeacher) {
        Teacher existingTeacher = getTeacherById(id);

        if (!existingTeacher.getEmail().equals(updatedTeacher.getEmail())
                && teacherRepository.existsByEmail(updatedTeacher.getEmail())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Teacher email already exists"
            );
        }

        existingTeacher.setName(updatedTeacher.getName());
        existingTeacher.setEmail(updatedTeacher.getEmail());
        existingTeacher.setPhone(updatedTeacher.getPhone());
        existingTeacher.setSubject(updatedTeacher.getSubject());

        return teacherRepository.save(existingTeacher);
    }

    public void deleteTeacher(Long id) {
        Teacher teacher = getTeacherById(id);
        teacherRepository.delete(teacher);
    }
}
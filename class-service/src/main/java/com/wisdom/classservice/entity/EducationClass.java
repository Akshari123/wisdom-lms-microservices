package com.wisdom.classservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "classes")
public class EducationClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String subject;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Column(nullable = false)
    private String year;

    @Column(name = "class_date", nullable = false)
    private LocalDate classDate;

    @Column(name = "class_time", nullable = false)
    private LocalTime classTime;

    public EducationClass() {
    }

    public EducationClass(
            String subject,
            Long teacherId,
            String year,
            LocalDate classDate,
            LocalTime classTime) {

        this.subject = subject;
        this.teacherId = teacherId;
        this.year = year;
        this.classDate = classDate;
        this.classTime = classTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public LocalDate getClassDate() {
        return classDate;
    }

    public void setClassDate(LocalDate classDate) {
        this.classDate = classDate;
    }

    public LocalTime getClassTime() {
        return classTime;
    }

    public void setClassTime(LocalTime classTime) {
        this.classTime = classTime;
    }
}
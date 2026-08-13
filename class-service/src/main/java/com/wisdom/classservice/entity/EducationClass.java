package com.wisdom.classservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;

@Document(collection = "classes")
public class EducationClass {

    @Id
    private String id;

    private String subject;
    private Long teacherId;
    private String year;
    private LocalDate classDate;
    private LocalTime classTime;

    public EducationClass() {
    }

    public EducationClass(String subject, Long teacherId, String year,
                          LocalDate classDate, LocalTime classTime) {
        this.subject = subject;
        this.teacherId = teacherId;
        this.year = year;
        this.classDate = classDate;
        this.classTime = classTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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
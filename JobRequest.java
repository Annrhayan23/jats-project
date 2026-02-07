package com.jats.dto;

import com.jats.entity.Job;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class JobRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Company is required")
    private String company;

    @NotNull(message = "Job type is required")
    private Job.JobType jobType;

    @NotNull(message = "Experience level is required")
    private Job.ExperienceLevel experienceLevel;

    private String salary;
    private String requirements;

    public JobRequest() {
    }

    public JobRequest(String title, String description, String location, String company, Job.JobType jobType,
            Job.ExperienceLevel experienceLevel, String salary, String requirements) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.company = company;
        this.jobType = jobType;
        this.experienceLevel = experienceLevel;
        this.salary = salary;
        this.requirements = requirements;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public Job.JobType getJobType() {
        return jobType;
    }

    public void setJobType(Job.JobType jobType) {
        this.jobType = jobType;
    }

    public Job.ExperienceLevel getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(Job.ExperienceLevel experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }
}

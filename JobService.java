package com.jats.service;

import com.jats.dto.*;
import com.jats.entity.Job;
import com.jats.entity.User;
import com.jats.repository.JobRepository;
import com.jats.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public JobService(JobRepository jobRepository, UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public JobResponse createJob(JobRequest request) {
        User recruiter = getCurrentUser();

        if (!recruiter.getRoles().contains(User.Role.RECRUITER) &&
                !recruiter.getRoles().contains(User.Role.ADMIN)) {
            throw new RuntimeException("Only recruiters can create jobs");
        }

        Job job = new Job();
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setCompany(request.getCompany());
        job.setJobType(request.getJobType());
        job.setExperienceLevel(request.getExperienceLevel());
        job.setSalary(request.getSalary());
        job.setRequirements(request.getRequirements());
        job.setRecruiter(recruiter);

        job = jobRepository.save(job);
        return toJobResponse(job);
    }

    @Transactional
    public JobResponse updateJob(Long jobId, JobRequest request) {
        User currentUser = getCurrentUser();
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getRecruiter().getId().equals(currentUser.getId()) &&
                !currentUser.getRoles().contains(User.Role.ADMIN)) {
            throw new RuntimeException("Unauthorized to update this job");
        }

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setCompany(request.getCompany());
        job.setJobType(request.getJobType());
        job.setExperienceLevel(request.getExperienceLevel());
        job.setSalary(request.getSalary());
        job.setRequirements(request.getRequirements());

        job = jobRepository.save(job);
        return toJobResponse(job);
    }

    @Transactional
    public void deleteJob(Long jobId) {
        User currentUser = getCurrentUser();
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getRecruiter().getId().equals(currentUser.getId()) &&
                !currentUser.getRoles().contains(User.Role.ADMIN)) {
            throw new RuntimeException("Unauthorized to delete this job");
        }

        job.setActive(false);
        jobRepository.save(job);
    }

    public JobResponse getJobById(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        return toJobResponse(job);
    }

    public Page<JobResponse> getAllActiveJobs(Pageable pageable) {
        return jobRepository.findByActiveTrue(pageable)
                .map(this::toJobResponse);
    }

    public Page<JobResponse> getMyJobs(Pageable pageable) {
        User recruiter = getCurrentUser();
        return jobRepository.findByRecruiter(recruiter, pageable)
                .map(this::toJobResponse);
    }

    public Page<JobResponse> searchJobs(String keyword, Pageable pageable) {
        return jobRepository.searchByKeyword(keyword, pageable)
                .map(this::toJobResponse);
    }

    public Page<JobResponse> filterJobs(String location, Job.JobType jobType,
            Job.ExperienceLevel experienceLevel, Pageable pageable) {
        return jobRepository.findWithFilters(location, jobType, experienceLevel, pageable)
                .map(this::toJobResponse);
    }

    private JobResponse toJobResponse(Job job) {
        JobResponse response = new JobResponse();
        response.setId(job.getId());
        response.setTitle(job.getTitle());
        response.setDescription(job.getDescription());
        response.setLocation(job.getLocation());
        response.setCompany(job.getCompany());
        response.setJobType(job.getJobType());
        response.setExperienceLevel(job.getExperienceLevel());
        response.setSalary(job.getSalary());
        response.setRequirements(job.getRequirements());
        response.setRecruiterName(job.getRecruiter().getFullName());
        response.setRecruiterEmail(job.getRecruiter().getEmail());
        response.setActive(job.isActive());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());
        return response;
    }
}

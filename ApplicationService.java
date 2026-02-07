package com.jats.service;

import com.jats.dto.*;
import com.jats.entity.Application;
import com.jats.entity.Job;
import com.jats.entity.User;
import com.jats.repository.ApplicationRepository;
import com.jats.repository.JobRepository;
import com.jats.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public ApplicationService(ApplicationRepository applicationRepository, JobRepository jobRepository,
            UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public ApplicationResponse createApplication(ApplicationRequest request) {
        User applicant = getCurrentUser();

        if (!applicant.getRoles().contains(User.Role.APPLICANT)) {
            throw new RuntimeException("Only applicants can apply for jobs");
        }

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.isActive()) {
            throw new RuntimeException("This job is no longer active");
        }

        if (applicationRepository.existsByApplicantAndJob(applicant, job)) {
            throw new RuntimeException("You have already applied for this job");
        }

        Application application = new Application();
        application.setApplicant(applicant);
        application.setJob(job);
        application.setCoverLetter(request.getCoverLetter());
        application.setResumeUrl(request.getResumeUrl());
        application.setStatus(Application.ApplicationStatus.APPLIED);

        application = applicationRepository.save(application);
        return toApplicationResponse(application);
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(Long applicationId, StatusUpdateRequest request) {
        User currentUser = getCurrentUser();

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // Only recruiter of the job or admin can update status
        if (!application.getJob().getRecruiter().getId().equals(currentUser.getId()) &&
                !currentUser.getRoles().contains(User.Role.ADMIN)) {
            throw new RuntimeException("Unauthorized to update this application");
        }

        // Validate status transition
        if (!application.canTransitionTo(request.getStatus())) {
            throw new RuntimeException("Invalid status transition from " +
                    application.getStatus() + " to " + request.getStatus());
        }

        application.setStatus(request.getStatus());
        application.setRecruiterNotes(request.getRecruiterNotes());
        application.setReviewedAt(LocalDateTime.now());

        application = applicationRepository.save(application);
        return toApplicationResponse(application);
    }

    @Transactional
    public void withdrawApplication(Long applicationId) {
        User currentUser = getCurrentUser();

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!application.getApplicant().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized to withdraw this application");
        }

        if (!application.canTransitionTo(Application.ApplicationStatus.WITHDRAWN)) {
            throw new RuntimeException("Cannot withdraw application in current status: " +
                    application.getStatus());
        }

        application.setStatus(Application.ApplicationStatus.WITHDRAWN);
        applicationRepository.save(application);
    }

    public ApplicationResponse getApplicationById(Long applicationId) {
        User currentUser = getCurrentUser();

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // Check authorization
        boolean isApplicant = application.getApplicant().getId().equals(currentUser.getId());
        boolean isRecruiter = application.getJob().getRecruiter().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRoles().contains(User.Role.ADMIN);

        if (!isApplicant && !isRecruiter && !isAdmin) {
            throw new RuntimeException("Unauthorized to view this application");
        }

        return toApplicationResponse(application);
    }

    public Page<ApplicationResponse> getMyApplications(Pageable pageable) {
        User applicant = getCurrentUser();
        return applicationRepository.findByApplicant(applicant, pageable)
                .map(this::toApplicationResponse);
    }

    public Page<ApplicationResponse> getApplicationsForJob(Long jobId, Pageable pageable) {
        User currentUser = getCurrentUser();

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getRecruiter().getId().equals(currentUser.getId()) &&
                !currentUser.getRoles().contains(User.Role.ADMIN)) {
            throw new RuntimeException("Unauthorized to view applications for this job");
        }

        return applicationRepository.findByJob(job, pageable)
                .map(this::toApplicationResponse);
    }

    public Page<ApplicationResponse> getRecruiterApplications(Application.ApplicationStatus status,
            Pageable pageable) {
        User recruiter = getCurrentUser();
        return applicationRepository.findByRecruiterAndStatus(recruiter, status, pageable)
                .map(this::toApplicationResponse);
    }

    private ApplicationResponse toApplicationResponse(Application application) {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setJobId(application.getJob().getId());
        response.setJobTitle(application.getJob().getTitle());
        response.setCompany(application.getJob().getCompany());
        response.setApplicantName(application.getApplicant().getFullName());
        response.setApplicantEmail(application.getApplicant().getEmail());
        response.setCoverLetter(application.getCoverLetter());
        response.setResumeUrl(application.getResumeUrl());
        response.setStatus(application.getStatus());
        response.setRecruiterNotes(application.getRecruiterNotes());
        response.setAppliedAt(application.getAppliedAt());
        response.setUpdatedAt(application.getUpdatedAt());
        response.setReviewedAt(application.getReviewedAt());
        return response;
    }
}

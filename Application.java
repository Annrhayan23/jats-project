package com.jats.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "applications", uniqueConstraints = @UniqueConstraint(columnNames = { "applicant_id", "job_id" }))
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(columnDefinition = "TEXT")
    private String coverLetter;

    private String resumeUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(columnDefinition = "TEXT")
    private String recruiterNotes;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime appliedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime reviewedAt;

    public Application() {
    }

    public Application(Long id, User applicant, Job job, String coverLetter, String resumeUrl, ApplicationStatus status,
            String recruiterNotes, LocalDateTime appliedAt, LocalDateTime updatedAt, LocalDateTime reviewedAt) {
        this.id = id;
        this.applicant = applicant;
        this.job = job;
        this.coverLetter = coverLetter;
        this.resumeUrl = resumeUrl;
        this.status = status;
        this.recruiterNotes = recruiterNotes;
        this.appliedAt = appliedAt;
        this.updatedAt = updatedAt;
        this.reviewedAt = reviewedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getApplicant() {
        return applicant;
    }

    public void setApplicant(User applicant) {
        this.applicant = applicant;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public String getResumeUrl() {
        return resumeUrl;
    }

    public void setResumeUrl(String resumeUrl) {
        this.resumeUrl = resumeUrl;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getRecruiterNotes() {
        return recruiterNotes;
    }

    public void setRecruiterNotes(String recruiterNotes) {
        this.recruiterNotes = recruiterNotes;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public enum ApplicationStatus {
        APPLIED, SHORTLISTED, INTERVIEW_SCHEDULED, INTERVIEW_COMPLETED, OFFERED, REJECTED, WITHDRAWN
    }

    public boolean canTransitionTo(ApplicationStatus newStatus) {
        switch (this.status) {
            case APPLIED:
                return newStatus == ApplicationStatus.SHORTLISTED ||
                        newStatus == ApplicationStatus.REJECTED ||
                        newStatus == ApplicationStatus.WITHDRAWN;
            case SHORTLISTED:
                return newStatus == ApplicationStatus.INTERVIEW_SCHEDULED ||
                        newStatus == ApplicationStatus.REJECTED ||
                        newStatus == ApplicationStatus.WITHDRAWN;
            case INTERVIEW_SCHEDULED:
                return newStatus == ApplicationStatus.INTERVIEW_COMPLETED ||
                        newStatus == ApplicationStatus.REJECTED ||
                        newStatus == ApplicationStatus.WITHDRAWN;
            case INTERVIEW_COMPLETED:
                return newStatus == ApplicationStatus.OFFERED ||
                        newStatus == ApplicationStatus.REJECTED;
            case OFFERED:
            case REJECTED:
            case WITHDRAWN:
                return false;
            default:
                return false;
        }
    }
}

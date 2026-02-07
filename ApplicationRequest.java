package com.jats.dto;

import jakarta.validation.constraints.NotNull;

public class ApplicationRequest {
    @NotNull(message = "Job ID is required")
    private Long jobId;

    private String coverLetter;
    private String resumeUrl;

    public ApplicationRequest() {
    }

    public ApplicationRequest(Long jobId, String coverLetter, String resumeUrl) {
        this.jobId = jobId;
        this.coverLetter = coverLetter;
        this.resumeUrl = resumeUrl;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
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
}

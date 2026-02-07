package com.jats.repository;

import com.jats.entity.Application;
import com.jats.entity.Job;
import com.jats.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    
    Page<Application> findByApplicant(User applicant, Pageable pageable);
    
    Page<Application> findByJob(Job job, Pageable pageable);
    
    @Query("SELECT a FROM Application a WHERE a.job.recruiter = :recruiter")
    Page<Application> findByRecruiter(@Param("recruiter") User recruiter, Pageable pageable);
    
    Page<Application> findByStatus(Application.ApplicationStatus status, Pageable pageable);
    
    @Query("SELECT a FROM Application a WHERE a.job.recruiter = :recruiter " +
           "AND (:status IS NULL OR a.status = :status)")
    Page<Application> findByRecruiterAndStatus(
        @Param("recruiter") User recruiter,
        @Param("status") Application.ApplicationStatus status,
        Pageable pageable
    );
    
    Optional<Application> findByApplicantAndJob(User applicant, Job job);
    
    boolean existsByApplicantAndJob(User applicant, Job job);
    
    @Query("SELECT COUNT(a) FROM Application a WHERE a.job = :job AND a.status = :status")
    long countByJobAndStatus(@Param("job") Job job, @Param("status") Application.ApplicationStatus status);
}

package com.jats.repository;

import com.jats.entity.Job;
import com.jats.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    
    Page<Job> findByActiveTrue(Pageable pageable);
    
    Page<Job> findByRecruiter(User recruiter, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.active = true " +
           "AND (:location IS NULL OR j.location LIKE %:location%) " +
           "AND (:jobType IS NULL OR j.jobType = :jobType) " +
           "AND (:experienceLevel IS NULL OR j.experienceLevel = :experienceLevel)")
    Page<Job> findWithFilters(
        @Param("location") String location,
        @Param("jobType") Job.JobType jobType,
        @Param("experienceLevel") Job.ExperienceLevel experienceLevel,
        Pageable pageable
    );
    
    @Query("SELECT j FROM Job j WHERE j.active = true " +
           "AND (LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Job> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}

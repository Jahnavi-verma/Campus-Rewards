package com.campusrecycle.repository;

import com.campusrecycle.model.RecyclingSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HeatmapSubmissionRepository extends JpaRepository<RecyclingSubmission, Long> {

    // Converts the Postgres timestamp into a unique 'YYYY-MM-DD' text string list
    @Query(value = "SELECT DISTINCT TO_CHAR(submitted_at, 'YYYY-MM-DD') " +
                   "FROM recycling_submissions " +
                   "WHERE user_id = :userId AND status = 'APPROVED' " +
                   "ORDER BY TO_CHAR(submitted_at, 'YYYY-MM-DD') DESC", 
           nativeQuery = true)
    List<String> findUniqueActivityDatesByUserId(@Param("userId") Long userId);
}
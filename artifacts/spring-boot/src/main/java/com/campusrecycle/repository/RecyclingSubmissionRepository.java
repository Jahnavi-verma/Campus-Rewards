package com.campusrecycle.repository;

import com.campusrecycle.model.RecyclingSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecyclingSubmissionRepository extends JpaRepository<RecyclingSubmission, Long> {

    List<RecyclingSubmission> findByUserIdOrderBySubmittedAtDesc(Long userId);

    List<RecyclingSubmission> findByStatusOrderBySubmittedAtDesc(String status);

    List<RecyclingSubmission> findTop20ByOrderBySubmittedAtDesc();

    @Query("SELECT SUM(s.pointsEarned) FROM RecyclingSubmission s WHERE s.user.id = :userId AND s.status = 'APPROVED'")
    Integer sumApprovedPointsByUserId(@Param("userId") Long userId);

    @Query("SELECT s.itemType, COUNT(s), SUM(s.quantity) FROM RecyclingSubmission s WHERE s.status = 'APPROVED' GROUP BY s.itemType")
    List<Object[]> getStatsByItemType();
}

package com.thv.sport.system.respository;

import com.thv.sport.system.dto.response.user.UserStatsResponse;
import com.thv.sport.system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);

    boolean existsByConfirmForgot(String token);

    Optional<User> findByConfirmForgot(String confirmForgot);

    Optional<User> findByConfirmationToken(String token);

    // Find users whose full name contains the given value (case-insensitive)
    java.util.List<User> findByFullNameContainingIgnoreCase(String fullName);

    // Single JPQL query to search users by name using LIKE (case-insensitive).
    // If userName is null or empty, this will return all users.
    @Query("SELECT u FROM User u " +
            "WHERE (:userName IS NULL OR :userName = '' " +
            "OR LOWER(u.fullName) " +
            "LIKE LOWER(CONCAT('%', :userName, '%'))) " +
            "order by u.userId ASC ")
    java.util.List<User> findAllByUserNameLike(@Param("userName") String userName);

    // Returns aggregated user statistics:
    //  - totalUsers: total number of users
    //  - newUsers: number of users created at or after the provided `since` timestamp
    //  - lockedUsers: number of users with isLocked = true
    @Query("SELECT new com.thv.sport.system.dto.response.user.UserStatsResponse(COUNT(u), " +
            "SUM(CASE WHEN u.createdAt >= :since THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN u.isLocked = true THEN 1 ELSE 0 END)) FROM User u")
    UserStatsResponse getUserStatsSince(@Param("since") LocalDateTime since);

}

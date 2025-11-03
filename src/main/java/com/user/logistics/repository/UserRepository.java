package com.user.logistics.repository;



import com.user.logistics.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsByEmail(String mail);

    List<User> findByRole(String driver);

    Optional<User> findByEmail(String email);



    User findByResetToken(String resetToken);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.resetToken = ?2, u.tokenExpiry = ?3 WHERE u.email = ?1")
    void saveResetToken(String email, String token, LocalDateTime expiry);

    @Modifying
    @Query("UPDATE User u SET u.resetToken = NULL, u.tokenExpiry = NULL WHERE u.resetToken = ?1")
    void invalidateResetToken(String token);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<User> findByPhoneNumber(String identifier);
}

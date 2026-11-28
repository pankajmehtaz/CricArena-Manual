package com.cricarena.backend.repository;

import com.cricarena.backend.entity.UserInvitation;
import com.cricarena.backend.entity.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserInvitationRepository extends JpaRepository<UserInvitation, Long> {

    Optional<UserInvitation> findByTokenHash(String tokenHash);

    List<UserInvitation> findAllByEmail(String email);

    List<UserInvitation> findAllByEmailAndStatus(String email, InvitationStatus status);

    boolean existsByEmailAndStatus(String email, InvitationStatus status);

    boolean existsByEmailAndCollege_IdAndStatus(String email, Long collegeId, InvitationStatus status);

    List<UserInvitation> findAllByOrderByCreatedAtDesc();
}

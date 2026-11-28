package com.cricarena.backend.service;

import java.time.LocalDateTime;

public interface InvitationNotificationService {

    void sendInvitationEmail(String recipientEmail, String collegeName, String invitationUrl, LocalDateTime expiresAt);
}

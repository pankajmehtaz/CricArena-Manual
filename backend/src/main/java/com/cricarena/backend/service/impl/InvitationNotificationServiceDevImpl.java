package com.cricarena.backend.service.impl;

import com.cricarena.backend.service.InvitationNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class InvitationNotificationServiceDevImpl implements InvitationNotificationService {

    private static final Logger log = LoggerFactory.getLogger(InvitationNotificationServiceDevImpl.class);

    @Override
    public void sendInvitationEmail(String recipientEmail, String collegeName, String invitationUrl, LocalDateTime expiresAt) {
        log.info("================================================================================");
        log.info("[DEV EMAIL DISPATCH] College Admin Invitation Notification");
        log.info("To: {}", recipientEmail);
        log.info("College: {}", collegeName);
        log.info("Invitation Link: {}", invitationUrl);
        log.info("Expires At: {}", expiresAt);
        log.info("Instructions: Open the link above in the browser to set your password and activate your account.");
        log.info("================================================================================");
    }
}

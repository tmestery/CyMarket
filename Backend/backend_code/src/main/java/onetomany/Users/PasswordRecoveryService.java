package onetomany.Users;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PasswordRecoveryService {

    private static final int CODE_LENGTH = 6;
    private static final long EXPIRATION_MINUTES = 15;

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String defaultFromAddress;

    public PasswordRecoveryService(JavaMailSender mailSender,
                                   UserRepository userRepository,
                                   @Value("${spring.mail.username:}") String defaultFromAddress) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
        this.defaultFromAddress = defaultFromAddress;
    }

    public User prepareRecoveryCode(User user) {
        String recoveryCode = generateRecoveryCode();
        user.setPasswordRecoveryCode(recoveryCode);
        user.setPasswordRecoveryExpiry(Date.from(Instant.now().plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES)));
        return userRepository.save(user);
    }

    public void sendRecoveryEmail(User user) {
        User updatedUser = prepareRecoveryCode(user);
        SimpleMailMessage message = new SimpleMailMessage();
        if (defaultFromAddress != null && !defaultFromAddress.isBlank()) {
            message.setFrom(defaultFromAddress);
        }
        message.setTo(updatedUser.getEmailId());
        message.setSubject("Password Recovery Code");
        message.setText("Your password recovery code is: " + updatedUser.getPasswordRecoveryCode()
                + "\nThis code will expire in " + EXPIRATION_MINUTES + " minutes."
                + "\nIf you did not request this, please ignore this email.");

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to send recovery email", ex);
        }
    }

    public boolean isRecoveryCodeValid(User user, String submittedCode) {
        if (user.getPasswordRecoveryCode() == null || user.getPasswordRecoveryExpiry() == null) {
            return false;
        }
        if (!user.getPasswordRecoveryCode().equals(submittedCode)) {
            return false;
        }
        return user.getPasswordRecoveryExpiry().after(new Date());
    }

    public void clearRecoveryDetails(User user) {
        user.setPasswordRecoveryCode(null);
        user.setPasswordRecoveryExpiry(null);
    }

    private String generateRecoveryCode() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            builder.append(secureRandom.nextInt(10));
        }
        return builder.toString();
    }
}

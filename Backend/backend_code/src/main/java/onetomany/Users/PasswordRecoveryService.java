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
    private final String fromAddress;

    public PasswordRecoveryService(JavaMailSender mailSender,
                                   UserRepository userRepository,
                                   @Value("${app.mail.from:}") String fromAddress) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
        this.fromAddress = fromAddress;
    }

    public User prepareRecoveryCode(User user) {
        String recoveryCode = generateRecoveryCode();
        user.setPasswordRecoveryCode(recoveryCode);
        user.setPasswordRecoveryExpiry(Date.from(
                Instant.now().plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES)));
        return userRepository.save(user);
    }

    public void sendRecoveryEmail(User user) {
        System.out.println(fromAddress);
        if (fromAddress == null || fromAddress.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Configura app.mail.from con un remitente verificado en SendGrid");
        }
        User updatedUser = prepareRecoveryCode(user);
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);                     //  remitente verificado
        msg.setTo(updatedUser.getEmailId());
        msg.setSubject("Password Recovery Code");
        msg.setText("Your password recovery code is: " + updatedUser.getPasswordRecoveryCode()
                + "\nThis code will expire in " + EXPIRATION_MINUTES + " minutes."
                + "\nIf you did not request this, please ignore this email.");
        mailSender.send(msg);
    }

    public boolean isRecoveryCodeValid(User user, String submittedCode) {
        if (user.getPasswordRecoveryCode() == null || user.getPasswordRecoveryExpiry() == null) return false;
        if (!user.getPasswordRecoveryCode().equals(submittedCode)) return false;
        return user.getPasswordRecoveryExpiry().after(new Date());
    }

    public void clearRecoveryDetails(User user) {
        user.setPasswordRecoveryCode(null);
        user.setPasswordRecoveryExpiry(null);
    }

    private String generateRecoveryCode() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) b.append(secureRandom.nextInt(10));
        return b.toString();
    }
}

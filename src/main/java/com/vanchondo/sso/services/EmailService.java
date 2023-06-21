package com.vanchondo.sso.services;

import com.vanchondo.sso.configs.properties.EmailConfiguration;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.springframework.stereotype.Service;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class EmailService {
    private EmailConfiguration emailConfiguration;
    private final Session session;

    public EmailService(EmailConfiguration emailConfiguration) {
        this.emailConfiguration = emailConfiguration;
        session = Session.getInstance(emailConfiguration.getProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        emailConfiguration.getUsername(),
                        emailConfiguration.getPassword()
                );
            }
        });
    }

    public void sendEmail(String toEmail) throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailConfiguration.getPassword()));
        message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Test Mail Subject");

        String msg = "This is my first email using JavaMailer";

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(msg, "text/html; charset=utf-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        message.setContent(multipart);

        Transport.send(message);
    }

}

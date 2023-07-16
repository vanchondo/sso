package com.vanchondo.sso.services;

import com.vanchondo.sso.configs.properties.EmailConfiguration;
import com.vanchondo.sso.utilities.EmailUtil;

import org.springframework.stereotype.Service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@Log4j2
public class EmailService {
    private final EmailConfiguration emailConfiguration;
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

    public void sendEmail(String toEmail, String token) throws MessagingException, TemplateException, IOException {
        String methodName = "::sendEmail::";
        log.info("{}Sending validation email to={}", methodName, toEmail);
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailConfiguration.getFrom(), "NoReply"));
        message.setReplyTo(InternetAddress.parse(emailConfiguration.getFrom(), false));
        message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
        message.setSubject("Verificar cuenta");
        String link = String.format("https://login.victoranchondo.com/validate?email=%s&token=%s", EmailUtil.encode(toEmail), EmailUtil.encode(token));

        String msg = getEmailBody(link);
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(msg, "text/html; charset=utf-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);
        message.setContent(multipart);

        Transport.send(message);
        log.info("{}Email sent successfully to={}", methodName, toEmail);
    }

    private String getEmailBody(String link) throws IOException, TemplateException {
        // Creates the freemarker configuration
        Configuration cfg = getConfiguration("/templates/email");

        // Set the properties
        Map<String, Object> properties = getEmailPropertyMap(link);

        // Get the template
        Template template = cfg.getTemplate("emailValidation.ftl");

        // Write the freemarker output to a StringWriter
        StringWriter stringWriter = new StringWriter();
        template.process(properties, stringWriter);

        // Get the String from StringWritter
        return stringWriter.toString();
    }

    private Configuration getConfiguration(String templatesFolder) {
        Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        // Set templates folder
        cfg.setClassForTemplateLoading(this.getClass(), templatesFolder);

        // Recommended settings
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.US);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        return cfg;
    }

    private Map<String, Object> getEmailPropertyMap(String link) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("link", link);

        return properties;
    }

}

package org.acme.emailservice.email;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

@ApplicationScoped
public class EmailSender {

    private static Logger log = Logger.getLogger(EmailSender.class);

    @Inject
    Config config;

    String invitationTextBodyTemplate;
    String invitationHtmlBodyTemplate;
    String notificationTextBodyTemplate;
    String notificationHtmlBodyTemplate;

    @ConfigProperty(name = "email.invitation-text-body-template")
    String invitationTextBodyTemplateUri;

    @ConfigProperty(name = "email.invitation-html-body-template")
    String invitationHtmlBodyTemplateUri;

    @ConfigProperty(name = "email.notification-text-body-template")
    String notificationTextBodyTemplateUri;

    @ConfigProperty(name = "email.notification-html-body-template")
    String notificationHtmlBodyTemplateUri;

    public void send(String emailAddress, String subject, String textBody, String htmlBody) throws Exception {
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");

        props.put("mail.smtp.port", config.getPort());
        props.put("mail.smtp.starttls.enable", config.getStarttls());
        props.put("mail.smtp.auth", config.getAuth());

        Session session = Session.getDefaultInstance(props);

        MimeMessage msg = new MimeMessage(session);
        Multipart multipart = new MimeMultipart();

        if (textBody != null) {
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(textBody, "UTF-8");
            multipart.addBodyPart(textPart);
        }

        if (htmlBody != null) {
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlBody, "text/html; charset=UTF-8");
            multipart.addBodyPart(htmlPart);
        }

        msg.setFrom(new InternetAddress(config.getFrom(),
        config.getFromDisplayName()));
        msg.setRecipient(Message.RecipientType.TO, new
        InternetAddress(emailAddress));
        msg.setSubject(subject);
        msg.setContent(multipart);

        Transport transport = session.getTransport("smtp");

        try {
            transport.connect(config.getSmtpHost(), config.getFrom(), config.getPassword());
            transport.sendMessage(msg, msg.getAllRecipients());
        } catch (Exception e) {
            log.warn("Failed to send email", e);
            e.printStackTrace();
        } finally {
            try {
                transport.close();
            } catch (MessagingException e) {
                log.warn("Failed to close transport", e);
            }
        }
    }

    public void SendNotificationEmail(String emailAddress) throws Exception {
        String subject = config.getFromDisplayName() + " " + config.getNotificationSubject();
        String textBody = notificationTextBodyTemplate;
        String htmlBody = notificationHtmlBodyTemplate;

        send(emailAddress, subject, textBody, htmlBody);
    }

    public void SendInvitationEmail(String emailAddress) throws Exception {
        String subject = config.getFromDisplayName() + " " + config.getInvitationSubject();
        String textBody = invitationTextBodyTemplate;
        String htmlBody = invitationHtmlBodyTemplate;

        send(emailAddress, subject, textBody, htmlBody);
    }

    @PostConstruct
    public void init() throws IOException, URISyntaxException {
        invitationTextBodyTemplate = LoadTemplate(invitationTextBodyTemplateUri);
        invitationHtmlBodyTemplate = LoadTemplate(invitationHtmlBodyTemplateUri);
        notificationTextBodyTemplate = LoadTemplate(notificationTextBodyTemplateUri);
        notificationHtmlBodyTemplate = LoadTemplate(notificationHtmlBodyTemplateUri);
    }

    public String LoadTemplate(String templateUri) throws URISyntaxException, IOException {
        URI uri = null;
        if (templateUri.startsWith("classpath:")) {
            uri = EmailSender.class.getResource(templateUri.substring("classpath:".length())).toURI();
        } else {
            uri = URI.create(templateUri);
        }

        return Files.readAllLines(Paths.get(uri)).stream().collect(Collectors.joining("\n"));
    }
}

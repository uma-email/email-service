package org.acme.emailservice.email;

import java.util.Date;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.acme.emailservice.exception.EmailServiceException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Provider;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

@ApplicationScoped
public class EmailSender {

    private static Logger log = Logger.getLogger(EmailSender.class);

    @ConfigProperty(name = "SMTP_PASSWORD")
    public String password;

    @Inject
    Config config;

    private static final String SENDER_FROM = "no-reply@umabox.org";
    private static final String SENDER_NAME = "no-reply";
    private static final String SENDER_SMTP = "mail.umabox.org";
    private static final int SMTP_PORT = 587;

    public boolean send(String emailAddress, String subject, String textBody, String htmlBody) throws Exception {
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        // props.put("mail.smtp.class", "com.sun.mail.smtp.SMTPTransport");

        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props);

        // String prot = "smtp";

        // Provider p = new Provider(Provider.Type.TRANSPORT, prot, "smtpsend$SMTPExtension", "Jakarta Mail demo",
        //         "no version");
        // props.put("mail." + prot + ".class", "smtpsend$SMTPExtension");

        // session.addProvider(p);

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(SENDER_FROM, SENDER_NAME));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(emailAddress));
        msg.setSubject("[no-reply] " + subject);
        msg.setContent(textBody, "text/html;charset=utf-8");

        Transport transport = session.getTransport("smtp");

        try {
            transport.connect(SENDER_SMTP, SENDER_FROM, password);
            transport.sendMessage(msg, msg.getAllRecipients());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            transport.close();
        }
        return false;
    }

    /*
     * public void send(String emailAddress, String subject, String textBody, String
     * htmlBody) throws EmailServiceException {
     * 
     * Transport transport = null; try { Properties props = new Properties();
     * 
     * props.setProperty("mail.transport.protocol", "smtp");
     * 
     * props.setProperty("mail.smtp.host", config.smtpHost);
     * 
     * props.setProperty("mail.smtp.port", config.getPort());
     * 
     * if (config.auth) { props.setProperty("mail.smtp.auth", "true"); }
     * 
     * // if (config.ssl) { // props.setProperty("mail.smtp.ssl.enable", "true"); //
     * }
     * 
     * if (config.starttls) { props.setProperty("mail.smtp.starttls.enable",
     * "true"); }
     * 
     * // if (ssl || starttls) { // props.put("mail.smtp.ssl.protocols",
     * SUPPORTED_SSL_PROTOCOLS);
     * 
     * // setupTruststore(props); // }
     * 
     * // props.setProperty("mail.smtp.timeout", "10000"); //
     * props.setProperty("mail.smtp.connectiontimeout", "10000");
     * 
     * String from = config.from; String fromDisplayName = config.fromDisplayName;
     * // String replyTo = config.replyTo; // String replyToDisplayName =
     * config.replyToDisplayName; // String envelopeFrom = config.envelopeFrom;
     * 
     * Session session = Session.getInstance(props);
     * 
     * Multipart multipart = new MimeMultipart();
     * 
     * if (textBody != null) { MimeBodyPart textPart = new MimeBodyPart();
     * textPart.setText(textBody, "UTF-8"); multipart.addBodyPart(textPart); }
     * 
     * if (htmlBody != null) { MimeBodyPart htmlPart = new MimeBodyPart();
     * htmlPart.setContent(htmlBody, "text/html; charset=UTF-8");
     * multipart.addBodyPart(htmlPart); }
     * 
     * Message msg = new MimeMessage(session); msg.setFrom(new InternetAddress(from,
     * fromDisplayName, "utf-8"));
     * 
     * msg.setHeader("To", emailAddress); msg.setSubject(subject);
     * msg.setContent(multipart); // msg.saveChanges(); msg.setSentDate(new Date());
     * 
     * transport = session.getTransport(); if (config.auth) {
     * transport.connect(config.smtpHost, config.user.get(), config.password.get());
     * } else { transport.connect(); } transport.sendMessage(msg, new
     * InternetAddress[]{new InternetAddress(emailAddress)}); } catch (Exception e)
     * { log.info("Failed to send an email", e); throw new EmailServiceException(e);
     * } finally { if (transport != null) { try { transport.close(); } catch
     * (MessagingException e) { log.warn("Failed to close transport", e); } } } }
     */
}

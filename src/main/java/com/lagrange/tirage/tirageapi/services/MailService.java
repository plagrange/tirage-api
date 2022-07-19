/**
 * 
 */
package com.lagrange.tirage.tirageapi.services;

import com.lagrange.tirage.tirageapi.exceptions.ErrorCodesEnum;
import com.lagrange.tirage.tirageapi.exceptions.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * @author pmekeze
 *
 */
@Slf4j
@Service
public class MailService {

    private static MailService instance;

    private Transport transport;

    private MimeMessage message;

    private Session session;

    static final String USERNAME = "bomintech@gmail.com";
    static final String CRITERIA = "MbomintecH11";

    public MailService() {
        configure();
    }

    public static MailService getInstance() {

        if (instance == null) {
            instance = new MailService();
        }

        return instance;
    }

    public void configure() {

        // 1 -> Création de la session
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.ssl.checkserveridentity", true);

        session = Session.getInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, CRITERIA);
            }
        });

        message = new MimeMessage(session);

    }

    public void sendMail(String to, String company, String criteria) throws UserException {

        String texte = "Bonjour " + to.substring(0, to.indexOf("@")) + ",\n";

        texte = texte
                + "Veuillez cliquer sur le lien ci-dessous pour participer au tirage au sort de la reunion \n      https://www.lagrangien.fr/tirageapp/index.html#/tirage/"
                + company + "/" + to + " \nVotre code de sécurité pour le tirage est : " + criteria
                + " \n Bonne Change et a bientot \n \n Dr Ing Col Cpt Lagrange";

            try {

                message.setText(texte);
                message.setFrom(new InternetAddress(USERNAME));
                message.setSubject("Tirage au sort");
                message.addRecipients(Message.RecipientType.TO, to);

                transport = session.getTransport();
                transport.connect();
                transport.sendMessage(message, new Address[] { new InternetAddress(to) });

            } catch (MessagingException e) {
                log.warn("Was not able to notify user :" + to, e);
                throw  new UserException(ErrorCodesEnum.NOTIFY_PARTICIPANT_FAILED);
            } finally {
                try {
                    if (transport != null) {
                        transport.close();
                    }
                } catch (MessagingException e) {
                    log.warn("Was not able to notify user :" + to, e);
                    throw  new UserException(ErrorCodesEnum.NOTIFY_PARTICIPANT_FAILED);
                }
            }
    }
}

package com.bingo.utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


/**
 * @author Abhinav Gupta
 * @version 1.0
 * @since 12-May-2020
 */
@Service
public class EmailService {

    private static final String EMAIL_SUBJECT = "Bingo Slips !!!";

    private static final String SENDER_NAME = "Abhinav";

    private static final String BINGO_SLIP_ATTACHMENT_NAME_PREFIX = "Bingo-slip-";

    private JavaMailSender javaMailSender;

    @Autowired
    FileIOService fileIOService;

    @Autowired
    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public boolean sendMail(String toEmail, String subject, String textMessage, byte[] content, String gameId, String email, String contentId) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);// true indicates multipart message

            helper.setFrom("abhinav.g@comakeit.com");
            helper.setSubject(subject);
            helper.setTo(toEmail);
            helper.addAttachment(BINGO_SLIP_ATTACHMENT_NAME_PREFIX + email + ".pdf", new ByteArrayResource(content));
            helper.setText(textMessage, true);// true indicates body is html

            ClassPathResource classPathResource = new ClassPathResource("static/bingo_game_image.jpg");
            helper.addInline(contentId, classPathResource);

            javaMailSender.send(message);
            
            System.out.println("Email sent successfully to : " + toEmail);
            return true;
        } catch (MessagingException e) {
            System.out.println("Email could not be sent to : " + toEmail);
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.out.println("Email could not be sent to : " + toEmail);
            e.printStackTrace();
            return false;
        }

    }

    public List<String> sendMailToParticipants(List<String> emails, String gameId) {

        List<String> emailNotSent = new ArrayList<String>();

        String contentId = ContentIdGenerator.getContentId();

        String mailMessage = getEmailContent(gameId, contentId);

        String mailSubject = EMAIL_SUBJECT;

        emails.forEach(e -> {
            String userSlipPdfName = fileIOService.getUserSlipPdfName(gameId, e);
            Path path = Paths.get(userSlipPdfName);
            byte[] content;
            try {
                content = Files.readAllBytes(path);
                boolean isMailSent = sendMail(e, mailSubject, mailMessage, content, gameId, e, contentId);
                if (!isMailSent) {
                    emailNotSent.add(e);
                }
                TimeUnit.SECONDS.sleep(6);
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        });
        return emailNotSent;
    }

    private String getEmailContent(String gameId, String contentId) {
        return "Hi,<br />Hope u are having a good day. <br />"
                + "Here are your Bingo slips. Kindly check attachment. <br />"
                + "Please take a print out or write down the numbers of each ticket separately before the games starts &#128578<br />"
                + "Enjoy !!!"
                + "  <br /><br /><br /> "
                + "Game id#" + gameId
                + " <br />"
                + "<img height='400' width='400' src=\"cid:" + contentId + "\" />"
                + "<br /><br />"
                + "Regards <br />~- &#129505; " + SENDER_NAME;
    }
}

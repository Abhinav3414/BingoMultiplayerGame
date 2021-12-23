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

import com.bingo.dao.BingoUser;


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

    public boolean sendMail(String toEmail, String subject, String textMessage, byte[] fileContent, String gameId,
            String contentId) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);// true indicates multipart message

            helper.setFrom("bingo.multiplayer.game@gmail.com");
            helper.setSubject(subject);
            helper.setTo(toEmail);
            helper.addAttachment(BINGO_SLIP_ATTACHMENT_NAME_PREFIX + toEmail + ".pdf",
                    new ByteArrayResource(fileContent));
            helper.setText(textMessage, true);// true indicates body is html

			try {
				ClassPathResource classPathResource = new ClassPathResource("bingo_game_image.jpg");
				helper.addInline(contentId, classPathResource);
			} catch (Exception e) {
				System.out.println("Could not attach image : bingo_game_image.jpg");
				e.printStackTrace();
			}

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

    public boolean sendMailToLeader(String toEmail, String subject, String gameId, String line1, String line2,
            String line3) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);// true indicates multipart message

            helper.setSubject(subject);
            helper.setTo(toEmail);
            helper.setText(getEmailContentForLeader(line1, line2, line3, gameId), true);// true indicates body is html

            javaMailSender.send(message);

            System.out.println("Email sent successfully to Game leader: " + toEmail);
            return true;
        } catch (MessagingException e) {
            System.out.println("Email could not be sent to Game leader: " + toEmail);
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.out.println("Email could not be sent to Game leader: " + toEmail);
            e.printStackTrace();
            return false;
        }

    }

    public List<String> sendMailToParticipants(List<BingoUser> users, String gameId, String gameRoomUrl) {

        List<String> emailNotSent = new ArrayList<String>();

        String contentId = ContentIdGenerator.getContentId();

        String mailSubject = EMAIL_SUBJECT;

        users.forEach(bu -> {

            String mailMessage = getEmailContent(gameId, contentId, bu.getUserId(), gameRoomUrl);

            String userSlipPdfName = fileIOService.getUserSlipPdfName(gameId, bu.getEmail(), null);
            Path path = Paths.get(userSlipPdfName);
            byte[] content;
            try {
                content = Files.readAllBytes(path);
                boolean isMailSent = sendMail(bu.getEmail(), mailSubject, mailMessage, content, gameId, contentId);
                if (!isMailSent) {
                    emailNotSent.add(bu.getEmail());
                }
                TimeUnit.SECONDS.sleep(3);
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        });
        return emailNotSent;
    }

    private String getEmailContent(String gameId, String contentId, String playerId, String gameRoomLink) {
        return "Hi,<br />Hope you are having a good day. <br/>"
                + "Here are your Bingo slips. Kindly check attachment. <br/>"
                + "Please take a print out or write down the numbers of each ticket separately before the games starts.<br/><br/>"
                + "OR you can enter your unique player Id in game room to view your slips online.<br/>"
                + "<b>Uniques Player id : " + playerId + "</b><br /><br />"
                + "<a href=\"" + gameRoomLink + "\" target=\"_blank\">Open Game Room</a>"
                + "<br />"
                + "Enjoy !!! <br /><br />"
                + "Game id#" + gameId + " <br />"
                + "<img height='400' width='400' src=\"cid:" + contentId + "\" />"
                + "<br /><br />"
                + "Regards <br />~ &#129505; " + SENDER_NAME;
    }

    private String getEmailContentComakeIt(String gameId, String contentId, String playerId, String gameRoomLink) {
        return "Hi,<br />Hope you are having a good day. <br/>"
                + "Here are your Tambola slips. Kindly check attachment. <br/>"
                + "Please take a print out or write down the numbers of each ticket separately before the games starts.<br/><br/>"
                + "OR you can enter your unique player Id in game room to view your slips online and play.<br/>"
                + "<b>Uniques Player id : " + playerId + "</b><br /><br />"
                + "<a href=\"" + gameRoomLink + "\" target=\"_blank\">Open Game Room</a>"
                + "<br />"
                + "Enjoy !!! <br /><br />"
                + "Game id#" + gameId + " <br />"
                + "<img height='400' width='400' src=\"cid:" + contentId + "\" />"
                + "<br /><br />"
                + "Regards <br />~ Its all about Fridays & Tambola- &#129505; " + SENDER_NAME;
    }

    private String getEmailContentForLeader(String line1, String line2, String line3, String gameId) {
        return "Hi, Thanks for starting bingo game. <br />Hope u are having a good day. <br/>"
                + "Game id#" + gameId
                + " <br />"
                + line1 + "<br/>"
                + line2 + "<br/>"
                + line3 + "<br/>"
                + "<br/>Enjoy !!!"
                + "  <br /><br /><br/> "
                + "<br /><br />"
                + "Regards <br />~- &#129505; " + SENDER_NAME;
    }

}

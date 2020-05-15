package com.bingo.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.bingo.dao.BingoGame;
import com.bingo.dao.BingoSlip;
import com.bingo.dao.BingoSlipsTemplateData;
import com.bingo.dao.SlipHtmlResponse;
import com.bingo.utility.EmailService;
import com.bingo.utility.FileIOService;
import com.bingo.utility.SlipToPdfGeneratorService;
import com.itextpdf.text.DocumentException;


/**
 * @author Abhinav Gupta
 * @version 1.0
 * @since 12-May-2020
 */

@Controller
public class BingoRestController {

    private static final String GAME_IS_STARTED = "Game is started";
    private static final String BINGO_MULTIPLAYER = "Bingo Multiplayer";
    private static final String WELCOME_TO_BINGO_GAME = "Welcome to Bingo Game";
    BingoGame game = null;
    int pdfGenerated = -1;

    @Autowired
    private EmailService emailService;
    @Autowired
    private SlipToPdfGeneratorService slipToPdfGeneratorService;
    @Autowired
    private FileIOService fileIOService;

    @RequestMapping(method = RequestMethod.GET, path = "/")
    public ModelAndView homePage(Model model) {
        ModelAndView mav = createModelView("index");
        game = new BingoGame();
        System.out.println("game id: " + game.gameId);
        return mav;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/gamesetup")
    public ModelAndView generateBingoEmails(Model model) throws IOException {

        List<String> emails = fileIOService.readEmailsFromExcel("emails-bingo-users.xlsx");

        emails.forEach(e -> {
            if (game.bingoBoard.getUserSlips(e).isEmpty()) {
                game.bingoBoard.generateSlipsForUser(e);
            } else {
                System.out.println("Slips are already generated for user: " + e);
            }
        });

        System.out.println(game.calls);

        String bingoFolderName = fileIOService.createBingoGameFolder(game.gameId);

        game.bingoBoard.bingoUsers.stream().map(bu -> bu.email).forEach(e -> {
            fileIOService.createUserFolder(game.gameId, bingoFolderName, e);
        });

        fileIOService.writeCallsToCsv(bingoFolderName, game.calls);

        ModelAndView mav = createModelView("setup-game");
        mav.addObject("bingo_start_game", GAME_IS_STARTED);
        mav.addObject("bingo_game_id", game.gameId);
        mav.addObject("bingo_calls", game.calls);
        mav.addObject("pdfGenerated", pdfGenerated);

        System.out.println(emails);
        List<String> userEmails = game.bingoBoard.bingoUsers.stream().map(bu -> bu.email).collect(Collectors.toList());
        mav.addObject("bingo_user_emails", userEmails);

        List<String> pdfNotGenerated = new ArrayList<>();

        emails.forEach(email -> {
            String slipName = fileIOService.getUserSlipPdfName(game.gameId, email);
            List<BingoSlip> userSlips = game.bingoBoard.getUserSlips(email);
            List<SlipHtmlResponse> slipResponses = userSlips.stream()
                    .map(us -> new SlipHtmlResponse(us.slipId, us.bingoMatrix)).collect(Collectors.toList());
            try {
                slipToPdfGeneratorService.generateSlipPdf(slipName, email, game, slipResponses);
            } catch (Exception e) {
                System.out.println("Slip could not be generated for : " + email);
                pdfNotGenerated.add(email);
                // e.printStackTrace();
            }
        });

        if (pdfNotGenerated.isEmpty()) {
            System.out.println("Pdf generated for all successfully !!!, Game id : " + game.gameId);
            pdfGenerated = 5;
        } else {
            System.out.println("Pdf could not be geneatated for: ");
            System.out.println(pdfNotGenerated);
        }
        return mav;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/startBingo")
    public ModelAndView generateBingo(Model model)
            throws EncryptedDocumentException, InvalidFormatException, IOException, InterruptedException, ExecutionException {

        List<String> emails = game.bingoBoard.bingoUsers.stream().map(u -> u.email).collect(Collectors.toList());

        ModelAndView mav = createModelView("setup-game");
        mav.addObject("bingo_start_game", GAME_IS_STARTED);
        mav.addObject("bingo_game_id", game.gameId);
        mav.addObject("bingo_calls", game.calls);
        mav.addObject("bingo_user_emails", emails);

        List<String> emailNotSent = emailService.sendMailToParticipants(emails, game.gameId);

        if (!emailNotSent.isEmpty()) {
            System.out.println("Emails could not be sent to: ");
            System.out.println(emailNotSent);
        } else {
            System.out.println("All Emails have been sent successfully. Enjoy !!!!\n--- Game is Started ---");
        }
        return mav;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/callNextRandomNumber")
    public ModelAndView callRandomNumber(Model model) throws Exception {
        if (game.currentCall == -1) {
            game.currentCall = 0;
        }

        if (game.currentCall == 89) {
            game.currentCall = 0;
        }
        ModelAndView mav = createModelView("setup-game");
        mav.addObject("bingo_start_game", GAME_IS_STARTED);
        mav.addObject("bingo_game_id", game.gameId);

        mav.addObject("bingo_call_number", String.format("Call %d :", game.currentCall + 1));
        mav.addObject("bingo_call_value", game.calls.get(game.currentCall));

        List<Integer> doneCalls = new ArrayList<>();
        int b = 0;
        while (b <= game.currentCall) {
            doneCalls.add(game.calls.get(b));
            b++;
        }
        mav.addObject("bingo_done_calls", doneCalls);
        mav.addObject("bingo_calls", game.calls);

        List<String> emails = game.bingoBoard.bingoUsers.stream().map(u -> u.email).collect(Collectors.toList());
        mav.addObject("bingo_user_emails", emails);
        game.currentCall++;

        return mav;
    }

    @RequestMapping(value = "/slips/{userEmail}", method = RequestMethod.GET)
    public ModelAndView showUserSlips(Model model, @PathVariable("userEmail") String userEmail) throws DocumentException, IOException {

        ModelAndView mav = createModelView("slips");
        mav.addObject("bingo_user_emails", "emails");
        mav.addObject("bingo_user", userEmail);

        List<BingoSlip> userSlips = game.bingoBoard.getUserSlips(userEmail);
        List<SlipHtmlResponse> slipResponses = userSlips.stream()
                .map(us -> new SlipHtmlResponse(us.slipId, us.bingoMatrix)).collect(Collectors.toList());

        mav.addObject("bingoData", new BingoSlipsTemplateData(userEmail, game.gameId, slipResponses));

        return mav;
    }

    private ModelAndView createModelView(String name) {
        ModelAndView mav = new ModelAndView(name);
        mav.addObject("bingo_welcome_heading", WELCOME_TO_BINGO_GAME);
        mav.addObject("bingo_welcome_title", BINGO_MULTIPLAYER);
        return mav;
    }

}

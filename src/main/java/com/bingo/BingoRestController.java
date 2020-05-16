package com.bingo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bingo.appservice.BingoAppService;
import com.bingo.dao.BingoGame;
import com.bingo.dao.BingoSlip;
import com.bingo.dao.BingoSlipsTemplateData;
import com.bingo.dao.SlipHtmlResponse;
import com.bingo.utility.EmailService;
import com.itextpdf.text.DocumentException;


/**
 * @author Abhinav Gupta
 * @version 1.0
 * @since 12-May-2020
 */

@Controller
public class BingoRestController {

    private static final String GAME_IS_ON = "Game is On !!!";
    private static final String SETUP_GAME = "Setup your Game";
    private static final String BINGO_MULTIPLAYER = "Bingo Multiplayer";
    private static final String WELCOME_TO_BINGO_GAME = "Welcome to Bingo !!!";
    BingoGame game = null;
    int pdfGenerated = -1;

    private final String UPLOAD_DIR = "./";
    private static boolean isExcelUploaded = false;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BingoAppService bingoAppService;

    @RequestMapping(method = RequestMethod.GET, path = "/")
    public ModelAndView homePage(Model model) {

        ModelAndView mav = createModelView("index");
        game = bingoAppService.startGame();
        System.out.println("game id: " + game.gameId);
        System.out.println(game.calls);

        // Delete email excel from local memory
        File fileToDelete = new File("emails-bingo-users.xlsx");
        boolean success = fileToDelete.delete();
        isExcelUploaded = false;
        pdfGenerated = -1;
        return mav;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/gamesetup")
    public ModelAndView generateBingoEmails(Model model) {
        ModelAndView mav = createModelView("setup-game");

        System.out.println("Excel is present : " + isExcelUploaded);
        if (isExcelUploaded) {
            // read excel, import participants and generateSlipsForUser
            bingoAppService.generateSlipsForUser();

            bingoAppService.createBingoFolderStructure();

            List<String> userEmails = game.bingoBoard.bingoUsers.stream().map(bu -> bu.email).collect(Collectors.toList());
            List<String> pdfNotGenerated = bingoAppService.generateSlipPDFForUsers(userEmails);

            if (pdfNotGenerated.isEmpty()) {
                System.out.println("Pdf generated for all successfully !!!, Game id : " + game.gameId);
                pdfGenerated = 5;
            } else {
                System.out.println("Pdf could not be geneatated for: ");
                System.out.println(pdfNotGenerated);
            }

            mav.addObject("bingo_user_emails", userEmails);
        }

        mav.addObject("setup_game", SETUP_GAME);
        mav.addObject("bingo_game_id", game.gameId);
        mav.addObject("bingo_calls", game.calls);
        mav.addObject("pdfGenerated", pdfGenerated);
        mav.addObject("manage_players", !isExcelUploaded);

        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/gamesetup/uploadFile", method = RequestMethod.POST)
    public ModelAndView uploadFile(@RequestParam(required = false) MultipartFile file, RedirectAttributes redirectAttributes) throws IllegalStateException, IOException {
        if (file == null || file.isEmpty()) {
            // redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
            return new ModelAndView("redirect:" + "/gamesetup");
        }
        // normalize the file path
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        if (fileName.endsWith(".xlsx")) {

            // save the file on the local file system
            try {
                Path path = Paths.get(UPLOAD_DIR + BingoAppService.EMAILS_BINGO_USERS_XLSX);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                isExcelUploaded = true;
                System.out.println("Excel is read successfully and save in memory");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("FileType is not correct");
        }
        return new ModelAndView("redirect:" + "/gamesetup");
    }

    @RequestMapping(method = RequestMethod.GET, path = "/emailandstartbingo")
    public ModelAndView emailandstartbingo(Model model) {

        List<String> emails = game.bingoBoard.bingoUsers.stream().map(u -> u.email).collect(Collectors.toList());
        System.out.println("pdfGenerated " + pdfGenerated);
        ModelAndView mav = createModelView("setup-game");
        mav.addObject("setup_game", GAME_IS_ON);
        mav.addObject("bingo_game_id", game.gameId);
        mav.addObject("bingo_calls", game.calls);
        mav.addObject("bingo_user_emails", emails);
        mav.addObject("show_call_next_button", true);

        List<String> emailNotSent = emailService.sendMailToParticipants(emails, game.gameId);

        if (!emailNotSent.isEmpty()) {
            System.out.println("Emails could not be sent to: ");
            System.out.println(emailNotSent);
        } else {
            System.out.println("All Emails have been sent successfully. Enjoy !!!!\n--- Game is Started ---");
        }
        return mav;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/startbingo")
    public ModelAndView startbingo(Model model) {

        List<String> emails = game.bingoBoard.bingoUsers.stream().map(u -> u.email).collect(Collectors.toList());
        System.out.println("pdfGenerated " + pdfGenerated);
        ModelAndView mav = createModelView("setup-game");
        mav.addObject("setup_game", GAME_IS_ON);
        mav.addObject("bingo_game_id", game.gameId);
        mav.addObject("bingo_calls", game.calls);
        mav.addObject("bingo_user_emails", emails);
        mav.addObject("show_call_next_button", true);

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
        mav.addObject("setup_game", GAME_IS_ON);
        mav.addObject("bingo_game_id", game.gameId);

        mav.addObject("bingo_call_number", String.format("Call %d:", game.currentCall + 1));
        mav.addObject("bingo_call_value", game.calls.get(game.currentCall));
        mav.addObject("show_call_next_button", true);

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

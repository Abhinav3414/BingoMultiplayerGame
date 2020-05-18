package com.bingo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
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
import com.bingo.repository.BingoGameRepository;
import com.bingo.utility.EmailService;
import com.bingo.utility.FileIOService;


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

    private final String UPLOAD_DIR = "./";

    @Autowired
    private BingoGameRepository bingoGameRepository;

    @Autowired
    private FileIOService fileIOService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BingoAppService bingoAppService;

    @RequestMapping(method = RequestMethod.GET, path = "/")
    public ModelAndView homePage(Model model) {

        ModelAndView mav = createModelView("index");

        // Delete email excel from local memory
        File fileToDelete = new File("emails-bingo-users.xlsx");
        fileToDelete.delete();
        return mav;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, path = "/initiategame")
    public String initiategame(Model model) {

        BingoGame game = bingoAppService.startGame();
        System.out.println("game id: " + game.getGameId());
        System.out.println(game.getCalls());

        return game.getGameId();
    }

    @RequestMapping(method = RequestMethod.GET, path = "{gameId}/gamesetup")
    public ModelAndView generateBingoEmails(Model model, @PathVariable("gameId") String gameId) {

        ModelAndView mav = createModelView("setup-game");

        BingoGame bGame = bingoGameRepository.findById(gameId).get();

        System.out.println("Excel is present : " + bGame.isExcelUploaded());
        if (bGame.isExcelUploaded()) {
            // read excel, import participants and generateSlipsForUser
            bingoAppService.generateSlipsForUser(gameId);

            bingoAppService.createBingoFolderStructure(bGame);

            List<String> userEmails = bingoAppService.getBoardUsers(bGame);
            List<String> pdfNotGenerated = bingoAppService.generateSlipPDFForUsers(userEmails, bGame);

            if (pdfNotGenerated.isEmpty()) {
                System.out.println("Pdf generated for all successfully !!!, Game id : " + bGame.getGameId());
                bGame.setPdfsGenerated(true);
                bingoGameRepository.save(bGame);
            } else {
                System.out.println("Pdf could not be geneatated for: ");
                System.out.println(pdfNotGenerated);
            }

            mav.addObject("bingo_user_emails", userEmails);
        }

        mav.addObject("setup_game", SETUP_GAME);
        mav.addObject("bingo_game_id", bGame.getGameId());
        mav.addObject("bingo_calls", bGame.getCalls());
        mav.addObject("pdfGenerated", bGame.isPdfsGenerated());
        mav.addObject("manage_players", !bGame.isExcelUploaded());

        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "{gameId}/gamesetup/uploadFile", method = RequestMethod.POST)
    public ModelAndView uploadFile(@RequestParam(required = false) MultipartFile file, RedirectAttributes redirectAttributes,
            @PathVariable("gameId") String gameId) {

        BingoGame bGame = bingoGameRepository.findById(gameId).get();
        fileIOService.createBingoGameFolder(bGame.getGameId());

        if (file == null || file.isEmpty()) {
            // redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
            return new ModelAndView("redirect:" + gameId + "/gamesetup");
        }
        // normalize the file path
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        if (fileName.endsWith(".xlsx")) {

            // save the file on the local file system
            try {
                Path path = Paths.get(UPLOAD_DIR + '/' + fileIOService.getBingoFolder(gameId) + '/' + BingoAppService.EMAILS_BINGO_USERS_XLSX);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                bGame.setExcelUploaded(true);
                bingoGameRepository.save(bGame);
                System.out.println("Excel is read successfully and saved in memory");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("FileType is not correct");
        }
        return new ModelAndView("redirect:" + "/" + gameId + "/gamesetup");
    }

    @RequestMapping(value = "/download/{gameId}/{userEmail}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<FileSystemResource> getBingoUserSlipsForGame(@PathVariable("gameId") String gameId, @PathVariable("userEmail") String userEmail) throws IOException {
        String slipName = bingoAppService.getBingoUserSlipsForGame(gameId, userEmail);
        FileSystemResource file = new FileSystemResource(new File(slipName));

        return ResponseEntity
                .ok()
                .contentLength(file.contentLength())
                .contentType(
                        MediaType.parseMediaType("application/pdf"))
                .body(file);
    }

    @RequestMapping(method = RequestMethod.GET, path = "{gameId}/emailandstartbingo")
    public ModelAndView emailandstartbingo(Model model, @PathVariable("gameId") String gameId) {

        BingoGame bGame = bingoGameRepository.findById(gameId).get();

        List<String> emails = bingoAppService.getBoardUsers(bGame);
        System.out.println("pdfGenerated " + bGame.isPdfsGenerated());
        ModelAndView mav = createModelView("setup-game");
        mav.addObject("setup_game", GAME_IS_ON);
        mav.addObject("bingo_game_id", bGame.getGameId());
        mav.addObject("bingo_calls", bGame.getCalls());
        mav.addObject("bingo_user_emails", emails);
        mav.addObject("show_call_next_button", true);

        List<String> emailNotSent = emailService.sendMailToParticipants(emails, bGame.getGameId());

        if (!emailNotSent.isEmpty()) {
            System.out.println("Emails could not be sent to: ");
            System.out.println(emailNotSent);
        } else {
            System.out.println("All Emails have been sent successfully. Enjoy !!!!\n--- Game is Started ---");
        }
        return mav;
    }

    @RequestMapping(method = RequestMethod.GET, path = "{gameId}/startbingo")
    public ModelAndView startbingo(Model model, @PathVariable("gameId") String gameId) {
        BingoGame bGame = bingoGameRepository.findById(gameId).get();
        List<String> emails = bingoAppService.getBoardUsers(bGame);
        System.out.println("pdfGenerated " + bGame.isPdfsGenerated());
        ModelAndView mav = createModelView("setup-game");
        mav.addObject("setup_game", GAME_IS_ON);
        mav.addObject("bingo_game_id", bGame.getGameId());
        mav.addObject("bingo_calls", bGame.getCalls());
        mav.addObject("bingo_user_emails", emails);
        mav.addObject("show_call_next_button", true);

        return mav;
    }

    @RequestMapping(method = RequestMethod.GET, path = "{gameId}/callNextRandomNumber")
    public ModelAndView callRandomNumber(Model model, @PathVariable("gameId") String gameId) throws Exception {
        BingoGame bGame = bingoGameRepository.findById(gameId).get();

        if (bGame.getCurrentCall() == -1) {
            bGame.setCurrentCall(0);
        }

        if (bGame.getCurrentCall() == 89) {
            bGame.setCurrentCall(0);
        }
        ModelAndView mav = createModelView("setup-game");
        mav.addObject("setup_game", GAME_IS_ON);
        mav.addObject("bingo_game_id", bGame.getGameId());

        mav.addObject("bingo_call_number", String.format("Call %d:", bGame.getCurrentCall() + 1));
        mav.addObject("bingo_call_value", bGame.getCalls().get(bGame.getCurrentCall()));
        mav.addObject("show_call_next_button", true);

        List<Integer> doneCalls = new ArrayList<>();
        int b = 0;
        while (b <= bGame.getCurrentCall()) {
            doneCalls.add(bGame.getCalls().get(b));
            b++;
        }
        mav.addObject("bingo_done_calls", doneCalls);

        List<String> emails = bingoAppService.getBoardUsers(bGame);
        mav.addObject("bingo_user_emails", emails);

        int currentCall = bGame.getCurrentCall();
        bGame.setCurrentCall(++currentCall);
        bingoGameRepository.save(bGame);

        return mav;
    }

    @RequestMapping(value = "{gameId}/slips/{userEmail}", method = RequestMethod.GET)
    public ModelAndView showUserSlips(Model model, @PathVariable("userEmail") String userEmail, @PathVariable("gameId") String gameId) {

        ModelAndView mav = createModelView("slips");
        mav.addObject("bingo_user_emails", "emails");
        mav.addObject("bingo_user", userEmail);

        BingoGame bGame = bingoGameRepository.findById(gameId).get();
        List<BingoSlip> userSlips = bingoAppService.getUserSlips(userEmail, bGame);

        List<SlipHtmlResponse> slipResponses = userSlips.stream()
                .map(us -> new SlipHtmlResponse(us.getSlipId(), us.getBingoMatrix())).collect(Collectors.toList());

        mav.addObject("bingoData", new BingoSlipsTemplateData(userEmail, gameId, slipResponses));

        return mav;
    }

    @RequestMapping(value = "/sampleexcel", method = RequestMethod.GET)
    public ModelAndView showSampleExcel(Model model) {
        ModelAndView mav = new ModelAndView("sample-excel");
        return mav;
    }

    private ModelAndView createModelView(String name) {
        ModelAndView mav = new ModelAndView(name);
        mav.addObject("bingo_welcome_heading", WELCOME_TO_BINGO_GAME);
        mav.addObject("bingo_welcome_title", BINGO_MULTIPLAYER);
        return mav;
    }

}

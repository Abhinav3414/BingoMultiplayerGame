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
import com.bingo.repository.BingoSlipRepository;
import com.bingo.repository.BingoUserRepository;
import com.bingo.utility.EmailService;


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
    private BingoUserRepository bingoUserRepository;
    
    @Autowired
    private BingoGameRepository bingoGameRepository;

    @Autowired
    private BingoSlipRepository bingoSlipRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BingoAppService bingoAppService;

    @RequestMapping(method = RequestMethod.GET, path = "/")
    public ModelAndView homePage(Model model) {

        ModelAndView mav = createModelView("index");
        game = bingoAppService.startGame();
        System.out.println("game id: " + game.getGameId());
        System.out.println(game.getCalls());

        // Delete email excel from local memory
        File fileToDelete = new File("emails-bingo-users.xlsx");
        fileToDelete.delete();
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

            List<String> userEmails = bingoUserRepository.findByBoardId(game.getBingoBoardId()).stream().map(bu -> bu.getEmail()).collect(Collectors.toList());
            List<String> pdfNotGenerated = bingoAppService.generateSlipPDFForUsers(userEmails);

            if (pdfNotGenerated.isEmpty()) {
                System.out.println("Pdf generated for all successfully !!!, Game id : " + game.getGameId());
                pdfGenerated = 5;
            } else {
                System.out.println("Pdf could not be geneatated for: ");
                System.out.println(pdfNotGenerated);
            }

            mav.addObject("bingo_user_emails", userEmails);
        }

        mav.addObject("setup_game", SETUP_GAME);
        mav.addObject("bingo_game_id", game.getGameId());
        mav.addObject("bingo_calls", game.getCalls());
        mav.addObject("pdfGenerated", pdfGenerated);
        mav.addObject("manage_players", !isExcelUploaded);

        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/gamesetup/uploadFile", method = RequestMethod.POST)
    public ModelAndView uploadFile(@RequestParam(required = false) MultipartFile file, RedirectAttributes redirectAttributes) {
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
                System.out.println("Excel is read successfully and saved in memory");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("FileType is not correct");
        }
        return new ModelAndView("redirect:" + "/gamesetup");
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

    @RequestMapping(method = RequestMethod.GET, path = "/emailandstartbingo")
    public ModelAndView emailandstartbingo(Model model) {

        List<String> emails = bingoUserRepository.findByBoardId(game.getBingoBoardId()).stream().map(u -> u.getEmail()).collect(Collectors.toList());
        System.out.println("pdfGenerated " + pdfGenerated);
        ModelAndView mav = createModelView("setup-game");
        mav.addObject("setup_game", GAME_IS_ON);
        mav.addObject("bingo_game_id", game.getGameId());
        mav.addObject("bingo_calls", game.getCalls());
        mav.addObject("bingo_user_emails", emails);
        mav.addObject("show_call_next_button", true);

        List<String> emailNotSent = emailService.sendMailToParticipants(emails, game.getGameId());

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

        List<String> emails = bingoUserRepository.findByBoardId(game.getBingoBoardId()).stream().map(u -> u.getEmail()).collect(Collectors.toList());
        System.out.println("pdfGenerated " + pdfGenerated);
        ModelAndView mav = createModelView("setup-game");
        mav.addObject("setup_game", GAME_IS_ON);
        mav.addObject("bingo_game_id", game.getGameId());
        mav.addObject("bingo_calls", game.getCalls());
        mav.addObject("bingo_user_emails", emails);
        mav.addObject("show_call_next_button", true);

        return mav;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/callNextRandomNumber")
    public ModelAndView callRandomNumber(Model model) throws Exception {
        
        BingoGame bGame = bingoGameRepository.findById(game.getGameId()).get();
        
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

        List<String> emails = bingoUserRepository.findByBoardId(bGame.getBingoBoardId()).stream().map(u -> u.getEmail()).collect(Collectors.toList());
        mav.addObject("bingo_user_emails", emails);

        int currentCall = bGame.getCurrentCall();
        bGame.setCurrentCall(++currentCall);
        bingoGameRepository.save(bGame);

        return mav;
    }

    @RequestMapping(value = "/slips/{userEmail}", method = RequestMethod.GET)
    public ModelAndView showUserSlips(Model model, @PathVariable("userEmail") String userEmail) {

        ModelAndView mav = createModelView("slips");
        mav.addObject("bingo_user_emails", "emails");
        mav.addObject("bingo_user", userEmail);

        List<BingoSlip> userSlips = bingoSlipRepository.findByUserId(bingoUserRepository.findByEmail(userEmail).getUserId());
        List<SlipHtmlResponse> slipResponses = userSlips.stream()
                .map(us -> new SlipHtmlResponse(us.getSlipId(), us.getBingoMatrix())).collect(Collectors.toList());

        mav.addObject("bingoData", new BingoSlipsTemplateData(userEmail, game.getGameId(), slipResponses));

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

package com.bingo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.bingo.appservice.BingoAppService;
import com.bingo.dao.BingoGame;
import com.bingo.dao.BingoSlip;
import com.bingo.dao.BingoSlipsTemplateData;
import com.bingo.dao.PlayerResponse;
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

        return mav;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, path = "/initiategame")
    public ResponseEntity<Map<String, String>> initiategame(Model model) {

        // Delete email excel from local memory
        File fileToDelete = new File("emails-bingo-users.xlsx");
        fileToDelete.delete();

        BingoGame game = bingoAppService.startGame();
        System.out.println("Game id: " + game.getGameId());
        System.out.println(game.getCalls());

        return new ResponseEntity<>(Collections.singletonMap("gameId", game.getGameId()), HttpStatus.OK);
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
                .header("Content-Disposition", "attachment; filename=bingo_slips_" + gameId + ".pdf")
                .body(file);
    }

    @RequestMapping(method = RequestMethod.GET, path = "{gameId}/emailandstartbingo")
    public ModelAndView emailandstartbingo(Model model, @PathVariable("gameId") String gameId) {

        BingoGame bGame = bingoGameRepository.findById(gameId).get();

        List<String> emails = bingoAppService.getBoardUserEmails(bGame);
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
        List<String> emails = bingoAppService.getBoardUserEmails(bGame);
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

        List<String> emails = bingoAppService.getBoardUserEmails(bGame);
        mav.addObject("bingo_user_emails", emails);

        int currentCall = bGame.getCurrentCall();
        bGame.setCurrentCall(++currentCall);
        bingoGameRepository.save(bGame);

        return mav;
    }

    private ModelAndView createModelView(String name) {
        ModelAndView mav = new ModelAndView(name);
        mav.addObject("bingo_welcome_heading", WELCOME_TO_BINGO_GAME);
        mav.addObject("bingo_welcome_title", BINGO_MULTIPLAYER);
        return mav;
    }

    @RequestMapping(value = "/sampleexcel", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<byte[]> getSampleExcel(Model model) throws IOException {

        ClassPathResource imageFile = new ClassPathResource("static/excel-instructions-image.png");
        byte[] imageBytes = StreamUtils.copyToByteArray(imageFile.getInputStream());
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageBytes);
    }

    @ResponseBody
    @RequestMapping(value = "{gameId}/gamesetup/addPlayers", method = RequestMethod.POST)
    public ResponseEntity addPlayers(@PathVariable("gameId") String gameId, @RequestBody List<PlayerResponse> players) {

        BingoGame bGame = bingoGameRepository.findById(gameId).get();
        fileIOService.createBingoGameFolder(bGame.getGameId());

        bingoAppService.generateSlipsForUser(gameId, players);
        bingoAppService.createBingoFolderStructure(bGame);

        generatePdfs(bGame);

        return ResponseEntity.ok().build();
    }

    @ResponseBody
    @RequestMapping(value = "{gameId}/gamesetup/uploadExcelFile", method = RequestMethod.POST)
    public ResponseEntity uploadExcelFile(@RequestParam(required = false) MultipartFile file, @PathVariable("gameId") String gameId) {

        BingoGame bGame = bingoGameRepository.findById(gameId).get();
        fileIOService.createBingoGameFolder(bGame.getGameId());

        if (file == null || file.isEmpty()) {
            // redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
            return ResponseEntity.notFound().build();
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

        List<String> emails = fileIOService.readEmailsFromExcel(fileIOService.getBingoFolder(gameId) + File.separator + BingoAppService.EMAILS_BINGO_USERS_XLSX);

        // read excel, import participants and generateSlipsForUser

        List<PlayerResponse> players = emails.stream().map(e -> new PlayerResponse(null, null, e)).collect(Collectors.toList());
        bingoAppService.generateSlipsForUser(gameId, players);

        bingoAppService.createBingoFolderStructure(bGame);

        generatePdfs(bGame);

        return ResponseEntity.ok().build();
    }

    private void generatePdfs(BingoGame bGame) {
        List<String> userEmails = bingoAppService.getBoardUserEmails(bGame);
        List<String> pdfNotGenerated = bingoAppService.generateSlipPDFForUsers(userEmails, bGame);

        if (pdfNotGenerated.isEmpty()) {
            System.out.println("Pdf generated for all successfully !!!, Game id : " + bGame.getGameId());
            bGame.setPdfsGenerated(true);
            bingoGameRepository.save(bGame);
        } else {
            System.out.println("Pdf could not be geneatated for: ");
            System.out.println(pdfNotGenerated);
        }
    }

    @RequestMapping(method = RequestMethod.GET, path = "{gameId}/getBingoPlayers")
    public ResponseEntity<List<PlayerResponse>> getBingoPlayers(Model model, @PathVariable("gameId") String gameId) {
        List<PlayerResponse> boardUsers = bingoAppService.getBoardUsers(bingoGameRepository.findById(gameId).get()).stream()
                .map(p -> new PlayerResponse(p.getUserId(), p.getName(), p.getEmail())).collect(Collectors.toList());
        return new ResponseEntity<>(boardUsers, HttpStatus.OK);
    }

    @RequestMapping(value = "{gameId}/playerslips/{userEmail}", method = RequestMethod.GET)
    public ResponseEntity<BingoSlipsTemplateData> getUserSlips(Model model, @PathVariable("userEmail") String userEmail,
            @PathVariable("gameId") String gameId) {

        BingoGame bGame = bingoGameRepository.findById(gameId).get();
        List<BingoSlip> userSlips = bingoAppService.getUserSlips(userEmail, bGame);

        List<SlipHtmlResponse> slipResponses = userSlips.stream()
                .map(us -> new SlipHtmlResponse(us.getSlipId(), us.getBingoMatrix())).collect(Collectors.toList());

        return new ResponseEntity<>(new BingoSlipsTemplateData(userEmail, gameId, slipResponses), HttpStatus.OK);
    }

}

package com.bingo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bingo.appservice.BingoAppService;
import com.bingo.dao.BingoGame;
import com.bingo.dao.BingoSlip;
import com.bingo.dao.BingoSlipsTemplateData;
import com.bingo.dao.BingoUser;
import com.bingo.dao.BingoUserType;
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

@RestController
public class BingoRestController {

  private final String UPLOAD_DIR = "./";

  @Autowired
  private BingoGameRepository bingoGameRepository;

  @Autowired
  private FileIOService fileIOService;

  @Autowired
  private EmailService emailService;

  @Autowired
  private BingoAppService bingoAppService;

  @PostMapping("/initiategame")
  public ResponseEntity<Map<String, String>> initiategame() {

    // Delete email excel from local memory
    File fileToDelete = new File("emails-bingo-users.xlsx");
    fileToDelete.delete();

    BingoGame game = bingoAppService.startGame();
    System.out.println("Game id: " + game.getGameId());
    System.out.println(game.getCalls());

    return new ResponseEntity<>(Collections.singletonMap("gameId", game.getGameId()), HttpStatus.OK);
  }

  @PostMapping("{gameId}/assignLeader")
  public ResponseEntity<PlayerResponse> assignLeader(@PathVariable("gameId") String gameId, @RequestBody PlayerResponse leader) {

    BingoGame bGame = bingoGameRepository.findById(gameId).get();

    BingoUser bLeader = bingoAppService.setLeader(bGame.getGameId(), leader);

    if (bLeader != null) {
      bGame.setLeaderAssigned(true);
      bingoGameRepository.save(bGame);
      return new ResponseEntity<>(new PlayerResponse(bLeader.getUserId(), bLeader.getName(), bLeader.getEmail()), HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
  }

  @GetMapping("{gameId}/gameSetupStatus")
  public ResponseEntity<BingoGame> gameSetupStatus(@PathVariable("gameId") String gameId) {
    BingoGame bGame = bingoGameRepository.findById(gameId).get();
    bGame.setCalls(new ArrayList<Integer>());
    return new ResponseEntity<>(bGame, HttpStatus.OK);
  }

  @RequestMapping(value = "/download/{gameId}/{userEmail}", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<FileSystemResource> getBingoUserSlipsForGame(@PathVariable("gameId") String gameId,
      @PathVariable("userEmail") String userEmail, @RequestHeader("X-Requested-With") String leaderId) throws IOException {
    bingoAppService.validateGameAccess(gameId, leaderId);
    String slipName = bingoAppService.getBingoUserSlipsForGame(gameId, userEmail);
    FileSystemResource file = new FileSystemResource(new File(slipName));

    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Disposition", "attachment; filename=bingo_slips_" + gameId + ".pdf");
    headers.add("Access-Control-Expose-Headers", "Content-Disposition");

    return ResponseEntity
        .ok()
        .contentLength(file.contentLength())
        .contentType(
            MediaType.parseMediaType("application/pdf"))
        .headers(headers)
        .body(file);
  }

  // TODO work on email
  @RequestMapping(method = RequestMethod.GET, path = "{gameId}/emailandstartbingo")
  public void emailandstartbingo(@PathVariable("gameId") String gameId) {

    BingoGame bGame = bingoGameRepository.findById(gameId).get();

    List<String> emails = bingoAppService.getBoardUserEmails(bGame);
    System.out.println("pdfGenerated " + bGame.isPdfsGenerated());

    List<String> emailNotSent = emailService.sendMailToParticipants(emails, bGame.getGameId());

    if (!emailNotSent.isEmpty()) {
      System.out.println("Emails could not be sent to: ");
      System.out.println(emailNotSent);
    } else {
      System.out.println("All Emails have been sent successfully. Enjoy !!!!\n--- Game is Started ---");
    }
  }

  @RequestMapping(value = "{gameId}/callNext", method = RequestMethod.POST)
  public @ResponseBody ResponseEntity<Integer> callNext(@PathVariable("gameId") String gameId, @RequestHeader("X-Requested-With") String leaderId) {

    bingoAppService.validateGameAccess(gameId, leaderId);
    BingoGame bGame = bingoGameRepository.findById(gameId).get();

    if (bGame.getCurrentCall() == 89) {
      bGame.setCurrentCall(0);
    } else {
      int currentCall = bGame.getCurrentCall();

      if (bGame.getCurrentCall() == -1) {
        bGame.setHaveCallsStarted(true);
      }
      bGame.setCurrentCall(++currentCall);
    }
    bingoGameRepository.save(bGame);

    return new ResponseEntity<>(bGame.getCalls().get(bGame.getCurrentCall()), HttpStatus.OK);
  }

  @RequestMapping(value = "/sampleexcel", method = RequestMethod.GET)
  public @ResponseBody ResponseEntity<byte[]> getSampleExcel() throws IOException {

    ClassPathResource imageFile = new ClassPathResource("static/excel-instructions-image.png");
    byte[] imageBytes = StreamUtils.copyToByteArray(imageFile.getInputStream());
    return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageBytes);
  }

  @ResponseBody
  @RequestMapping(value = "{gameId}/gamesetup/addPlayers", method = RequestMethod.POST)
  public ResponseEntity<String> addPlayers(@PathVariable("gameId") String gameId, @RequestBody List<PlayerResponse> players,
      @RequestHeader("X-Requested-With") String leaderId) {

    bingoAppService.validateGameAccess(gameId, leaderId);
    BingoGame bGame = bingoGameRepository.findById(gameId).get();
    fileIOService.createBingoGameFolder(bGame.getGameId());

    bingoAppService.generateSlipsForUser(gameId, players);
    bingoAppService.createBingoFolderStructure(bGame);

    generatePdfs(bGame);

    bGame.setPdfsGenerated(true);
    bGame.setPlayerSetupComplete(true);

    bingoGameRepository.save(bGame);

    return ResponseEntity.ok().build();
  }

  @ResponseBody
  @RequestMapping(value = "{gameId}/gamesetup/uploadExcelFile", method = RequestMethod.POST)
  public ResponseEntity<String> uploadExcelFile(@RequestParam(required = false) MultipartFile file, @PathVariable("gameId") String gameId,
      @RequestHeader("X-Requested-With") String leaderId) {

    bingoAppService.validateGameAccess(gameId, leaderId);
    BingoGame bGame = bingoGameRepository.findById(gameId).get();
    fileIOService.createBingoGameFolder(bGame.getGameId());

    if (file == null || file.isEmpty()) {
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

    bGame.setPdfsGenerated(true);
    bGame.setPlayerSetupComplete(true);

    bingoGameRepository.save(bGame);

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

  public ResponseEntity<List<PlayerResponse>> getBingoPlayers(@PathVariable("gameId") String gameId, @RequestHeader("X-Requested-With") String leaderId) {

    bingoAppService.validateGameAccess(gameId, leaderId);

    List<PlayerResponse> boardUsers = bingoAppService.getBoardUsers(bingoGameRepository.findById(gameId).get()).stream()
        .filter(p -> !p.getUserType().equals(BingoUserType.ORGANIZER))
        .map(p -> new PlayerResponse(p.getUserId(), p.getName(), p.getEmail())).collect(Collectors.toList());
    return new ResponseEntity<>(boardUsers, HttpStatus.OK);
  }

  @RequestMapping(value = "{gameId}/playerslips/{playerId}", method = RequestMethod.GET)
  public ResponseEntity<BingoSlipsTemplateData> getUserSlips(@PathVariable("playerId") String playerId,
      @PathVariable("gameId") String gameId, @RequestHeader("X-Requested-With") String leaderId) {

    bingoAppService.validateGameAccess(gameId, leaderId);

    List<BingoSlip> userSlips = bingoAppService.getUserSlips(playerId);

    List<SlipHtmlResponse> slipResponses = userSlips.stream()
        .map(us -> new SlipHtmlResponse(us.getSlipId(), us.getBingoMatrix())).collect(Collectors.toList());

    return new ResponseEntity<>(new BingoSlipsTemplateData(bingoAppService.getPlayerEmail(playerId), gameId, slipResponses), HttpStatus.OK);
  }

  @RequestMapping(method = RequestMethod.GET, path = "{gameId}/getallcalls")
  public ResponseEntity<Map<Integer, Integer>> getAllCalls(@PathVariable("gameId") String gameId) {

    BingoGame bGame = bingoGameRepository.findById(gameId).get();

    List<Integer> calls = bGame.getCalls();

    Map<Integer, Integer> calledCallsMap = new HashMap<>();
    int currentCall = bGame.getCurrentCall();

    if (currentCall > -1) {
      for (int i = 0; i <= currentCall; i++) {
        calledCallsMap.put(i + 1, calls.get(i));
      }
    }
    return new ResponseEntity<>(calledCallsMap, HttpStatus.OK);
  }

}

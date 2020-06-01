package com.bingo;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
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
import com.bingo.dao.BingoBoardType;
import com.bingo.dao.BingoGame;
import com.bingo.dao.BingoSlipsTemplateData;
import com.bingo.dao.BingoUser;
import com.bingo.dao.PlayerResponse;
import com.bingo.repository.BingoGameRepository;


/**
 * @author Abhinav Gupta
 * @version 1.0
 * @since 12-May-2020
 */

@RestController
public class BingoRestController {

    @Autowired
    private BingoGameRepository bingoGameRepository;

    @Autowired
    private BingoAppService bingoAppService;

    @PostMapping("/initiategame")
    public ResponseEntity<Map<String, String>> initiategame() {

        // Delete email excel from local memory
        File fileToDelete = new File("emails-bingo-users.xlsx");
        fileToDelete.delete();

        BingoGame game = bingoAppService.startGame();
        return new ResponseEntity<>(Collections.singletonMap("gameId", game.getGameId()), HttpStatus.OK);
    }

    @PostMapping("{gameId}/assignLeader")
    public ResponseEntity<PlayerResponse> assignLeader(@PathVariable("gameId") String gameId,
            @RequestBody PlayerResponse leader) {

        BingoUser bLeader = bingoAppService.setLeader(gameId, leader);

        if (bLeader != null) {
            return new ResponseEntity<>(new PlayerResponse(bLeader.getUserId(), bLeader.getName(), bLeader.getEmail()),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("{gameId}/boardType/{boardType}/slipcount/{slips}")
    public ResponseEntity<PlayerResponse> setUpBoardTypeAndSlipCount(@PathVariable("gameId") String gameId,
            @PathVariable("boardType") BingoBoardType boardType, @PathVariable("slips") int slips) {

        bingoAppService.setUpBoardTypeAndSlipCount(gameId, boardType, slips);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("{gameId}/gameSetupStatus")
    public ResponseEntity<BingoGame> gameSetupStatus(@PathVariable("gameId") String gameId) {
        BingoGame bGame = bingoGameRepository.findById(gameId).get();
        return new ResponseEntity<>(bGame, HttpStatus.OK);
    }

    @RequestMapping(value = "/download/{gameId}/{userEmail}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<FileSystemResource> getBingoUserSlipsForGame(@PathVariable("gameId") String gameId,
            @PathVariable("userEmail") String userEmail, @RequestHeader("X-Requested-With") String leaderId)
            throws IOException {
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

    @PostMapping("{gameId}/sendEmail")
    public ResponseEntity<List<String>> emailandstartbingo(@PathVariable("gameId") String gameId,
            @RequestHeader("X-Requested-With") String leaderId) {

        bingoAppService.validateGameAccess(gameId, leaderId);

        List<String> emails = bingoAppService.sendEmail(gameId);

        BingoGame bGame = bingoGameRepository.findById(gameId).get();

        if (bGame.isHaveCallsStarted() == false) {
            bGame.setHaveCallsStarted(true);
            bingoGameRepository.save(bGame);
        }

        return new ResponseEntity<>(emails, HttpStatus.OK);
    }

    @RequestMapping(value = "{gameId}/callNext", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<Integer> callNext(@PathVariable("gameId") String gameId,
            @RequestHeader("X-Requested-With") String leaderId) {

        bingoAppService.validateGameAccess(gameId, leaderId);

        return new ResponseEntity<>(bingoAppService.callNext(gameId), HttpStatus.OK);
    }

    @RequestMapping(value = "/sampleexcel", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<byte[]> getSampleExcel() throws IOException {

        ClassPathResource imageFile = new ClassPathResource("excel-instructions-image.png");
        byte[] imageBytes = StreamUtils.copyToByteArray(imageFile.getInputStream());
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageBytes);
    }

    @ResponseBody
    @RequestMapping(value = "{gameId}/gamesetup/addPlayers", method = RequestMethod.POST)
    public ResponseEntity<String> addPlayers(@PathVariable("gameId") String gameId,
            @RequestBody List<PlayerResponse> players,
            @RequestHeader("X-Requested-With") String leaderId) {

        bingoAppService.validateGameAccess(gameId, leaderId);
        bingoAppService.addManualPlayers(gameId, players);

        return ResponseEntity.ok().build();
    }

    @ResponseBody
    @RequestMapping(value = "{gameId}/gamesetup/uploadExcelFile", method = RequestMethod.POST)
    public ResponseEntity<String> uploadExcelFile(@RequestParam(required = false) MultipartFile file,
            @PathVariable("gameId") String gameId,
            @RequestHeader("X-Requested-With") String leaderId) {

        bingoAppService.validateGameAccess(gameId, leaderId);

        if (file == null || file.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        bingoAppService.addPlayersFromExcel(file, gameId);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.GET, path = "{gameId}/getBingoPlayers")

    public ResponseEntity<List<PlayerResponse>> getBingoPlayers(@PathVariable("gameId") String gameId,
            @RequestHeader("X-Requested-With") String leaderId) {

        bingoAppService.validateGameAccess(gameId, leaderId);

        return new ResponseEntity<>(bingoAppService.getBingoBoardPlayers(gameId), HttpStatus.OK);
    }

    @RequestMapping(value = "{gameId}/playerslips/{playerId}", method = RequestMethod.GET)
    public ResponseEntity<BingoSlipsTemplateData> getUserSlips(@PathVariable("playerId") String playerId,
            @PathVariable("gameId") String gameId, @RequestHeader("X-Requested-With") String leaderId) {

        bingoAppService.validateGameAccess(gameId, leaderId);

        return new ResponseEntity<>(bingoAppService.getUserSlipsWrapper(gameId, playerId), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "{gameId}/getallcalls")
    public ResponseEntity<Map<Integer, Integer>> getAllCalls(@PathVariable("gameId") String gameId) {

        Map<Integer, Integer> allCallsMap = bingoAppService.getAllCalls(gameId);

        return new ResponseEntity<>(allCallsMap, HttpStatus.OK);
    }

}

package com.bingo;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
import com.bingo.dao.BingoSlip;
import com.bingo.dao.BingoSlipsTemplateData;
import com.bingo.dao.BingoUser;
import com.bingo.dao.GameSetupAttributesResponse;
import com.bingo.dao.PlayerResponse;
import com.bingo.dao.SlipHtmlResponse;
import com.bingo.dao.SlipInfoResponse;
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

    @PostMapping("/assignLeader")
    public ResponseEntity<Map<String, String>> assignLeader(@RequestBody PlayerResponse leader) {

        BingoGame game = bingoAppService.startGame();

        BingoUser bLeader = bingoAppService.setLeader(game.getGameId(), leader);

        if (bLeader != null) {
            new PlayerResponse(bLeader.getUserId(), bLeader.getName(), bLeader.getEmail(),
                    bLeader.getBingoSlipEmailStatus());

            Map<String, String> res = new HashMap<>();
            res.put("gameId", game.getGameId());
            res.put("leaderId", bLeader.getUserId());
            Collections.unmodifiableMap(res);
            return new ResponseEntity<>(res, HttpStatus.OK);

        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("{gameId}/joinPlayer")
    public ResponseEntity<PlayerResponse> joinPlayer(@PathVariable("gameId") String gameId,
            @RequestBody PlayerResponse player) {

        BingoUser bPlayer = bingoAppService.addPlayer(gameId, player);

        PlayerResponse playerResponse = new PlayerResponse(bPlayer.getUserId(), bPlayer.getName(),
                bPlayer.getEmail(), bPlayer.getBingoSlipEmailStatus());

        return new ResponseEntity<>(playerResponse, HttpStatus.OK);
    }

    @GetMapping("{gameId}/gameSetupStatus")
    public ResponseEntity<BingoGame> gameSetupStatus(@PathVariable("gameId") String gameId) {
        BingoGame bGame = bingoGameRepository.findById(gameId).orElse(null);
        return new ResponseEntity<>(bGame, HttpStatus.OK);
    }

    @PostMapping("{gameId}/setupGame")
    public ResponseEntity<Map<String, String>> setupGame(@PathVariable("gameId") String gameId,
            @RequestBody GameSetupAttributesResponse gameSetupAttributes,
            @RequestHeader("X-Requested-With") String leaderId) {

        bingoAppService.validateGameAccess(gameId, leaderId);

        bingoAppService.setUpGame(gameId, gameSetupAttributes.getBoardType(), gameSetupAttributes.getSlips(),
                gameSetupAttributes.isEmailSlips(), gameSetupAttributes.getGameName(),
                gameSetupAttributes.isJoinGameViaLink());

        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    @PostMapping("{gameId}/completePlayerSetup")
    public ResponseEntity<String> completePlayerSetup(@PathVariable("gameId") String gameId, @RequestHeader("X-Requested-With") String leaderId) {

        bingoAppService.validateGameAccess(gameId, leaderId);
        bingoAppService.completePlayerSetup(gameId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("{gameId}/startCalls")
    public ResponseEntity<PlayerResponse> startCalls(@PathVariable("gameId") String gameId,
            @RequestHeader("X-Requested-With") String leaderId) {

        bingoAppService.validateGameAccess(gameId, leaderId);
        BingoGame bGame = bingoGameRepository.findById(gameId).orElse(null);
        if (bGame != null) {
            bGame.setHaveCallsStarted(true);
            bingoGameRepository.save(bGame);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("{gameId}/callNext")
    public @ResponseBody ResponseEntity<Integer> callNext(@PathVariable("gameId") String gameId,
            @RequestHeader("X-Requested-With") String leaderId) {

        bingoAppService.validateGameAccess(gameId, leaderId);

        return new ResponseEntity<>(bingoAppService.callNext(gameId), HttpStatus.OK);
    }

    @GetMapping("{gameId}/getallcalls")
    public ResponseEntity<Map<Integer, Integer>> getAllCalls(@PathVariable("gameId") String gameId) {

        Map<Integer, Integer> allCallsMap = bingoAppService.getAllCalls(gameId);

        return new ResponseEntity<>(allCallsMap, HttpStatus.OK);
    }

    @PostMapping("{gameId}/entergameroom/{leaderEmail}")
    public ResponseEntity<PlayerResponse> enterGameRoom(@PathVariable("gameId") String gameId,
            @PathVariable("leaderEmail") String leaderEmail) {
        PlayerResponse res = null;
        try {
            res = bingoAppService.enterGameRoom(gameId, leaderEmail);
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(res, HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/download/{gameId}/{userId}")
    @ResponseBody
    public ResponseEntity<FileSystemResource> downloadBingoUserSlipsForGame(@PathVariable("gameId") String gameId,
            @PathVariable("userId") String userId, @RequestHeader("X-Requested-With") String leaderId)
            throws IOException {
        bingoAppService.validateGameAccess(gameId, leaderId);

        String slipName = bingoAppService.getBingoUserSlipsForGame(gameId, userId);
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

    @PostMapping("{gameId}/sendEmailToAll")
    public ResponseEntity<List<String>> sendEmailToAll(@PathVariable("gameId") String gameId,
            @RequestHeader("X-Requested-With") String leaderId, HttpServletRequest request) {

        bingoAppService.validateGameAccess(gameId, leaderId);

        List<String> emails = bingoAppService.sendEmailToAll(gameId, getApplicationContextUrl(gameId, request));

        BingoGame bGame = bingoGameRepository.findById(gameId).get();

        if (bGame.isHaveCallsStarted() == false) {
            bGame.setHaveCallsStarted(true);
            bingoGameRepository.save(bGame);
        }
        return new ResponseEntity<>(emails, HttpStatus.OK);
    }

    @PostMapping("{gameId}/sendEmail/{playerId}")
    public ResponseEntity<Boolean> sendEmail(@PathVariable("gameId") String gameId,
            @PathVariable("playerId") String playerId, @RequestHeader("X-Requested-With") String leaderId,
            HttpServletRequest request) {

        bingoAppService.validateGameAccess(gameId, leaderId);
        List<String> emailsNotSend =
                bingoAppService.sendEmail(gameId, playerId, getApplicationContextUrl(gameId, request));

        boolean isSentSuccess = emailsNotSend.isEmpty() ? true : false;

        return new ResponseEntity<>(isSentSuccess, HttpStatus.OK);
    }

    private String getApplicationContextUrl(String gameId, HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        StringBuilder contexturl = new StringBuilder(url.substring(0, url.indexOf("bingo-game") + 11));
        contexturl.append("#/gameroom/" + gameId);
        return contexturl.toString();
    }

    @RequestMapping(value = "/sampleexcel", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<byte[]> getSampleExcel() throws IOException {

        ClassPathResource imageFile = new ClassPathResource("excel-instructions-image.png");
        byte[] imageBytes = StreamUtils.copyToByteArray(imageFile.getInputStream());
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageBytes);
    }

    @ResponseBody
    @PostMapping("{gameId}/gamesetup/addPlayers")
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

        List<PlayerResponse> bingoBoardPlayers = bingoAppService.getBingoBoardPlayers(gameId);

        return new ResponseEntity<>(bingoBoardPlayers, HttpStatus.OK);
    }

    @RequestMapping(value = "{gameId}/playerslips/{playerId}", method = RequestMethod.GET)
    public ResponseEntity<BingoSlipsTemplateData> getUserSlips(@PathVariable("playerId") String playerId,
            @PathVariable("gameId") String gameId) {

        return new ResponseEntity<>(bingoAppService.getUserSlipsWrapper(gameId, playerId), HttpStatus.OK);
    }

    @PostMapping("{gameId}/updateSlip/{playerId}")
    public ResponseEntity<SlipHtmlResponse> updateSlipNumber(@PathVariable("gameId") String gameId,
            @PathVariable("playerId") String playerId, @RequestBody SlipInfoResponse slipInfo) {

        BingoSlip slipRes = bingoAppService.updateSlipNumber(gameId, playerId, slipInfo);

        SlipHtmlResponse res = new SlipHtmlResponse(slipRes.getSlipId(), slipRes.getBingoMatrix(),
                slipRes.getBingoSlipType().equals(BingoBoardType.GAMEBOARD_90));

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

}

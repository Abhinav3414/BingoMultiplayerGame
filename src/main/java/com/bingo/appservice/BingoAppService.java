package com.bingo.appservice;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.bingo.dao.BingoBoard;
import com.bingo.dao.BingoBoardType;
import com.bingo.dao.BingoGame;
import com.bingo.dao.BingoSlip;
import com.bingo.dao.BingoSlip75Type;
import com.bingo.dao.BingoSlip90Type;
import com.bingo.dao.BingoSlipsTemplateData;
import com.bingo.dao.BingoUser;
import com.bingo.dao.BingoUserType;
import com.bingo.dao.PlayerResponse;
import com.bingo.dao.SlipHtmlResponse;
import com.bingo.repository.BingoBoardRepository;
import com.bingo.repository.BingoGameRepository;
import com.bingo.repository.BingoSlipRepository;
import com.bingo.repository.BingoUserRepository;
import com.bingo.utility.EmailService;
import com.bingo.utility.FileIOService;
import com.bingo.utility.SlipToPdfGeneratorService;


/**
 * @author Abhinav Gupta
 * @version 1.0
 * @since 12-May-2020
 */

@Service
public class BingoAppService {

    public static final String EMAILS_BINGO_USERS_XLSX = "emails-bingo-users.xlsx";

    private final String UPLOAD_DIR = "./";

    @Autowired
    private BingoUserRepository bingoUserRepository;

    @Autowired
    private BingoGameRepository bingoGameRepository;

    @Autowired
    private BingoBoardRepository bingoBoardRepository;

    @Autowired
    private BingoSlipRepository bingoSlipRepository;

    @Autowired
    private FileIOService fileIOService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SlipToPdfGeneratorService slipToPdfGeneratorService;

    public BingoGame startGame() {
        BingoBoard bb = bingoBoardRepository.save(new BingoBoard(BingoBoardType.GAMEBOARD_90));
        BingoGame bGame = new BingoGame();
        bGame.setBingoBoardId(bb.getBoardId());

        BingoGame startedGame = bingoGameRepository.save(bGame);
        bb.setGameId(startedGame.getGameId());
        bingoBoardRepository.save(bb);

        System.out.println("Game id: " + startedGame.getGameId());
        return startedGame;
    }

    public void setUpBoardTypeAndSlipCount(String gameId, BingoBoardType boardType, int slips) {
        BingoBoard bBoard = bingoBoardRepository.findByGameId(gameId);
        bBoard.setBingoBoardType(boardType);
        bBoard.setSlipsPerUser(slips);
        bBoard.generateCallSequence();
        bingoBoardRepository.save(bBoard);

        BingoGame bGame = bingoGameRepository.findById(gameId).get();
        System.out.println(bBoard.getCalls());
        bGame.setBingoBoardReady(true);
        bingoGameRepository.save(bGame);
    }

    public Integer callNext(String gameId) {
        BingoGame bGame = bingoGameRepository.findById(gameId).get();
        BingoBoard bBoard = bingoBoardRepository.findById(bGame.getBingoBoardId()).get();

        if (bBoard.getBingoBoardType().equals(BingoBoardType.GAMEBOARD_90) && bBoard.getCurrentCall() == 89) {
            bBoard.setCurrentCall(0);
        } else if (bBoard.getBingoBoardType().equals(BingoBoardType.GAMEBOARD_75) && bBoard.getCurrentCall() == 74) {
            bBoard.setCurrentCall(0);
        } else {
            int currentCall = bBoard.getCurrentCall();

            if (bBoard.getCurrentCall() == -1) {
                bGame.setHaveCallsStarted(true);
                bingoGameRepository.save(bGame);
            }
            bBoard.setCurrentCall(++currentCall);
        }

        bingoBoardRepository.save(bBoard);
        return bBoard.getCalls().get(bBoard.getCurrentCall());
    }

    public void addManualPlayers(String gameId, List<PlayerResponse> players) {
        BingoGame bGame = bingoGameRepository.findById(gameId).get();
        fileIOService.createBingoGameFolder(bGame.getGameId());

        generateSlipsForUser(gameId, players);
        createBingoPlayerFolders(bGame);
        writeCallsToCSV(bGame);

        generatePdfs(bGame);

        bGame.setPdfsGenerated(true);
        bGame.setPlayerSetupComplete(true);

        bingoGameRepository.save(bGame);
    }

    public void addPlayersFromExcel(MultipartFile file, String gameId) {
        BingoGame bGame = bingoGameRepository.findById(gameId).get();
        fileIOService.createBingoGameFolder(bGame.getGameId());

        // normalize the file path
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        if (fileName.endsWith(".xlsx")) {

            // save the file on the local file system
            try {
                Path path = Paths.get(UPLOAD_DIR + '/' + fileIOService.getBingoFolder(gameId) + '/'
                        + BingoAppService.EMAILS_BINGO_USERS_XLSX);
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

        List<String> emails = fileIOService.readEmailsFromExcel(
                fileIOService.getBingoFolder(gameId) + File.separator + BingoAppService.EMAILS_BINGO_USERS_XLSX);

        // read excel, import participants and generateSlipsForUser

        List<PlayerResponse> players =
                emails.stream().map(e -> new PlayerResponse(null, null, e)).collect(Collectors.toList());

        generateSlipsForUser(gameId, players);
        createBingoPlayerFolders(bGame);
        writeCallsToCSV(bGame);
        generatePdfs(bGame);

        bGame.setPdfsGenerated(true);
        bGame.setPlayerSetupComplete(true);

        bingoGameRepository.save(bGame);
    }

    public void generatePdfs(BingoGame bGame) {
        List<String> userEmails = getBoardUserEmails(bGame);
        List<String> pdfNotGenerated = generateSlipPDFForUsers(userEmails, bGame);

        if (pdfNotGenerated.isEmpty()) {
            System.out.println("Pdf generated for all successfully !!!, Game id : " + bGame.getGameId());
            bGame.setPdfsGenerated(true);
            bingoGameRepository.save(bGame);
        } else {
            System.out.println("Pdf could not be geneatated for: ");
            System.out.println(pdfNotGenerated);
        }
    }

    public void generateSlipsForUser(String gameId, List<PlayerResponse> players) {

        List<String> userIds = new ArrayList<>();

        BingoGame bGame = bingoGameRepository.findById(gameId).get();

        players.forEach(p -> {
            BingoUser bUser = bingoUserRepository.findByEmailAndBoardIdLike(p.getEmail(), bGame.getBingoBoardId());
            if (bUser != null) {
                bingoUserRepository.delete(bUser);
            }
            BingoUser bu = bingoUserRepository.save(new BingoUser(p.getName(), p.getEmail(), bGame.getBingoBoardId()));
            userIds.add(bu.getUserId());
        });

        Optional<BingoBoard> bingoBoard = bingoBoardRepository.findById(bGame.getBingoBoardId());

        bingoBoard.get().setUserIds(userIds);

        bingoUserRepository.findByBoardId(bGame.getBingoBoardId()).forEach(u -> {
            generateSlipsForUser(u, bGame);
        });

        bingoBoardRepository.save(bingoBoard.get());
    }

    private List<BingoSlip> generateSlipsForUser(BingoUser bu, BingoGame bGame) {

        BingoBoard bingoBoard = bingoBoardRepository.findById(bGame.getBingoBoardId()).get();
        int slipsPerUser = bingoBoard.getSlipsPerUser();

        BingoBoardType bingoBoardType = bingoBoard.getBingoBoardType();

        if (bingoBoardType.equals(BingoBoardType.GAMEBOARD_90)) {
            for (int i = 0; i < slipsPerUser; i++) {
                BingoSlip bingoSlip = new BingoSlip90Type(bu.getUserId(), bGame.getBingoBoardId());
                bingoSlipRepository.save(bingoSlip);
            }
        }

        if (bingoBoardType.equals(BingoBoardType.GAMEBOARD_75)) {
            for (int i = 0; i < slipsPerUser; i++) {
                BingoSlip bingoSlip = new BingoSlip75Type(bu.getUserId(), bGame.getBingoBoardId());
                bingoSlipRepository.save(bingoSlip);
            }
        }

        return bingoSlipRepository.findByUserId(bu.getUserId());
    }

    public List<String> generateSlipPDFForUsers(List<String> emails, BingoGame game) {
        List<String> pdfNotGenerated = new ArrayList<>();

        boolean is90Game = bingoBoardRepository.findById(game.getBingoBoardId()).get().getBingoBoardType()
                .equals(BingoBoardType.GAMEBOARD_90);

        emails.forEach(email -> {
            String slipName = fileIOService.getUserSlipPdfName(game.getGameId(), email);
            List<BingoSlip> userSlips = bingoSlipRepository
                    .findByUserId(
                            bingoUserRepository.findByEmailAndBoardIdLike(email, game.getBingoBoardId()).getUserId());

            List<SlipHtmlResponse> slipResponses = userSlips.stream()
                    .map(us -> new SlipHtmlResponse(us.getSlipId(), us.getBingoMatrix(), is90Game))
                    .collect(Collectors.toList());
            try {
                slipToPdfGeneratorService.generateSlipPdf(slipName, email, game, slipResponses);
            } catch (Exception e) {
                System.out.println("Slip could not be generated for : " + email);
                pdfNotGenerated.add(email);
            }
        });
        return pdfNotGenerated;
    }

    public void createBingoPlayerFolders(BingoGame game) {
        String bingoFolderName = fileIOService.getBingoFolder(game.getGameId());

        bingoUserRepository.findByBoardId(game.getBingoBoardId())
                .stream()
                .filter(us -> !us.getUserType().equals(BingoUserType.ORGANIZER))
                .map(bu -> bu.getEmail())
                .forEach(e -> {
                    fileIOService.createUserFolder(game.getGameId(), bingoFolderName, e);
                });
    }

    private void writeCallsToCSV(BingoGame game) {
        String bingoFolderName = fileIOService.getBingoFolder(game.getGameId());
        List<Integer> calls = bingoBoardRepository.findByGameId(game.getGameId()).getCalls();
        fileIOService.writeCallsToCsv(bingoFolderName, calls);
    }

    public String getBingoUserSlipsForGame(String gameId, String email) {
        return fileIOService.getUserSlipPdfName(gameId, email);
    }

    public List<BingoSlip> getUserSlipsByEmail(String userEmail, BingoGame bGame) {
        return bingoSlipRepository
                .findByUserId(
                        bingoUserRepository.findByEmailAndBoardIdLike(userEmail, bGame.getBingoBoardId()).getUserId());
    }

    public List<BingoSlip> getUserSlips(String playerId) {
        return bingoSlipRepository.findByUserId(playerId);
    }

    public List<String> getBoardUserEmails(BingoGame bGame) {
        return bingoUserRepository.findByBoardId(bGame.getBingoBoardId())
                .stream()
                .filter(us -> !us.getUserType().equals(BingoUserType.ORGANIZER))
                .map(u -> u.getEmail())
                .collect(Collectors.toList());
    }

    public List<BingoUser> getBoardUsers(BingoGame bGame) {
        return bingoUserRepository.findByBoardId(bGame.getBingoBoardId()).stream().collect(Collectors.toList());
    }

    public BingoUser setLeader(String gameId, PlayerResponse organizer) {

        BingoGame bGame = bingoGameRepository.findById(gameId).get();

        BingoBoard bBoard = bingoBoardRepository.findByGameId(bGame.getGameId());
        BingoUser gameOrganizer = bingoUserRepository.save(new BingoUser(organizer.getName(), organizer.getEmail(),
                BingoUserType.ORGANIZER, bBoard.getBoardId()));
        bBoard.setLeaderId(gameOrganizer.getUserId());
        bingoBoardRepository.save(bBoard);

        bGame.setLeaderAssigned(true);
        bingoGameRepository.save(bGame);

        return gameOrganizer;
    }

    public String getPlayerEmail(String playerId) {
        return bingoUserRepository.findById(playerId).get().getEmail();
    }

    public void validateGameAccess(String gameId, String leaderId) {
        BingoBoard bBoard = bingoBoardRepository.findByGameId(gameId);
        if (!bBoard.getLeaderId().equals(leaderId)) {
            throw new IllegalAccessError("Not Authorized");
        }
    }

    public List<PlayerResponse> getBingoBoardPlayers(String gameId) {
        List<PlayerResponse> boardPlayers = getBoardUsers(bingoGameRepository.findById(gameId).get())
                .stream()
                .filter(p -> !p.getUserType().equals(BingoUserType.ORGANIZER))
                .map(p -> new PlayerResponse(p.getUserId(), p.getName(), p.getEmail())).collect(Collectors.toList());
        return boardPlayers;
    }

    public BingoSlipsTemplateData getUserSlipsWrapper(String gameId, String playerId) {

        List<BingoSlip> userSlips = getUserSlips(playerId);
        boolean is90Slip = userSlips.get(0).getBingoSlipType().equals(BingoBoardType.GAMEBOARD_90);

        List<SlipHtmlResponse> slipResponses = userSlips.stream()
                .map(us -> {
                    return new SlipHtmlResponse(us.getSlipId(), us.getBingoMatrix(), is90Slip);
                }).collect(Collectors.toList());

        return new BingoSlipsTemplateData(getPlayerEmail(playerId), gameId, slipResponses);
    }

    public Map<Integer, Integer> getAllCalls(String gameId) {
        BingoGame bGame = bingoGameRepository.findById(gameId).get();
        BingoBoard bBoard = bingoBoardRepository.findById(bGame.getBingoBoardId()).get();

        List<Integer> calls = bBoard.getCalls();

        Map<Integer, Integer> calledCallsMap = new HashMap<>();
        int currentCall = bBoard.getCurrentCall();

        if (currentCall > -1) {
            for (int i = 0; i <= currentCall; i++) {
                calledCallsMap.put(i + 1, calls.get(i));
            }
        }
        return calledCallsMap;
    }

    public List<String> sendEmail(String gameId) {
        BingoGame bGame = bingoGameRepository.findById(gameId).get();

        List<String> emails = getBoardUserEmails(bGame);
        System.out.println("pdfGenerated " + bGame.isPdfsGenerated());

        List<String> emailNotSent = emailService.sendMailToParticipants(emails, bGame.getGameId());

        if (!emailNotSent.isEmpty()) {
            System.out.println("Emails could not be sent to: ");
            System.out.println(emailNotSent);
        } else {
            System.out.println("All Emails have been sent successfully. Enjoy !!!!");
        }

        String line1 = "";
        String line2 = "";
        String line3 = "";

        StringBuilder sb = new StringBuilder();
        if (!emailNotSent.isEmpty()) {
            line1 = "Slips Email could not be sent to :";
            for (String email : emailNotSent) {
                sb.append(email).append(", ");
            }
            line2 = sb.toString();
            line3 = "please make sure these user get slips.";
        } else {
            line1 = "All Emails have been sent successfully.";
        }

        emailService.sendMailToLeader(getBingoLeaderEmail(bGame.getGameId()), "Bingo Game Status For Game Leader",
                bGame.getGameId(), line1, line2, line3);

        return emailNotSent;
    }

    public String getBingoLeaderEmail(String gameId) {
        String leaderId = bingoBoardRepository.findByGameId(gameId).getLeaderId();
        return bingoUserRepository.findById(leaderId).get().getEmail();
    }
}

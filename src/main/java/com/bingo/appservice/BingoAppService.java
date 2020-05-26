package com.bingo.appservice;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bingo.dao.BingoBoard;
import com.bingo.dao.BingoGame;
import com.bingo.dao.BingoSlip;
import com.bingo.dao.BingoUser;
import com.bingo.dao.SlipHtmlResponse;
import com.bingo.repository.BingoBoardRepository;
import com.bingo.repository.BingoGameRepository;
import com.bingo.repository.BingoSlipRepository;
import com.bingo.repository.BingoUserRepository;
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
    private SlipToPdfGeneratorService slipToPdfGeneratorService;

    public BingoGame startGame() {
        BingoBoard bb = bingoBoardRepository.save(new BingoBoard());
        BingoGame bGame = new BingoGame();
        bGame.setBingoBoardId(bb.getBoardId());
        return bingoGameRepository.save(bGame);
    }

    public List<String> generateSlipsForUser(String gameId) {

        List<String> emails = fileIOService.readEmailsFromExcel(fileIOService.getBingoFolder(gameId) + File.separator + EMAILS_BINGO_USERS_XLSX);
        List<String> userIds = new ArrayList<>();

        BingoGame bGame = bingoGameRepository.findById(gameId).get();

        emails.forEach(e -> {
            BingoUser bUser = bingoUserRepository.findByEmailAndBoardIdLike(e, bGame.getBingoBoardId());
            if (bUser != null) {
                bingoUserRepository.delete(bUser);
            }
            BingoUser bu = bingoUserRepository.save(new BingoUser(e, bGame.getBingoBoardId()));
            userIds.add(bu.getUserId());
        });

        Optional<BingoBoard> bingoBoard = bingoBoardRepository.findById(bGame.getBingoBoardId());

        bingoBoard.get().setUserIds(userIds);

        bingoUserRepository.findByBoardId(bGame.getBingoBoardId()).forEach(u -> {
            generateSlipsForUser(u, bGame);
        });

        // bingoSlipRepository.save(new BingoSlip());
        bingoBoardRepository.save(bingoBoard.get());
        return emails;
    }

    private List<BingoSlip> generateSlipsForUser(BingoUser bu, BingoGame bGame) {

        for (int i = 0; i < 6; i++) {
            BingoSlip bingoSlip = new BingoSlip(bu.getUserId(), bGame.getBingoBoardId());
            bingoSlipRepository.save(bingoSlip);
        }

        return bingoSlipRepository.findByUserId(bu.getUserId());
    }

    public List<String> generateSlipPDFForUsers(List<String> emails, BingoGame game) {
        List<String> pdfNotGenerated = new ArrayList<>();

        emails.forEach(email -> {
            String slipName = fileIOService.getUserSlipPdfName(game.getGameId(), email);
            List<BingoSlip> userSlips = bingoSlipRepository
                    .findByUserId(bingoUserRepository.findByEmailAndBoardIdLike(email, game.getBingoBoardId()).getUserId());

            List<SlipHtmlResponse> slipResponses = userSlips.stream()
                    .map(us -> new SlipHtmlResponse(us.getSlipId(), us.getBingoMatrix())).collect(Collectors.toList());
            try {
                slipToPdfGeneratorService.generateSlipPdf(slipName, email, game, slipResponses);
            } catch (Exception e) {
                System.out.println("Slip could not be generated for : " + email);
                pdfNotGenerated.add(email);
            }
        });
        return pdfNotGenerated;
    }

    public void createBingoFolderStructure(BingoGame game) {
        String bingoFolderName = fileIOService.getBingoFolder(game.getGameId());

        bingoUserRepository.findByBoardId(game.getBingoBoardId()).stream().map(bu -> bu.getEmail()).forEach(e -> {
            fileIOService.createUserFolder(game.getGameId(), bingoFolderName, e);
        });

        fileIOService.writeCallsToCsv(bingoFolderName, game.getCalls());
    }

    public String getBingoUserSlipsForGame(String gameId, String email) {
        return fileIOService.getUserSlipPdfName(gameId, email);
    }

    public List<BingoSlip> getUserSlips(String userEmail, BingoGame bGame) {
        return bingoSlipRepository
                .findByUserId(bingoUserRepository.findByEmailAndBoardIdLike(userEmail, bGame.getBingoBoardId()).getUserId());
    }

    public List<String> getBoardUserEmails(BingoGame bGame) {
        return bingoUserRepository.findByBoardId(bGame.getBingoBoardId()).stream().map(u -> u.getEmail()).collect(Collectors.toList());
    }

    public List<BingoUser> getBoardUsers(BingoGame bGame) {
        return bingoUserRepository.findByBoardId(bGame.getBingoBoardId()).stream().collect(Collectors.toList());
    }
}

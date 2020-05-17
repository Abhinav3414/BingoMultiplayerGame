package com.bingo.appservice;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bingo.dao.BingoGame;
import com.bingo.dao.BingoSlip;
import com.bingo.dao.BingoUser;
import com.bingo.dao.SlipHtmlResponse;
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

    BingoGame game = null;

    @Autowired
    private BingoUserRepository bin;
    @Autowired
    private FileIOService fileIOService;

    @Autowired
    private SlipToPdfGeneratorService slipToPdfGeneratorService;

    public BingoGame startGame() {
        game = new BingoGame();
        bin.save(new BingoUser("abhniav", "abc"));
        return game;
    }

    public List<String> generateSlipsForUser() {

        List<String> emails = fileIOService.readEmailsFromExcel(EMAILS_BINGO_USERS_XLSX);

        emails.forEach(e -> {
            if (game.bingoBoard.getUserSlips(e).isEmpty()) {
                game.bingoBoard.generateSlipsForUser(e);
            } else {
                System.out.println("Slips are already generated for user: " + e);
            }
        });
        return emails;
    }

    public List<String> generateSlipPDFForUsers(List<String> emails) {
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
            }
        });
        return pdfNotGenerated;
    }

    public void createBingoFolderStructure() {
        String bingoFolderName = fileIOService.createBingoGameFolder(game.gameId);

        game.bingoBoard.bingoUsers.stream().map(bu -> bu.getEmail()).forEach(e -> {
            fileIOService.createUserFolder(game.gameId, bingoFolderName, e);
        });

        fileIOService.writeCallsToCsv(bingoFolderName, game.calls);
    }
    
    public String getBingoUserSlipsForGame(String gameId, String email) {
        return fileIOService.getUserSlipPdfName(gameId, email);
    }

}

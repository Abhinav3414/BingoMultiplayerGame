package com.bingo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;


/**
 * @author Abhinav Gupta
 * @version 1.0
 * @since 12-May-2020
 */

@EnableMongoAuditing
@SpringBootApplication
public class BingoMultiplayerGameApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(BingoMultiplayerGameApplication.class, args);
    }
}

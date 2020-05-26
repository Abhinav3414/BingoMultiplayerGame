package com.bingo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


/**
 * @author Abhinav Gupta
 * @version 1.0
 * @since 12-May-2020
 */
@SpringBootApplication
public class BingoMultiplayerGameApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(BingoMultiplayerGameApplication.class, args);
    }
}

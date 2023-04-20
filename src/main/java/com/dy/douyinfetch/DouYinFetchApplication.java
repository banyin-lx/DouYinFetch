package com.dy.douyinfetch;

import com.dy.douyinfetch.service.FetchDouYin;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.URISyntaxException;

@SpringBootApplication
public class DouYinFetchApplication {

    public static void main(String[] args) throws IOException, URISyntaxException {
        SpringApplication.run(DouYinFetchApplication.class, args);
        String roomUrl = "https://live.douyin.com/22401792684";
        FetchDouYin.startListening(roomUrl);
    }

}

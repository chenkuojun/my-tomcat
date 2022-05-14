package com.chenkuojun.mytomcatuseexample;

import com.chenkuojun.mytomcat.init.config.EnableMyTomcatsStarter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableMyTomcatsStarter
public class MyTomcatUseExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyTomcatUseExampleApplication.class, args);
    }

}

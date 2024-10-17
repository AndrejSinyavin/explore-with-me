package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StatsServiceApplication {
    public static final String dateTimePattern = "yyyy-MM-dd HH:mm:ss";

    public static void main(String[] args) {
        SpringApplication.run(StatsServiceApplication.class, args);
    }

}

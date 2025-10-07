package com.example.timelock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TimeLockedReleaseApplication {
  public static void main(String[] args) {
    SpringApplication.run(TimeLockedReleaseApplication.class, args);
  }
}

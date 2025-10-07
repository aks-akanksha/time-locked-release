package com.example.timelock;

import org.springframework.boot.SpringApplication;

import com.example.timelock.TimeLockedReleaseApplication;

public class TestTimeLockedReleaseApplication {

	public static void main(String[] args) {
		SpringApplication.from(TimeLockedReleaseApplication::main)
		                 .with(TestcontainersConfiguration.class)
		                 .run(args);
	}
}

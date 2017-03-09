package com.ericksonjoseph.assignment;

import com.ericksonjoseph.assignment.service.UserService;
import com.ericksonjoseph.assignment.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ericksonjoseph.assignment.fs.StorageProperties;
import com.ericksonjoseph.assignment.fs.StorageService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

	@SpringBootApplication
	@EnableConfigurationProperties(StorageProperties.class)
	public class Application implements CommandLineRunner {

		@Autowired
		private UserService userService;

		public static void main(String[] args) {
			SpringApplication.run(Application.class, args);
		}

		@Bean
		CommandLineRunner init(StorageService storageService) {
			// Clean up
			return (args) -> {
				storageService.deleteAll();
				storageService.init();
			};
		}

		@Override
		public void run(String... strings) throws Exception {
			//Persist default user
			userService.createUser(Config.get("app.login.username"), Config.get("app.login.password"));
		}
	}

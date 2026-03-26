package com.carrefourconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import de.codecentric.boot.admin.server.config.EnableAdminServer;

@EnableAdminServer
@SpringBootApplication
public class CarrefourconnectApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarrefourconnectApplication.class, args);
	}

}

package com.example.HolidayManager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class HolidayManagerApplication {

	public static void main(String[] args) {

		ConfigurableApplicationContext context = SpringApplication.run(HolidayManagerApplication.class, args);
	}

}

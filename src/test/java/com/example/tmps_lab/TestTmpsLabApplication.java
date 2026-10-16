package com.example.tmps_lab;

import org.springframework.boot.SpringApplication;

public class TestTmpsLabApplication {

	public static void main(String[] args) {
		SpringApplication.from(TmpsLabApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

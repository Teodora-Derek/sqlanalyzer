package com.sofiauni.sqlanalyzer;

import com.sofiauni.sqlanalyzer.demo.DemoApplication;
import org.springframework.boot.SpringApplication;

public class TestSqlanalyzerApplication {

	public static void main(String[] args) {
		SpringApplication.from(DemoApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

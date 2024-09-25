package com.example.demo;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.ebean.annotation.Platform;
import io.ebean.dbmigration.DbMigration;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
		DbMigration dbMigration = DbMigration.create();
		dbMigration.setPlatform(Platform.SQLITE);

		try {
			dbMigration.generateMigration();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

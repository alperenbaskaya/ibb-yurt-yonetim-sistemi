package com.ibb.yurtlar;

import com.ibb.yurtlar.config.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(FileStorageProperties.class)
public class YurtlarApplication {

	public static void main(String[] args) {
		SpringApplication.run(YurtlarApplication.class, args);
	}
}
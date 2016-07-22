package org.gfcyb.arp.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ArpServer {

	public static void main(String[] args) {
		System.out.println("-------- Start-up message in server console ---------");
		SpringApplication.run(ArpServer.class, args);
		System.out.println("-------- Application lauched ---------");
	}
}

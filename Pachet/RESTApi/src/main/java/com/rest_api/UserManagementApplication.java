package com.rest_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <ul>
 * 	<li>Clasa principala a API-ului.</li>
 * 	<li> Va fi adnotata cu <strong>@SpringBootApplication</strong> pentru a informa <strong>Spring</strong> framework.</li>
 * </ul>
 */
@SpringBootApplication
public class UserManagementApplication {
	/**
	 * Functia main care va porni aplicatia prin intermediul <strong>SpringBoot</strong>
	 */
	public static void main(String[] args) {
		SpringApplication.run(UserManagementApplication.class, args);
	}

}

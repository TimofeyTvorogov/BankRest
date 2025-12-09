package com.example.bankcards;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.Role.RoleName;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
public class TtApplication {
	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	CardRepository cardRepository;

	public static void main(String[] args) {
		SpringApplication.run(TtApplication.class, args);
	}

	@Bean
	CommandLineRunner clr() {

		return args -> {
			var role_user = new Role(RoleName.ROLE_USER);
			var role_admin = new Role(RoleName.ROLE_ADMIN);
			roleRepository.save(role_user);
			roleRepository.save(role_admin);

			var privelleged = List.of(role_user,role_admin);
			var common = List.of(role_user);

			var user1 = new User(null,common,"Yuri","12345");
			var user2 = new User(null,common,"Max","9876");
			var admin = new User(List.of(),privelleged,"Admin","$2a$12$6Im.lr46qotQ5iKlHU6S8eW5H.gEWD171k0.NuSHKFX6ivuzXL8aS");

			var yuriCard1 = new Card("12345",user1, LocalDate.of(2028,10,1), Card.CardStatus.ACTIVE,1000d);
			var yuriCard2 = new Card("45678",user1, LocalDate.now(), Card.CardStatus.EXPIRED,0d);
			user1.setCards(List.of(yuriCard1,yuriCard2));

			var ludoCard2 = new Card("19877",user2, LocalDate.now(), Card.CardStatus.BLOCKED,0d);
			user2.setCards(List.of(ludoCard2));
			userRepository.saveAll(List.of(user1,user2,admin));
			cardRepository.saveAll(List.of(yuriCard1,yuriCard2,ludoCard2));
		};
	}

}

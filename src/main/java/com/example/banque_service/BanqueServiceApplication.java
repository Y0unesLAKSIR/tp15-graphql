package com.example.banque_service;

import com.example.banque_service.entities.Compte;
import com.example.banque_service.entities.Transaction;
import com.example.banque_service.entities.TypeCompte;
import com.example.banque_service.entities.TypeTransaction;
import com.example.banque_service.repositories.CompteRepository;
import com.example.banque_service.repositories.TransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@SpringBootApplication
public class BanqueServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BanqueServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner initData(CompteRepository compteRepository, TransactionRepository transactionRepository) {
		return args -> {
			if (compteRepository.count() == 0) {
				LocalDate baseDate = LocalDate.of(2024, 11, 18);
				Date creationDate = Date.from(baseDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
				List<Compte> comptes = List.of(
					new Compte(null, 0, creationDate, TypeCompte.EPARGNE),
					new Compte(null, 0, creationDate, TypeCompte.COURANT),
					new Compte(null, 0, creationDate, TypeCompte.EPARGNE)
				);
				compteRepository.saveAll(comptes);

				Transaction t1 = new Transaction(null, 8271.796491312965, creationDate, TypeTransaction.DEPOT, comptes.get(0));
				Transaction t2 = new Transaction(null, 3672.765137493308, creationDate, TypeTransaction.DEPOT, comptes.get(1));
				Transaction t3 = new Transaction(null, 422.171034829543, creationDate, TypeTransaction.DEPOT, comptes.get(2));
				transactionRepository.saveAll(List.of(t1, t2, t3));

				comptes.get(0).setSolde(t1.getMontant());
				comptes.get(1).setSolde(t2.getMontant());
				comptes.get(2).setSolde(t3.getMontant());
				compteRepository.saveAll(comptes);
			}
		};
	}

}

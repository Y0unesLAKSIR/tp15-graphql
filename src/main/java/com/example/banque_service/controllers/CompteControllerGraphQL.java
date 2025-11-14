package com.example.banque_service.controllers;

import com.example.banque_service.dto.TransactionRequest;
import com.example.banque_service.entities.Compte;
import com.example.banque_service.entities.Transaction;
import com.example.banque_service.entities.TypeTransaction;
import com.example.banque_service.repositories.CompteRepository;
import com.example.banque_service.repositories.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@AllArgsConstructor
public class CompteControllerGraphQL {
    private final CompteRepository compteRepository;
    private final TransactionRepository transactionRepository;

    private static final DateTimeFormatter TRANSACTION_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @QueryMapping
    public List<Compte> allComptes() {
        return compteRepository.findAll();
    }

    @QueryMapping
    public Compte compteById(@Argument Long id) {
        Compte compte = compteRepository.findById(id).orElse(null);
        if (compte == null) throw new RuntimeException(String.format("Compte %s not found", id));
        else return compte;
    }

    @MutationMapping
    public Compte saveCompte(@Argument Compte compte) {
        return compteRepository.save(compte);
    }

    @MutationMapping
    @Transactional
    public Transaction addTransaction(@Argument("transaction") TransactionRequest transactionRequest) {
        Compte compte = compteRepository.findById(transactionRequest.getCompteId())
                .orElseThrow(() -> new RuntimeException("Compte not found"));

        Transaction transaction = new Transaction();
        transaction.setMontant(transactionRequest.getMontant());
        transaction.setDate(resolveTransactionDate(transactionRequest.getDate()));
        transaction.setType(transactionRequest.getType());
        transaction.setCompte(compte);

        transactionRepository.save(transaction);
        refreshCompteSolde(compte);

        return transaction;
    }

    @QueryMapping
    public List<Transaction> compteTransactions(@Argument Long id) {
        Compte compte = compteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compte not found"));
        return transactionRepository.findByCompte(compte);
    }

    @QueryMapping
    public List<Transaction> allTransactions() {
        return transactionRepository.findAll();
    }

    @QueryMapping
    public Map<String, Object> totalSolde() {
        long count = compteRepository.count();
        double sum = compteRepository.sumSoldes();
        double average = count > 0 ? sum / count : 0;

        return Map.of(
                "count", count,
                "sum", sum,
                "average", average
        );
    }

    @QueryMapping
    public Map<String, Object> transactionStats() {
        long count = transactionRepository.count();
        double sumDepots = defaultZero(transactionRepository.sumByType(TypeTransaction.DEPOT));
        double sumRetraits = defaultZero(transactionRepository.sumByType(TypeTransaction.RETRAIT));
        return Map.of(
                "count", count,
                "sumDepots", sumDepots,
                "sumRetraits", sumRetraits
        );
    }

    private Date resolveTransactionDate(String date) {
        if (date == null || date.isBlank()) {
            return new Date();
        }
        try {
            LocalDate localDate = LocalDate.parse(date, TRANSACTION_DATE_FORMATTER);
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException ex) {
            throw new RuntimeException("Invalid date format. Expected yyyy/MM/dd", ex);
        }
    }

    private void refreshCompteSolde(Compte compte) {
        double depots = defaultZero(transactionRepository.sumByCompteAndType(compte, TypeTransaction.DEPOT));
        double retraits = defaultZero(transactionRepository.sumByCompteAndType(compte, TypeTransaction.RETRAIT));
        compte.setSolde(depots - retraits);
        compteRepository.save(compte);
    }

    private double defaultZero(Double value) {
        return value == null ? 0 : value;
    }
}
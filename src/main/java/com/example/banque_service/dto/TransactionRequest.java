package com.example.banque_service.dto;

import com.example.banque_service.entities.TypeTransaction;
import lombok.Data;

@Data
public class TransactionRequest {
    private Long compteId;
    private double montant;
    private String date;
    private TypeTransaction type;
}

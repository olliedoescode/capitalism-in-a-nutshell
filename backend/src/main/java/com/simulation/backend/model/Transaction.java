package com.simulation.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long senderCompanyId;

    @Column(nullable = false)
    private Long receiverCompanyId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 32)
    private String transactionType;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    protected Transaction() {
    }

    public Transaction(
            Long senderCompanyId,
            Long receiverCompanyId,
            BigDecimal amount,
            String transactionType,
            LocalDateTime timestamp) {
        this.senderCompanyId = senderCompanyId;
        this.receiverCompanyId = receiverCompanyId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public Long getSenderCompanyId() {
        return senderCompanyId;
    }

    public Long getReceiverCompanyId() {
        return receiverCompanyId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

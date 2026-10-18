package com.simulation.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 120)
    private String name;

    @Column(nullable = false)
    private boolean isPlayer;

    @Column(length = 120)
    private String specialty;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal cashBalance = BigDecimal.ZERO;

    protected Company() {
    }

    public Company(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }

    public Company(String name, BigDecimal cashBalance) {
        this.name = name;
        this.cashBalance = cashBalance;
    }

    public Company(
            String name,
            boolean isPlayer,
            BigDecimal cashBalance,
            String specialty) {
        this.name = name;
        this.isPlayer = isPlayer;
        this.cashBalance = cashBalance;
        this.specialty = specialty;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPlayer() {
        return isPlayer;
    }

    public void setPlayer(boolean player) {
        isPlayer = player;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }
}

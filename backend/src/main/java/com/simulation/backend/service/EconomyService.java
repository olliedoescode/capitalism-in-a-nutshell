package com.simulation.backend.service;

import com.simulation.backend.model.Company;
import com.simulation.backend.model.Transaction;
import com.simulation.backend.repository.CompanyRepository;
import com.simulation.backend.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EconomyService {

    private final CompanyRepository companyRepository;
    private final TransactionRepository transactionRepository;

    public EconomyService(
            CompanyRepository companyRepository,
            TransactionRepository transactionRepository) {
        this.companyRepository = companyRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public boolean executeTransfer(
            Long senderId,
            Long receiverId,
            BigDecimal amount,
            String transactionType) {
        validateTransferInput(senderId, receiverId, amount, transactionType);

        Company sender;
        Company receiver;
        if (senderId < receiverId) {
            sender = findCompanyForUpdate(senderId, "Sender");
            receiver = findCompanyForUpdate(receiverId, "Receiver");
        } else {
            receiver = findCompanyForUpdate(receiverId, "Receiver");
            sender = findCompanyForUpdate(senderId, "Sender");
        }

        if (sender.getCashBalance().compareTo(amount) < 0) {
            return false;
        }

        sender.setCashBalance(sender.getCashBalance().subtract(amount));
        receiver.setCashBalance(receiver.getCashBalance().add(amount));
        companyRepository.save(sender);
        companyRepository.save(receiver);

        transactionRepository.save(new Transaction(
                senderId,
                receiverId,
                amount,
                transactionType,
                LocalDateTime.now()));

        return true;
    }

    private Company findCompanyForUpdate(Long companyId, String role) {
        return companyRepository.findByIdForUpdate(companyId)
                .orElseThrow(() -> new EntityNotFoundException(
                        role + " company not found: " + companyId));
    }

    private void validateTransferInput(
            Long senderId,
            Long receiverId,
            BigDecimal amount,
            String transactionType) {
        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException("Sender and receiver IDs are required");
        }
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Sender and receiver must be different");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        if (transactionType == null || transactionType.isBlank()) {
            throw new IllegalArgumentException("Transaction type is required");
        }
    }
}

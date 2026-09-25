package com.prj.beehouse.service;

import com.prj.beehouse.entity.Category;
import com.prj.beehouse.entity.Instalment;
import com.prj.beehouse.entity.Transaction;
import com.prj.beehouse.entity.User;
import com.prj.beehouse.exception.BadRequestException;
import com.prj.beehouse.exception.ForbiddenException;
import com.prj.beehouse.exception.ResourceNotFoundException;
import com.prj.beehouse.payload.request.AddTransactionRequest;
import com.prj.beehouse.payload.response.TransactionResponse;
import com.prj.beehouse.repository.InstalmentRepository;
import com.prj.beehouse.repository.TransactionRepository;
import com.prj.beehouse.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final UserService userService;
    private final InstalmentRepository instalmentRepository;

    @Transactional
    public String addTransaction(AddTransactionRequest request, CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        userService.ensureUserEnabled(user);
        ensureTransactionDateInCurrentInstalment(request.getDate());

        Transaction transaction = Transaction.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .amount(request.getAmount())
                .transactionType(request.getTransactionType())
                .date(request.getDate())
                .user(user)
                .build();

        if (request.getCategory() != null && !request.getCategory().isEmpty()) {
            Category category = categoryService.findCategoryByCategoryName(request.getCategory());
            transaction.setCategory(category);
        } else {
            transaction.setCategory(null);
        }

        Integer year = transaction.getDate().getYear();
        Integer month = transaction.getDate().getMonthValue();

        Instalment instalment = findInstalmentByYearAndMonth(year, month);
        BigDecimal currentAmount = instalment.getCurrentBalance();

        switch (transaction.getTransactionType()) {
            case ADD -> currentAmount = currentAmount.add(transaction.getAmount());
            case REDUCE -> currentAmount = currentAmount.subtract(transaction.getAmount());
        }

        instalment.setCurrentBalance(currentAmount);

        transactionRepository.save(transaction);
        instalmentRepository.save(instalment);

        return "Transaction added successfully";
    }

    @Transactional
    public String removeTransaction(Integer transactionId, CustomUserDetails userDetails) {
        userService.ensureUserEnabled(userDetails.getUser());
        Transaction transaction = findTransactionById(transactionId);
        if (transaction.getUser() == null || transaction.getUser().getId() != userDetails.getUser().getId())
            throw new ForbiddenException("Transaction does not belong to authenticated user");

        Integer year = transaction.getDate().getYear();
        Integer month = transaction.getDate().getMonthValue();

        Instalment instalment = findInstalmentByYearAndMonth(year, month);
        BigDecimal currentAmount = instalment.getCurrentBalance();

        switch (transaction.getTransactionType()) {
            case ADD -> currentAmount = currentAmount.subtract(transaction.getAmount());
            case REDUCE -> currentAmount = currentAmount.add(transaction.getAmount());
        }

        instalment.setCurrentBalance(currentAmount);

        transactionRepository.delete(transaction);
        instalmentRepository.save(instalment);

        return "Transaction deleted successfully";
    }


    // All transactions for a month/year.
    public Page<TransactionResponse> getTransactionsByMonthAndYear(int year, int month, int pageNumber, int pageSize, String sortBy, String direction) {
        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.Direction.valueOf(direction.toUpperCase()),
                sortBy);
        return transactionRepository.getTransactionsByMonthAndYear(year, month, pageable)
                .map(TransactionResponse::toResponse);
    }

    // All transactions for a user in a month/year.
    public Page<TransactionResponse> getTransactionsByUserAndMonth(String username, int year, int month, int pageNumber, int pageSize, String sortBy, String direction) {
        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.Direction.valueOf(direction.toUpperCase()),
                sortBy);
        User user = userService.findUserByUsername(username);
        return transactionRepository.getTransactionsByUserAndMonth(user.getId(), year, month, pageable)
                .map(TransactionResponse::toResponse);
    }

    // All transactions for the current month.
    public Page<TransactionResponse> getTransactionsForCurrentMonth(int pageNumber, int pageSize, String sortBy, String direction) {
        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.Direction.valueOf(direction.toUpperCase()),
                sortBy);
        LocalDate now = LocalDate.now();
        return transactionRepository.getTransactionsByMonthAndYear(now.getYear(), now.getMonthValue(), pageable)
                .map(TransactionResponse::toResponse);
    }

    protected Transaction findTransactionById(Integer id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
    }

    private Instalment findInstalmentByYearAndMonth(Integer year, Integer month) {
        return instalmentRepository.findByYearAndMonth(year, month)
                .orElseThrow(() -> new ResourceNotFoundException("Instalment", "year/month", year + "/" + month));
    }

    private void ensureTransactionDateInCurrentInstalment(LocalDate transactionDate) {
        LocalDate now = LocalDate.now();
        if (transactionDate.getYear() != now.getYear() || transactionDate.getMonthValue() != now.getMonthValue())
            throw new BadRequestException("Transaction can be registered only in the current instalment");
    }
}

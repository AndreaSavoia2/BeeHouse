package com.prj.beehouse.service;

import com.prj.beehouse.entity.Instalment;
import com.prj.beehouse.repository.InstalmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstalmentScheduler {

    private final InstalmentRepository instalmentRepository;
    private final InstalmentTemplateService instalmentTemplateService;

    /**
     * Runs on the first day of every month at 00:01.
     */
    @Scheduled(cron = "0 1 0 1 * ?") // First day of every month at 00:01.
    public void createMonthlyBudget() {
        BigDecimal cost = instalmentTemplateService.getCost();

        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        if (instalmentRepository.existsByYearAndMonth(year, month)) {
            return;
        }

        Instalment instalment = Instalment.builder()
                .year(year)
                .month(month)
                .initialAmount(cost)
                .currentBalance(cost)
                .instalmentTemplate(instalmentTemplateService.getDefaultTemplate())
                .build();

        instalmentRepository.save(instalment);

        log.info("Created new budget for {}/{} with initial amount {}", month, year, cost);
    }
}

package com.prj.beehouse.service;

import com.prj.beehouse.entity.Instalment;
import com.prj.beehouse.exception.ResourceNotFoundException;
import com.prj.beehouse.payload.response.InstalmentPeriodResponse;
import com.prj.beehouse.payload.response.InstalmentResponse;
import com.prj.beehouse.repository.InstalmentRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InstalmentService {

    private final InstalmentRepository instalmentRepository;

    public InstalmentResponse getCurrentAmount(Integer month, Integer year) {
        return InstalmentResponse.toResponse(findByYearAndMonth(year, month));
    }

    public InstalmentResponse getCurrentAmountForCurrentMonth() {
        Integer year = LocalDate.now().getYear();
        Integer month = LocalDate.now().getMonthValue();
        return getCurrentAmount(month, year);
    }

    public List<InstalmentPeriodResponse> getInstalmentPeriods() {
        return instalmentRepository.findAllByOrderByYearDescMonthDesc()
                .stream()
                .map(InstalmentPeriodResponse::toResponse)
                .toList();
    }

    protected Instalment findByYearAndMonth(Integer year, Integer month) {
        return instalmentRepository.findByYearAndMonth(year, month)
                .orElseThrow(() -> new ResourceNotFoundException("Instalment", "year/month", year + "/" + month));
    }
}

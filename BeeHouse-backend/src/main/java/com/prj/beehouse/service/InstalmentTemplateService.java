package com.prj.beehouse.service;

import com.prj.beehouse.entity.InstalmentTemplate;
import com.prj.beehouse.exception.ResourceNotFoundException;
import com.prj.beehouse.repository.InstalmentTemplateRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class InstalmentTemplateService {

    private final InstalmentTemplateRepository instalmentTemplateRepository;

    protected BigDecimal getCost() {
        return getDefaultTemplate().getCost();
    }

    @Transactional
    public String updateCost(BigDecimal cost) {
        InstalmentTemplate instalmentTemplate = getDefaultTemplate();
        instalmentTemplate.setCost(cost);
        instalmentTemplateRepository.save(instalmentTemplate);
        return "cost updated";
    }

    protected InstalmentTemplate getDefaultTemplate() {
        return instalmentTemplateRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException("InstalmentTemplate", "default", "missing"));
    }
}

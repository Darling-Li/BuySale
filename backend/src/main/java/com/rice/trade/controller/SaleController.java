package com.rice.trade.controller;

import com.rice.trade.dto.CreateSaleRequest;
import com.rice.trade.dto.SaleResponse;
import com.rice.trade.dto.UpdateSettlementRequest;
import com.rice.trade.service.SaleService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping
    public List<SaleResponse> search(
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Boolean settled,
            @RequestParam(required = false) String keyword
    ) {
        return saleService.search(productType, warehouseId, settled, keyword);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse create(@Valid @RequestBody CreateSaleRequest request) {
        return saleService.create(request);
    }

    @PatchMapping("/{id}/settlement")
    public SaleResponse updateSettlement(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSettlementRequest request
    ) {
        return saleService.updateSettlement(id, request);
    }
}

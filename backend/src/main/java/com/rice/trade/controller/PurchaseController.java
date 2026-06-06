package com.rice.trade.controller;

import com.rice.trade.dto.CreatePurchaseRequest;
import com.rice.trade.dto.PurchaseResponse;
import com.rice.trade.service.PurchaseService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping
    public List<PurchaseResponse> search(
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String keyword
    ) {
        return purchaseService.search(productType, warehouseId, keyword);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseResponse create(@Valid @RequestBody CreatePurchaseRequest request) {
        return purchaseService.create(request);
    }
}

package com.rice.trade.controller;

import com.rice.trade.dto.InventoryResponse;
import com.rice.trade.enums.ProductType;
import com.rice.trade.service.InventoryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<InventoryResponse> search(
            @RequestParam(required = false) ProductType productType,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String keyword
    ) {
        return inventoryService.search(productType, warehouseId, keyword);
    }
}


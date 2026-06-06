package com.rice.trade.controller;

import com.rice.trade.dto.ProductUnitRequest;
import com.rice.trade.dto.ProductUnitResponse;
import com.rice.trade.service.ProductUnitService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product-units")
public class ProductUnitController {

    private final ProductUnitService productUnitService;

    public ProductUnitController(ProductUnitService productUnitService) {
        this.productUnitService = productUnitService;
    }

    @GetMapping
    public List<ProductUnitResponse> list() {
        return productUnitService.listAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductUnitResponse create(@Valid @RequestBody ProductUnitRequest request) {
        return productUnitService.create(request);
    }

    @PutMapping("/{id}")
    public ProductUnitResponse update(@PathVariable Long id, @Valid @RequestBody ProductUnitRequest request) {
        return productUnitService.update(id, request);
    }
}

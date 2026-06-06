package com.rice.trade.controller;

import com.rice.trade.dto.ProductCategoryRequest;
import com.rice.trade.dto.ProductCategoryResponse;
import com.rice.trade.service.ProductCategoryService;
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
@RequestMapping("/api/product-categories")
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    public ProductCategoryController(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    @GetMapping
    public List<ProductCategoryResponse> list() {
        return productCategoryService.listAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductCategoryResponse create(@Valid @RequestBody ProductCategoryRequest request) {
        return productCategoryService.create(request);
    }

    @PutMapping("/{id}")
    public ProductCategoryResponse update(@PathVariable Long id, @Valid @RequestBody ProductCategoryRequest request) {
        return productCategoryService.update(id, request);
    }
}

package com.rice.trade.controller;

import com.rice.trade.dto.ProductTypeOption;
import com.rice.trade.dto.ProductUnitResponse;
import com.rice.trade.service.ProductCategoryService;
import com.rice.trade.service.ProductUnitService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reference")
public class ReferenceController {

    private final ProductCategoryService productCategoryService;
    private final ProductUnitService productUnitService;

    public ReferenceController(ProductCategoryService productCategoryService, ProductUnitService productUnitService) {
        this.productCategoryService = productCategoryService;
        this.productUnitService = productUnitService;
    }

    @GetMapping("/product-types")
    public List<ProductTypeOption> productTypes() {
        return productCategoryService.enabledOptions();
    }

    @GetMapping("/units")
    public List<ProductUnitResponse> units() {
        return productUnitService.listEnabled();
    }
}

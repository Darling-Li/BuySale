package com.rice.trade.controller;

import com.rice.trade.dto.ProductTypeOption;
import com.rice.trade.enums.ProductType;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reference")
public class ReferenceController {

    @GetMapping("/product-types")
    public List<ProductTypeOption> productTypes() {
        return Arrays.stream(ProductType.values())
                .map(type -> new ProductTypeOption(type.name(), type.getLabel()))
                .toList();
    }
}


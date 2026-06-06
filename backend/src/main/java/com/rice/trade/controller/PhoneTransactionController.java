package com.rice.trade.controller;

import com.rice.trade.dto.PhoneTransactionResponse;
import com.rice.trade.service.PhoneTransactionService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/phone-transactions")
public class PhoneTransactionController {

    private final PhoneTransactionService phoneTransactionService;

    public PhoneTransactionController(PhoneTransactionService phoneTransactionService) {
        this.phoneTransactionService = phoneTransactionService;
    }

    @GetMapping
    public List<PhoneTransactionResponse> search(@RequestParam String phone) {
        return phoneTransactionService.search(phone);
    }
}

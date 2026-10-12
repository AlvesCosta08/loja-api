package com.alvescosta.loja.controller;

import com.alvescosta.loja.dto.CouponRequest;
import com.alvescosta.loja.dto.CouponResponse;
import com.alvescosta.loja.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public CouponResponse create(@Valid @RequestBody CouponRequest request) {
        return couponService.create(request);
    }

    @GetMapping("/{code}")
    public CouponResponse findByCode(@PathVariable String code) {
        return couponService.findByCode(code);
    }
}

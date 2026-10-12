package com.alvescosta.loja.service;

import com.alvescosta.loja.domain.Order;
import com.alvescosta.loja.domain.Product;
import com.alvescosta.loja.domain.Role;
import com.alvescosta.loja.domain.User;
import com.alvescosta.loja.dto.*;
import com.alvescosta.loja.exception.BusinessException;
import com.alvescosta.loja.exception.NotFoundException;
import com.alvescosta.loja.repository.OrderRepository;
import com.alvescosta.loja.repository.ProductRepository;
import com.alvescosta.loja.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        long total = orderRepository.countPaid();
        BigDecimal revenue = orderRepository.sumPaidTotal();

        BigDecimal avg = (total == 0)
                ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);

        return new DashboardResponse(total, revenue, avg);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> listAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> listUsersWithStats() {
        return userRepository.findAll().stream()
                .map(u -> new AdminUserResponse(
                        u.getId(),
                        u.getEmail(),
                        u.getName(),
                        u.getRole(),
                        orderRepository.countPaidByUserId(u.getId()),
                        orderRepository.sumPaidTotalByUserId(u.getId())
                ))
                .toList();
    }

    @Transactional
    public UserResponse updateUserRole(Long targetUserId, Long requesterId, Role newRole) {
        if (targetUserId.equals(requesterId)) {
            throw new BusinessException("Nao e permitido alterar a propria role");
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado"));

        target.setRole(newRole);
        userRepository.save(target);
        return UserResponse.from(target);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> lowStock(int threshold) {
        return productRepository.findByStockLessThanEqualOrderByStockAsc(threshold)
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional
    public ProductResponse restock(Long productId, int quantity) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Produto nao encontrado"));

        p.setStock(p.getStock() + quantity);
        productRepository.save(p);
        return ProductResponse.from(p);
    }
}

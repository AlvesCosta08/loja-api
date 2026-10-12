package com.alvescosta.loja.repository;

import com.alvescosta.loja.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'PAID'")
    long countPaid();

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.status = 'PAID'")
    BigDecimal sumPaidTotal();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.status = 'PAID'")
    long countPaidByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.user.id = :userId AND o.status = 'PAID'")
    BigDecimal sumPaidTotalByUserId(Long userId);
}

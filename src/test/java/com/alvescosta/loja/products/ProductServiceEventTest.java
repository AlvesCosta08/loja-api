package com.alvescosta.loja.products;

import com.alvescosta.loja.assistant.indexer.ProductEvent;
import com.alvescosta.loja.domain.Product;
import com.alvescosta.loja.dto.ProductRequest;
import com.alvescosta.loja.repository.ProductRepository;
import com.alvescosta.loja.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceEventTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProductService productService;

    @Test
    void deve_publicar_evento_ao_criar_produto() {
        // Arrange
        var request = new ProductRequest("Camiseta", BigDecimal.TEN, 10, "Roupas", "url");
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(1L); // Simula o ID gerado pelo banco
            return p;
        });

        // Act
        productService.create(request);

        // Assert
        ArgumentCaptor<ProductEvent> eventCaptor = ArgumentCaptor.forClass(ProductEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        ProductEvent evento = eventCaptor.getValue();
        assertThat(evento.id()).isEqualTo(1L);
        assertThat(evento.name()).isEqualTo("Camiseta");
        assertThat(evento.active()).isTrue();
    }

    @Test
    void deve_publicar_evento_ao_deletar_produto() {
        // Arrange
        Product produto = new Product();
        produto.setId(1L);
        produto.setName("Camiseta");
        produto.setCategory("Roupas");
        when(productRepository.findById(1L)).thenReturn(Optional.of(produto));

        // Act
        productService.delete(1L);

        // Assert
        ArgumentCaptor<ProductEvent> eventCaptor = ArgumentCaptor.forClass(ProductEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        ProductEvent evento = eventCaptor.getValue();
        assertThat(evento.id()).isEqualTo(1L);
        assertThat(evento.active()).isFalse(); // Flag indica que é uma deleção
    }
}
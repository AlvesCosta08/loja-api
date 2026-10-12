package com.alvescosta.loja.service;

import com.alvescosta.loja.domain.Product;
import com.alvescosta.loja.dto.ProductRequest;
import com.alvescosta.loja.dto.ProductResponse;
import com.alvescosta.loja.exception.NotFoundException;
import com.alvescosta.loja.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @CacheEvict(value = {"products", "productLists"}, allEntries = true)
    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product p = Product.builder()
                .name(request.name())
                .price(request.price())
                .stock(request.stock())
                .category(request.category())
                .imageUrl(request.imageUrl())
                .build();
        return ProductResponse.from(productRepository.save(p));
    }

    @Cacheable(value = "products", key = "#id")
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return ProductResponse.from(getOrThrow(id));
    }

    @Cacheable(value = "productLists",
            key = "#category + '|' + #search + '|' + #pageable.pageNumber + '|' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<ProductResponse> list(String category, String search, Pageable pageable) {
        boolean hasCategory = category != null && !category.isBlank();
        boolean hasSearch = search != null && !search.isBlank();

        Page<Product> page;
        if (hasCategory && hasSearch) {
            page = productRepository.findByCategoryIgnoreCaseAndNameContainingIgnoreCase(
                    category, search, pageable);
        } else if (hasCategory) {
            page = productRepository.findByCategoryIgnoreCase(category, pageable);
        } else if (hasSearch) {
            page = productRepository.findByNameContainingIgnoreCase(search, pageable);
        } else {
            page = productRepository.findAll(pageable);
        }

        return page.map(ProductResponse::from);
    }

    @CacheEvict(value = {"products", "productLists"}, allEntries = true)
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product p = getOrThrow(id);
        p.setName(request.name());
        p.setPrice(request.price());
        p.setStock(request.stock());
        p.setCategory(request.category());
        p.setImageUrl(request.imageUrl());
        return ProductResponse.from(productRepository.save(p));
    }

    @CacheEvict(value = {"products", "productLists"}, allEntries = true)
    @Transactional
    public void delete(Long id) {
        Product p = getOrThrow(id);
        productRepository.delete(p);
    }

    private Product getOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Produto nao encontrado: " + id));
    }
}

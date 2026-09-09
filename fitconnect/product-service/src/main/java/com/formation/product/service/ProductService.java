package com.formation.product.service;

import com.formation.product.dto.ProductRequest;
import com.formation.product.dto.ProductResponse;
import com.formation.product.exception.ProductNotFoundException;
import com.formation.product.model.Product;
import com.formation.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(ProductMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return ProductMapper.toResponse(getProductOrThrow(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product saved = productRepository.save(ProductMapper.toEntity(request));
        return ProductMapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getProductOrThrow(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        return ProductMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    private Product getProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}

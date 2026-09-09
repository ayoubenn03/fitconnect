package com.formation.product.service;

import com.formation.product.dto.ProductRequest;
import com.formation.product.dto.ProductResponse;
import com.formation.product.exception.ProductNotFoundException;
import com.formation.product.model.Product;
import com.formation.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de ProductService, avec ProductRepository mocke (aucune base reelle sollicitee).
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product existingProduct;

    @BeforeEach
    void setUp() {
        existingProduct = new Product("Clavier", "Clavier standard", new BigDecimal("49.90"), 10);
        existingProduct.setId(1L);
    }

    @Test
    void findAll_retourneTousLesProduitsMappesEnResponse() {
        when(productRepository.findAll()).thenReturn(List.of(existingProduct));

        List<ProductResponse> result = productService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Clavier");
    }

    @Test
    void findById_produitExistant_retourneLeProduit() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));

        ProductResponse result = productService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Clavier");
    }

    @Test
    void findById_produitInexistant_leveProductNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_sauvegardeEtRetourneLeProduitCree() {
        ProductRequest request = new ProductRequest("Souris", "Souris optique", new BigDecimal("19.90"), 25);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(2L);
            return p;
        });

        ProductResponse result = productService.create(request);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("Souris");
        assertThat(result.getQuantity()).isEqualTo(25);
    }

    @Test
    void update_produitExistant_metAJourLesChamps() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductRequest request = new ProductRequest("Clavier Pro", "Retroeclaire", new BigDecimal("89.90"), 5);
        ProductResponse result = productService.update(1L, request);

        assertThat(result.getName()).isEqualTo("Clavier Pro");
        assertThat(result.getPrice()).isEqualByComparingTo("89.90");
        assertThat(result.getQuantity()).isEqualTo(5);
    }

    @Test
    void update_produitInexistant_leveProductNotFoundException() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());
        ProductRequest request = new ProductRequest("X", "Y", BigDecimal.ONE, 1);

        assertThatThrownBy(() -> productService.update(404L, request))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void delete_produitExistant_supprime() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.delete(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_produitInexistant_leveProductNotFoundException() {
        when(productRepository.existsById(404L)).thenReturn(false);

        assertThatThrownBy(() -> productService.delete(404L))
                .isInstanceOf(ProductNotFoundException.class);
    }
}

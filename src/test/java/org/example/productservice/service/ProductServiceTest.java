package org.example.productservice.service;

import org.example.productservice.dto.ProductRequest;
import org.example.productservice.dto.ProductResponse;
import org.example.productservice.model.Product;
import org.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    // -------------------------------------------------------------------------
    // create
    // -------------------------------------------------------------------------

    @Test
    void create_shouldReturnCreatedProduct() {
        ProductRequest request = buildRequest("White T-Shirt", "29.99", 10, Product.Category.CAMISETAS);

        Product saved = buildProduct(UUID.randomUUID(), "White T-Shirt", "29.99", 10, Product.Category.CAMISETAS, true);
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductResponse response = productService.create(request);

        assertThat(response.getName()).isEqualTo("White T-Shirt");
        assertThat(response.getPrice()).isEqualByComparingTo("29.99");
        assertThat(response.getStock()).isEqualTo(10);
        assertThat(response.getActive()).isTrue();
        verify(productRepository, times(1)).save(any(Product.class));
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Test
    void findById_shouldReturnProduct_whenExists() {
        UUID id = UUID.randomUUID();
        Product product = buildProduct(id, "Red Dress", "59.99", 5, Product.Category.VESTIDOS, true);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        ProductResponse response = productService.findById(id);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getName()).isEqualTo("Red Dress");
    }

    @Test
    void findById_shouldThrowException_whenProductNotFound() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(id))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void findById_shouldThrowException_whenProductIsInactive() {
        UUID id = UUID.randomUUID();
        Product inactive = buildProduct(id, "Old Jacket", "99.99", 3, Product.Category.ABRIGOS, false);
        when(productRepository.findById(id)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> productService.findById(id))
                .isInstanceOf(ProductNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    @Test
    void findAll_shouldReturnOnlyActiveProducts() {
        List<Product> activeProducts = List.of(
                buildProduct(UUID.randomUUID(), "Jeans", "49.99", 20, Product.Category.PANTALONES, true),
                buildProduct(UUID.randomUUID(), "Sneakers", "89.99", 15, Product.Category.CALZADO, true)
        );
        when(productRepository.findByActiveTrue()).thenReturn(activeProducts);

        List<ProductResponse> result = productService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ProductResponse::getName)
                .containsExactly("Jeans", "Sneakers");
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    @Test
    void update_shouldModifyProductFields() {
        UUID id = UUID.randomUUID();
        Product existing = buildProduct(id, "Old Name", "10.00", 5, Product.Category.OTROS, true);
        when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductRequest request = buildRequest("New Name", "99.99", 50, Product.Category.CAMISETAS);
        ProductResponse response = productService.update(id, request);

        assertThat(response.getName()).isEqualTo("New Name");
        assertThat(response.getPrice()).isEqualByComparingTo("99.99");
        assertThat(response.getStock()).isEqualTo(50);
        assertThat(response.getCategory()).isEqualTo(Product.Category.CAMISETAS);
    }

    // -------------------------------------------------------------------------
    // delete
    // -------------------------------------------------------------------------

    @Test
    void delete_shouldSetActiveFalse() {
        UUID id = UUID.randomUUID();
        Product product = buildProduct(id, "Skirt", "39.99", 8, Product.Category.FALDAS, true);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        productService.delete(id);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getActive()).isFalse();
    }

    @Test
    void delete_shouldThrowException_whenProductNotFound() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(id))
                .isInstanceOf(ProductNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private ProductRequest buildRequest(String name, String price, int stock, Product.Category category) {
        ProductRequest request = new ProductRequest();
        request.setName(name);
        request.setPrice(new BigDecimal(price));
        request.setStock(stock);
        request.setCategory(category);
        return request;
    }

    private Product buildProduct(UUID id, String name, String price, int stock,
                                  Product.Category category, boolean active) {
        return Product.builder()
                .id(id)
                .name(name)
                .price(new BigDecimal(price))
                .stock(stock)
                .category(category)
                .active(active)
                .build();
    }
}

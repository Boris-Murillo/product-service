package org.example.productservice.service;

import lombok.RequiredArgsConstructor;
import org.example.productservice.dto.ProductRequest;
import org.example.productservice.dto.ProductResponse;
import org.example.productservice.event.ProductEventPublisher;
import org.example.productservice.model.Product;
import org.example.productservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductEventPublisher eventPublisher;

    public List<ProductResponse> findAll() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    public ProductResponse findById(UUID id) {
        Product product = getActiveProduct(id);
        return ProductResponse.from(product);
    }

    public List<ProductResponse> findByCategory(Product.Category category) {
        return productRepository.findByCategoryAndActiveTrue(category)
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    public List<ProductResponse> search(String name) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(name)
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(request.getCategory())
                .size(request.getSize())
                .color(request.getColor())
                .brand(request.getBrand())
                .imageUrl(request.getImageUrl())
                .build();

        ProductResponse response = ProductResponse.from(productRepository.save(product));
        eventPublisher.publishCreated(response);
        return response;
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = getActiveProduct(id);

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(request.getCategory());
        product.setSize(request.getSize());
        product.setColor(request.getColor());
        product.setBrand(request.getBrand());
        product.setImageUrl(request.getImageUrl());

        ProductResponse response = ProductResponse.from(productRepository.save(product));
        eventPublisher.publishUpdated(response);
        return response;
    }

    @Transactional
    public void delete(UUID id) {
        Product product = getActiveProduct(id);
        product.setActive(false);
        ProductResponse response = ProductResponse.from(productRepository.save(product));
        eventPublisher.publishDeleted(response);
    }

    private Product getActiveProduct(UUID id) {
        return productRepository.findById(id)
                .filter(Product::getActive)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con id: " + id));
    }
}

package org.example.productservice.controller;

import tools.jackson.databind.ObjectMapper;
import org.example.productservice.dto.ProductRequest;
import org.example.productservice.dto.ProductResponse;
import org.example.productservice.model.Product;
import org.example.productservice.service.ProductNotFoundException;
import org.example.productservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // GET /api/products
    // -------------------------------------------------------------------------

    @Test
    void findAll_shouldReturn200WithProductList() throws Exception {
        List<ProductResponse> products = List.of(
                buildResponse(UUID.randomUUID(), "Jeans", "49.99"),
                buildResponse(UUID.randomUUID(), "White T-Shirt", "29.99")
        );
        when(productService.findAll()).thenReturn(products);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Jeans"))
                .andExpect(jsonPath("$[1].name").value("White T-Shirt"));
    }

    @Test
    void findAll_withCategoryParam_shouldFilterByCategory() throws Exception {
        List<ProductResponse> products = List.of(buildResponse(UUID.randomUUID(), "Red Dress", "59.99"));
        when(productService.findByCategory(Product.Category.VESTIDOS)).thenReturn(products);

        mockMvc.perform(get("/api/products").param("category", "VESTIDOS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Red Dress"));
    }

    @Test
    void findAll_withSearchParam_shouldSearchByName() throws Exception {
        List<ProductResponse> products = List.of(buildResponse(UUID.randomUUID(), "Black Sneakers", "89.99"));
        when(productService.search("sneakers")).thenReturn(products);

        mockMvc.perform(get("/api/products").param("search", "sneakers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Black Sneakers"));
    }

    @Test
    void findById_shouldReturn200_whenProductExists() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.findById(id)).thenReturn(buildResponse(id, "Skirt", "39.99"));

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Skirt"));
    }

    @Test
    void findById_shouldReturn404_whenProductNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.findById(id)).thenThrow(new ProductNotFoundException("Product not found: " + id));

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // POST /api/products
    // -------------------------------------------------------------------------

    @Test
    void create_shouldReturn201_withValidRequest() throws Exception {
        ProductRequest request = buildRequest("White T-Shirt", "29.99", 10, Product.Category.CAMISETAS);
        ProductResponse response = buildResponse(UUID.randomUUID(), "White T-Shirt", "29.99");
        when(productService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("White T-Shirt"));
    }

    @Test
    void create_shouldReturn400_whenNameIsBlank() throws Exception {
        ProductRequest request = buildRequest("", "29.99", 10, Product.Category.CAMISETAS);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenPriceIsZero() throws Exception {
        ProductRequest request = buildRequest("T-Shirt", "0", 10, Product.Category.CAMISETAS);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenStockIsNegative() throws Exception {
        ProductRequest request = buildRequest("T-Shirt", "29.99", -1, Product.Category.CAMISETAS);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturn200_withValidRequest() throws Exception {
        UUID id = UUID.randomUUID();
        ProductRequest request = buildRequest("Updated Jeans", "55.00", 20, Product.Category.PANTALONES);
        ProductResponse response = buildResponse(id, "Updated Jeans", "55.00");
        when(productService.update(eq(id), any())).thenReturn(response);

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Jeans"));
    }

    @Test
    void update_shouldReturn404_whenProductNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        ProductRequest request = buildRequest("Jeans", "55.00", 20, Product.Category.PANTALONES);
        when(productService.update(eq(id), any())).thenThrow(new ProductNotFoundException("Product not found: " + id));

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204_whenProductExists() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(productService).delete(id);

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_shouldReturn404_whenProductNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ProductNotFoundException("Product not found: " + id)).when(productService).delete(id);

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNotFound());
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

    private ProductResponse buildResponse(UUID id, String name, String price) {
        return ProductResponse.builder()
                .id(id)
                .name(name)
                .price(new BigDecimal(price))
                .category(Product.Category.CAMISETAS)
                .active(true)
                .build();
    }
}

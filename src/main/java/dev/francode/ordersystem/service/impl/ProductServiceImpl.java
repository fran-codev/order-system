package dev.francode.ordersystem.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import dev.francode.ordersystem.dto.product.ProductFilter;
import dev.francode.ordersystem.dto.product.ProductRequest;
import dev.francode.ordersystem.dto.product.ProductResponse;
import dev.francode.ordersystem.entity.Category;
import dev.francode.ordersystem.entity.Product;
import dev.francode.ordersystem.exceptions.custom.ValidationException;
import dev.francode.ordersystem.mapper.ProductMapper;
import dev.francode.ordersystem.repository.CategoryRepository;
import dev.francode.ordersystem.repository.ProductRepository;
import dev.francode.ordersystem.service.spec.ProductSpecifications;
import org.springframework.data.jpa.domain.Specification;
import dev.francode.ordersystem.service.interfaces.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;


import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final Cloudinary cloudinary;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp"
    );

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        MultipartFile imageFile = request.getImageUrl();
        if (imageFile != null && !imageFile.isEmpty()) {
            validateImage(imageFile);
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ValidationException("Categoría no encontrada"));

        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = uploadImageToCloudinary(imageFile);
        }

        Product product = productMapper.toEntity(request);
        product.setCategory(category);
        product.setImageUrl(imageUrl);

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ValidationException("Producto no encontrado"));

        MultipartFile imageFile = request.getImageUrl();
        if (imageFile != null && !imageFile.isEmpty()) {
            validateImage(imageFile);
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ValidationException("Categoría no encontrada"));

        String imageUrl = product.getImageUrl();
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = uploadImageToCloudinary(imageFile);
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setStatus(request.getStatus());
        product.setCategory(category);
        product.setImageUrl(imageUrl);

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ValidationException("Producto no encontrado"));
        productRepository.delete(product);
    }

    @Override
    public ProductResponse getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ValidationException("Producto no encontrado"));
        return productMapper.toResponse(product);
    }

    @Override
    public Page<ProductResponse> getProducts(ProductFilter filter, Pageable pageable) {
        Specification<Product> spec = ProductSpecifications.filterBy(filter);

        return productRepository.findAll(spec, pageable)
                .map(productMapper::toResponse);
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new ValidationException("Tipo de imagen no permitido. Solo se permiten JPG, PNG y WEBP.");
        }
    }

    private String uploadImageToCloudinary(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new ValidationException("Error al subir imagen a Cloudinary: " + e.getMessage());
        }
    }
}

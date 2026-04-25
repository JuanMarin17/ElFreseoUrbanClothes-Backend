package com.api.product.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.api.product.dto.ProductRequestDTO;
import com.api.product.dto.ProductResponseDTO;
import com.api.product.entity.Product;
import com.api.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    // Se define variable constante (final), de repository con tipo de dato ProductRepository, para poder usar los métodos de la interfaz repository
    private final ProductRepository productRepository;

    // Metodo de nuestro servicio para crear producto, respondiendo con ProductResponseDTO y recibiendo ProductRequestDTO
    public ProductResponseDTO createProduct (ProductRequestDTO productRequestDTO){
        Product product = new Product();

        product.setName_product(productRequestDTO.getName_product());
        product.setDescription_product(productRequestDTO.getDescription_product());
        product.setSize_product(productRequestDTO.getSize_product());
        product.setSale_price_product(productRequestDTO.getSale_price_product());
        product.setSupplier_price_product(productRequestDTO.getSupplier_price_product());
        product.setStock_product(productRequestDTO.getStock_product());
        product.setId_category(productRequestDTO.getId_category());
        product.setState_product(productRequestDTO.getState_product());
        
        productRepository.save(product);

        ProductResponseDTO response = new ProductResponseDTO();
        response.setId_product(product.getId_product());
        response.setName_product(productRequestDTO.getName_product());
        response.setDescription_product(productRequestDTO.getDescription_product());
        response.setSize_product(productRequestDTO.getSize_product());
        response.setSale_price_product(productRequestDTO.getSale_price_product());
        response.setSupplier_price_product(productRequestDTO.getSupplier_price_product());
        response.setStock_product(productRequestDTO.getStock_product());
        response.setId_category(productRequestDTO.getId_category());

        return response;
    }

    public List<ProductResponseDTO> ListProducts(){
        List<Product> products = productRepository.findAll();
        List<ProductResponseDTO> list = new ArrayList<>();

        for(Product product: products){
            ProductResponseDTO responseDTO = new ProductResponseDTO();
            responseDTO.setId_product(product.getId_product());
            responseDTO.setName_product(product.getName_product());
            responseDTO.setDescription_product(product.getDescription_product());
            responseDTO.setSize_product(product.getSize_product());
            responseDTO.setSale_price_product(product.getSale_price_product());
            responseDTO.setSupplier_price_product(product.getSupplier_price_product());
            responseDTO.setStock_product(product.getStock_product());
            responseDTO.setId_category(product.getId_category());
            list.add(responseDTO);
        }
        return list;

    }

    public Optional<ProductResponseDTO> listProdcut(Long id_product){
        Optional<Product> optionalProduct = productRepository.findById(id_product);
        
        if (optionalProduct.isPresent()){
            Product product = optionalProduct.get();
            ProductResponseDTO responseDTO = new ProductResponseDTO();
            responseDTO.setId_product(product.getId_product());
            responseDTO.setName_product(product.getName_product());
            responseDTO.setDescription_product(product.getDescription_product());
            responseDTO.setSize_product(product.getSize_product());
            responseDTO.setSale_price_product(product.getSale_price_product());
            responseDTO.setSupplier_price_product(product.getSupplier_price_product());
            responseDTO.setStock_product(product.getStock_product());
            responseDTO.setId_category(product.getId_category());
            return Optional.of(responseDTO);
        } else {

            return Optional.empty();
        }
    }

    public Optional<ProductResponseDTO> updateProduct(Long id_product, ProductRequestDTO productRequestDTO){
        Optional<Product> optionalProduct = productRepository.findById(id_product);
        
        if (optionalProduct.isPresent()){
            Product product = optionalProduct.get();
            product.setName_product(productRequestDTO.getName_product());
            product.setDescription_product(productRequestDTO.getDescription_product());
            product.setSize_product(productRequestDTO.getSize_product());
            product.setSale_price_product(productRequestDTO.getSale_price_product());
            product.setSupplier_price_product(productRequestDTO.getSupplier_price_product());
            product.setStock_product(productRequestDTO.getStock_product());
            product.setId_category(productRequestDTO.getId_category());
            product.setState_product(productRequestDTO.getState_product());

            Product updateProduct = productRepository.save(product);

            ProductResponseDTO responseDTO = new ProductResponseDTO();
            responseDTO.setId_product(updateProduct.getId_product());
            responseDTO.setName_product(updateProduct.getName_product());
            responseDTO.setDescription_product(updateProduct.getDescription_product());
            responseDTO.setSize_product(updateProduct.getSize_product());
            responseDTO.setSale_price_product(updateProduct.getSale_price_product());
            responseDTO.setSupplier_price_product(updateProduct.getSupplier_price_product());
            responseDTO.setStock_product(updateProduct.getStock_product());
            responseDTO.setId_category(updateProduct.getId_category());

            return Optional.of(responseDTO);
        } else {
            return Optional.empty();
        }
    }
    
    public Optional<ProductResponseDTO> deleteProduct(Long id_product){
    Optional<Product> optionalProduct = productRepository.findById(id_product);

    if(optionalProduct.isPresent()){
        Product product = optionalProduct.get();

        ProductResponseDTO responseDTO = new ProductResponseDTO();
        responseDTO.setId_product(product.getId_product());
        responseDTO.setName_product(product.getName_product());
        responseDTO.setDescription_product(product.getDescription_product());
        responseDTO.setSize_product(product.getSize_product());
        responseDTO.setSale_price_product(product.getSale_price_product());
        responseDTO.setSupplier_price_product(product.getSupplier_price_product());
        responseDTO.setStock_product(product.getStock_product());
        responseDTO.setId_category(product.getId_category());

        productRepository.delete(product);

        return Optional.of(responseDTO);
    } else {
        return Optional.empty();
    }
}
}

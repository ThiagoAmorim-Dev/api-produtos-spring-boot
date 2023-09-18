package com.example.springboot.controllers;


import com.example.springboot.dtos.ProductRecordDto;
import com.example.springboot.models.ProductModel;
import com.example.springboot.repositories.ProductRepository;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
public class ProductController {

    @Autowired
    ProductRepository productRepository;

    @PostMapping("/products")
    public ResponseEntity<ProductModel> saveProduct(@RequestBody @Valid ProductRecordDto productRecordDto) {
        var productModel = new ProductModel();
        BeanUtils.copyProperties(productRecordDto, productModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(productModel));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductModel>> getAllProducts(){
        List<ProductModel> productsList = productRepository.findAll();
        if(!productsList.isEmpty()){
            for(ProductModel product : productsList) {
                UUID id = product.getIdProduct();
                product.add(linkTo(methodOn(ProductController.class).getOneProduct(id)).withSelfRel());
            }
            return ResponseEntity.status(HttpStatus.OK).body(productsList);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(productsList);
        }
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Object> getOneProduct(@PathVariable(value ="id") UUID id) {
        Optional<ProductModel> product = productRepository.findById(id);
        if(product.isPresent()) {
            product.get().add(linkTo(methodOn(ProductController.class).getAllProducts()).withRel("Products List"));
            return ResponseEntity.status(HttpStatus.OK).body(product.get());
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cara, não encontrei esse produto. Me desculpe.");
        }
    }


    @PutMapping("/products/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable(value="id") UUID id,
                                                @RequestBody @Valid ProductRecordDto productRecordDto) {

        Optional<ProductModel> product = productRepository.findById(id);
        if(product.isPresent()) {
            var productModel = product.get();
            BeanUtils.copyProperties(productRecordDto, productModel);
            return ResponseEntity.status(HttpStatus.OK).body(productRepository.save(productModel));
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cara, não consegui encontrar o produto para realizar a sua edição.");

        }
    }



    @DeleteMapping("/products/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable(value="id") UUID id) {
        Optional<ProductModel> product = productRepository.findById(id);
        if(product.isPresent()) {
            productRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK).body("O produto "+ product.get().getName()+" foi excluído.");
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cara, não consegui encontrar o produto para realizar a exclusão.");
        }
    }

}

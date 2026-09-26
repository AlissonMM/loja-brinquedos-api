package edu.meialua.morkstore.adapters.out.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.meialua.morkstore.adapters.in.Product;
import edu.meialua.morkstore.adapters.in.enums.EntityType;
import edu.meialua.morkstore.adapters.in.enums.Action;
import edu.meialua.morkstore.adapters.in.repositories.OrderRepository;
import edu.meialua.morkstore.adapters.in.repositories.ProductRepository;
import edu.meialua.morkstore.config.KafkaTopics;
import edu.meialua.morkstore.model.LogEvent;
import edu.meialua.morkstore.model.ProductDTO;
import edu.meialua.morkstore.model.VisibilityUpdateDTO;
import edu.meialua.morkstore.service.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    KafkaProducerService kafkaProducerService;

    // Desserializa o attributesJson (string JSON crua vinda do form-data multipart)
    // em Map<String,String>; string nula/vazia ou inválida vira mapa vazio.
    private Map<String, String> parseAttributes(String attributesJson) {
        if (attributesJson == null || attributesJson.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(attributesJson, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }


    // http://localhost:8080/products/findAll
    @GetMapping("/findAll")
    public ResponseEntity<String> getProducts() throws JsonProcessingException {

        try {
            List<Product> products = productRepository.findAll();

            return ResponseEntity.ok(objectMapper.writeValueAsString(products));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("PRODUTOS NÃO ENCONTRADOS");
        }
    }


    // http://localhost:8080/products/findByName
    @GetMapping("/findByName/{name}")
    public ResponseEntity<String> getProductByName(
            @PathVariable("name") String name) throws JsonProcessingException {

        Optional<Product> product = productRepository.findByName(name);

        if (product.isPresent()) {
            return ResponseEntity.ok(objectMapper.writeValueAsString(product));
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("PRODUTO NÃO ENCONTRADO COM ESSE NOME");
    }


    // http://localhost:8080/products/findAllByBrand
    @GetMapping("/findAllByBrand/{brand}")
    public ResponseEntity<String> getProductsByBrand(
            @PathVariable("brand") String brand) throws JsonProcessingException {

        List<Product> products = productRepository.findAllByBrand(brand);

        if (!products.isEmpty()) {
            return ResponseEntity.ok(objectMapper.writeValueAsString(products));
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("PRODUTOS NÃO ENCONTRADOS COM ESSA MARCA");
    }


    // http://localhost:8080/products/findAllByCategory
    @GetMapping("/findAllByCategory/{category}")
    public ResponseEntity<String> getProductsByCategory(
            @PathVariable("category") String category) throws JsonProcessingException {

        List<Product> products = productRepository.findAllByCategory(category);

        if (!products.isEmpty()) {
            return ResponseEntity.ok(objectMapper.writeValueAsString(products));
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("PRODUTOS NÃO ENCONTRADOS COM ESSA CATEGORIA");
    }


    // http://localhost:8080/products/insert
    @PostMapping(
            value = "/insert",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, String>> createProduct(
            @ModelAttribute ProductDTO product) throws IOException {

        Map<String, String> response = new HashMap<>();

        Product addProduct = new Product();

        addProduct.setName(product.getName());
        addProduct.setDescription(product.getDescription());
        addProduct.setBrand(product.getBrand());
        addProduct.setValue(product.getValue());
        addProduct.setCategory(product.getCategory());
        addProduct.setImage(product.getImage().getBytes());
        addProduct.setVisibleInCatalog(product.isVisibleInCatalog());
        addProduct.setStock(product.getStock());
        addProduct.setAttributes(parseAttributes(product.getAttributesJson()));

        try {

            productRepository.save(addProduct);

            LogEvent event = LogEvent.builder()
                    .action(Action.REGISTER)
                    .entity(EntityType.PRODUCT)
                    .entityId(addProduct.getId())
                    .user(null)
                    .description("Produto cadastrado com sucesso.")
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaProducerService.sendProductEvent(event);

            response.put(
                    "message",
                    "PRODUTO CADASTRADO COM SUCESSO"
            );

            return ResponseEntity.ok().body(response);

        } catch (Exception e) {

            response.put(
                    "message",
                    "ERRO AO CADASTRAR PRODUTO! " + e.getMessage()
            );

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }
    }


    // http://localhost:8080/products/deleteById/{id}
    @DeleteMapping("/deleteById/{id}")
    public ResponseEntity<Map<String, String>> deleteById(
            @PathVariable("id") Long id) {

        Map<String, String> response = new HashMap<>();

        Optional<Product> existingProduct = productRepository.findById(id);

        if (existingProduct.isPresent()) {

            if (orderRepository.existsByItems_Product_Id(id)) {

                response.put(
                        "message",
                        "NÃO É POSSÍVEL EXCLUIR: ESTE PRODUTO JÁ FOI VENDIDO."
                );

                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(response);
            }

            try {

                productRepository.deleteById(id);

                LogEvent event = LogEvent.builder()
                        .action(Action.DELETE)
                        .entity(EntityType.PRODUCT)
                        .entityId(id)
                        .user(null)
                        .description("Produto deletado com sucesso.")
                        .timestamp(LocalDateTime.now())
                        .build();

                kafkaProducerService.sendProductEvent(event);

                response.put(
                        "message",
                        "PRODUTO DELETADO COM SUCESSO PELO ID."
                );

                return ResponseEntity.ok().body(response);

            } catch (DataIntegrityViolationException e) {

                response.put(
                        "message",
                        "NÃO É POSSÍVEL EXCLUIR: ESTE PRODUTO JÁ FOI VENDIDO."
                );

                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(response);
            }

        } else {

            response.put(
                    "message",
                    "NENHUM PRODUTO ENCONTRADO COM ESSE ID."
            );

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(response);
        }
    }


    // http://localhost:8080/products/update/{id}
    @PutMapping(
            value = "/update/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, String>> updateProduct(
            @PathVariable("id") Long id,
            @ModelAttribute ProductDTO product) {

        Map<String, String> response = new HashMap<>();

        Optional<Product> optionalProduct = productRepository.findById(id);

        if (optionalProduct.isEmpty()) {

            response.put(
                    "message",
                    "PRODUTO COM ESSES DADOS NÃO ENCONTRADO"
            );

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(response);
        }

        try {

            Product updProduct = optionalProduct.get();

            updProduct.setName(product.getName());
            updProduct.setDescription(product.getDescription());
            updProduct.setBrand(product.getBrand());
            updProduct.setValue(product.getValue());
            updProduct.setCategory(product.getCategory());
            updProduct.setVisibleInCatalog(product.isVisibleInCatalog());
            updProduct.setStock(product.getStock());
            updProduct.setAttributes(parseAttributes(product.getAttributesJson()));

            if (product.getImage() != null && !product.getImage().isEmpty()) {
                updProduct.setImage(product.getImage().getBytes());
            }

            productRepository.save(updProduct);

            LogEvent event = LogEvent.builder()
                    .action(Action.UPDATE)
                    .entity(EntityType.PRODUCT)
                    .entityId(updProduct.getId())
                    .user(null)
                    .description("Produto atualizado com sucesso.")
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaProducerService.sendProductEvent(event);

            response.put(
                    "message",
                    "PRODUTO ATUALIZADO COM SUCESSO"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            response.put(
                    "message",
                    "Error processing image"
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }


    @PatchMapping("/updateVisibility/{id}")
    public ResponseEntity<Map<String, String>> updateVisibility(
            @PathVariable("id") Long id,
            @RequestBody VisibilityUpdateDTO visibilityUpdateDTO) {

        Map<String, String> response = new HashMap<>();

        Optional<Product> productOptional = productRepository.findById(id);

        if (productOptional.isEmpty()) {

            response.put(
                    "message",
                    "Produto não encontrado"
            );

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(response);
        }

        Product product = productOptional.get();

        product.setVisibleInCatalog(
                visibilityUpdateDTO.isVisibleInCatalog()
        );

        try {

            productRepository.save(product);

            LogEvent event = LogEvent.builder()
                    .action(Action.UPDATE)
                    .entity(EntityType.PRODUCT)
                    .entityId(product.getId())
                    .user(null)
                    .description(
                            "Visibilidade do produto atualizada com sucesso."
                    )
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaProducerService.sendProductEvent(event);

            response.put(
                    "message",
                    "Visibilidade do produto atualizada com sucesso"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            response.put(
                    "message",
                    "Erro ao atualizar visibilidade: " + e.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }
}
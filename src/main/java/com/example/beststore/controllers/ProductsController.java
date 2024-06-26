package com.example.beststore.controllers;

import com.example.beststore.models.Product;
import com.example.beststore.models.ProductDto;
import com.example.beststore.services.ProductsRepository;
import jakarta.persistence.Column;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.Binding;
import java.io.InputStream;
import java.nio.channels.MulticastChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    private ProductsRepository repo;

    @GetMapping
    public String showProductList(Model model) {
        List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("products", products);
        return "index";
    }

    @GetMapping("/create")
    public String showCreatePage(Model model) {
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto", productDto);
        return "createproduct";
    }

    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute ProductDto productDto, BindingResult result){

        if(productDto.getImageFile().isEmpty()) {
            result.addError(new FieldError("productDto", "imageFile", "The image file is required"));
        }

        if(result.hasErrors()){
            return "createproduct";
        }

        //save image file
        MultipartFile image = productDto.getImageFile();
        Date createdAt = new Date();
        String storageFileName = createdAt.getTime() + "_" +image.getOriginalFilename();

        try {
            String uploadDir = "public/images/";
            //Path uploadPath = Paths.get(uploadDir);
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());

        }

         Product product = new Product();
        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());
        product.setCategory(productDto.getCategory());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setCreatedAt(createdAt);
        product.setImageFileName(storageFileName);

        repo.save(product);

        return "redirect:/products";
    }

//    @GetMapping("/edit")
//    public String showEditPage(Model model, @RequestParam int id) {
//
//        try {
//            Product product = repo.findById(id).get();
//            model.addAttribute("product", product);
//
//            ProductDto productDto = new ProductDto();
//            productDto.setName(product.getName());
//            productDto.setBrand(product.getBrand());
//            productDto.setCategory(product.getCategory());
//            productDto.setPrice(product.getPrice());
//            productDto.setDescription(product.getDescription());
//
//        }
//        catch(Exception ex) {
//            System.out.println("Exception: " + ex.getMessage());
//            return "redirect:/products";
//        }
//        return "editproduct";

    @GetMapping("/edit")
    public String showEditPage(Model model, @RequestParam int id) {

        try {
            Product product = repo.findById(id).orElse(null);
            if (product == null) {
                return "redirect:/products";
            }
            model.addAttribute("product", product);

            // Create a ProductDto and populate it with product data
            ProductDto productDto = new ProductDto();
            productDto.setName(product.getName());
            productDto.setBrand(product.getBrand());
            productDto.setCategory(product.getCategory());
            productDto.setPrice(product.getPrice());
            productDto.setDescription(product.getDescription());

            // Add the productDto to the model
            model.addAttribute("productDto", productDto);

        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
            return "redirect:/products";
        }
        return "editproduct";
    }


//    @PostMapping("/edit")
//    public String updateProduct(Model model, @RequestParam int id, @Valid @ModelAttribute ProductDto productDto, BindingResult result) {
//
//       try {
//           Product product = repo.findById(id).get();
//           model.addAttribute("product", product);
//
//           if(result.hasErrors()) {
//               return "editproduct";
//           }
//
//           if(productDto.getImageFile().isEmpty()) {
//               //delete old image
//               String uploadDir = "public/images/";
//               Path oldImagepath = Paths.get(uploadDir + product.getImageFileName());
//
//               try {
//                   Files.delete(oldImagepath);
//               }
//
//               catch(Exception ex) {
//                   System.out.println("Exception: " + ex.getMessage());
//               }
//
//               //save new image file
//               MultipartFile image = productDto.getImageFile();
//               Date createdAt = new Date();
//               String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();
//
//               try(InputStream inputStream = image.getInputStream()) {
//                   Files.copy(inputStream, Paths.get(uploadDir+storageFileName),
//                   StandardCopyOption.REPLACE_EXISTING);
//               }
//               product.setImageFileName(storageFileName);
//           }
//
//           product.setName(productDto.getName());
//           product.setBrand(productDto.getBrand());
//           product.setCategory(productDto.getCategory());
//           product.setPrice(productDto.getPrice());
//           product.setDescription(productDto.getDescription());
//
//           repo.save(product);
//       }
//       catch(Exception ex) {
//           System.out.println("Exception: " + ex.getMessage());
//       }
//
//
//        return "redirect:/products";
//    }

    @PostMapping("/edit")
    public String updateProduct(Model model, @RequestParam int id, @Valid @ModelAttribute ProductDto productDto, BindingResult result) {

        try {
            Product product = repo.findById(id).orElse(null);
            if (product == null) {
                return "redirect:/products";
            }
            model.addAttribute("product", product);

            if (result.hasErrors()) {
                return "editproduct";
            }

            if (!productDto.getImageFile().isEmpty()) {
                // Delete old image
                String uploadDir = "public/images/";
                Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());

                try {
                    Files.delete(oldImagePath);
                } catch (Exception ex) {
                    System.out.println("Exception: " + ex.getMessage());
                }

                // Save new image file
                MultipartFile image = productDto.getImageFile();
                Date createdAt = new Date();
                String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

                try (InputStream inputStream = image.getInputStream()) {
                    Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
                }
                product.setImageFileName(storageFileName);
            }

            // Update other product details
            product.setName(productDto.getName());
            product.setBrand(productDto.getBrand());
            product.setCategory(productDto.getCategory());
            product.setPrice(productDto.getPrice());
            product.setDescription(productDto.getDescription());

            repo.save(product);
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }

        return "redirect:/products";
    }

    @GetMapping("/delete")
    public  String deleteProduct(@RequestParam int id){

        try {
            Product product = repo.findById(id).get();

            //delete product image
            Path imagePath = Paths.get("public/images/" + product.getImageFileName());

            try {
                Files.delete(imagePath);
            } catch (Exception ex) {
                System.out.println("Exception: " + ex.getMessage());
            }

            //delete the product
            repo.delete(product);
        }
            catch(Exception ex) {
                System.out.println("Exception: " + ex.getMessage());
            }
        return "redirect:/products";
    }

}



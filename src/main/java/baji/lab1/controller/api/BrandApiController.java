package baji.lab1.controller.api;

import baji.lab1.entity.Brand;
import baji.lab1.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/brands")
public class BrandApiController {

    @Autowired
    private BrandRepository brandRepository;

    // GET все бренды
    @GetMapping
    public List<Brand> getAllBrands() {
        return (List<Brand>) brandRepository.findAll();
    }

    // GET бренд по ID
    @GetMapping("/{id}")
    public ResponseEntity<Brand> getBrandById(@PathVariable Long id) {
        Optional<Brand> brand = brandRepository.findById(id);
        return brand.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST создать бренд
    @PostMapping
    public ResponseEntity<Brand> createBrand(@RequestBody Brand brand) {
        Brand savedBrand = brandRepository.save(brand);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBrand);
    }

    // PUT обновить бренд
    @PutMapping("/{id}")
    public ResponseEntity<Brand> updateBrand(@PathVariable Long id, @RequestBody Brand brandDetails) {
        Optional<Brand> optionalBrand = brandRepository.findById(id);
        if (optionalBrand.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Brand brand = optionalBrand.get();
        brand.setName(brandDetails.getName()); // добавь другие поля, если есть

        Brand updatedBrand = brandRepository.save(brand);
        return ResponseEntity.ok(updatedBrand);
    }

    // DELETE удалить бренд
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        if (brandRepository.existsById(id)) {
            brandRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
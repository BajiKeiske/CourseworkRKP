package baji.lab1.controller;

import baji.lab1.dto.ReviewCreateDto;
import baji.lab1.entity.Product;
import baji.lab1.entity.Review;
import baji.lab1.entity.User;
import baji.lab1.repository.ProductRepository;
import baji.lab1.repository.ReviewRepository;
import baji.lab1.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ReviewService reviewService;

    // Добавление отзыва
    @PostMapping("/add")
    public String addReview(@Valid @ModelAttribute("reviewDto") ReviewCreateDto dto,
                            BindingResult bindingResult,
                            @AuthenticationPrincipal User currentUser,
                            RedirectAttributes redirectAttributes) {

        // ВЫВОДИМ ВСЕ ОШИБКИ ВАЛИДАЦИИ В КОНСОЛЬ
        if (bindingResult.hasErrors()) {
            System.out.println("========== ОШИБКИ ВАЛИДАЦИИ ОТЗЫВА ==========");
            bindingResult.getAllErrors().forEach(error -> {
                System.out.println("Ошибка: " + error.getDefaultMessage());
            });
            System.out.println("dto.getProductId(): " + dto.getProductId());
            System.out.println("dto.getRating(): " + dto.getRating());
            System.out.println("dto.getText(): " + dto.getText());
            System.out.println("=============================================");

            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.reviewDto", bindingResult);
            redirectAttributes.addFlashAttribute("reviewDto", dto);
            redirectAttributes.addFlashAttribute("reviewError", "Ошибка валидации: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/user/products/details/" + dto.getProductId();
        }

        try {
            // Проверяем, не оставлял ли уже отзыв
            if (reviewRepository.existsByUser_IdAndProduct_Id(currentUser.getId(), dto.getProductId())) {
                redirectAttributes.addFlashAttribute("reviewError", "Вы уже оставляли отзыв на этот товар");
                return "redirect:/user/products/details/" + dto.getProductId();
            }

            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

            // Создаём отзыв со статусом PENDING
            Review review = new Review();
            review.setUser(currentUser);
            review.setProduct(product);
            review.setText(dto.getText());
            review.setRating(dto.getRating());
            review.setStatus(baji.lab1.entity.ReviewStatus.PENDING);

            reviewRepository.save(review);

            // Обновляем рейтинг через ReviewService
            reviewService.updateProductRating(product.getId());

            redirectAttributes.addFlashAttribute("reviewSuccess", "Спасибо за отзыв! Он будет опубликован после проверки модератором.");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("reviewError", "Ошибка: " + e.getMessage());
        }

        return "redirect:/user/products/details/" + dto.getProductId();
    }

    // Удаление отзыва
    @PostMapping("/delete/{id}")
    public String deleteReview(@PathVariable Long id,
                               @AuthenticationPrincipal User currentUser,
                               RedirectAttributes redirectAttributes) {
        try {
            Review review = reviewRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));

            // Проверка прав
            if (!review.getUser().getId().equals(currentUser.getId())
                    && !currentUser.getRole().equals("ROLE_ADMIN")) {
                redirectAttributes.addFlashAttribute("reviewError", "Недостаточно прав");
                return "redirect:/user/products/catalog";
            }

            Long productId = review.getProduct().getId();

            // Удаляем отзыв
            reviewRepository.delete(review);

            // Обновляем рейтинг через ReviewService
            reviewService.updateProductRating(productId);

            redirectAttributes.addFlashAttribute("reviewSuccess", "Отзыв удалён");
            return "redirect:/user/products/details/" + productId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("reviewError", "Ошибка: " + e.getMessage());
            return "redirect:/user/products/catalog";
        }
    }
}
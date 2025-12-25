package baji.lab1.controller;

import baji.lab1.dto.ReviewCreateDto;
import baji.lab1.entity.Product;
import baji.lab1.entity.Review;
import baji.lab1.entity.User;
import baji.lab1.repository.ProductRepository;
import baji.lab1.repository.ReviewRepository;
import baji.lab1.service.ProductRatingService;
import baji.lab1.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ProductRatingService productRatingService; // Добавь эту зависимость

    @PostMapping("/add")
    public String addReview(@Valid @ModelAttribute("reviewDto") ReviewCreateDto dto,
                            BindingResult bindingResult,
                            @AuthenticationPrincipal User currentUser,
                            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.reviewDto", bindingResult);
            redirectAttributes.addFlashAttribute("reviewDto", dto);
            redirectAttributes.addFlashAttribute("reviewError", "Ошибка валидации");
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

            // Создаем отзыв
            Review review = Review.builder()
                    .user(currentUser)
                    .product(product)
                    .text(dto.getText())
                    .rating(dto.getRating())
                    .build();

            reviewRepository.save(review);

            // ОБНОВЛЯЕМ РЕЙТИНГ ТОВАРА
            productRatingService.updateProductRating(product.getId());

            redirectAttributes.addFlashAttribute("reviewSuccess", "Отзыв успешно добавлен!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("reviewError", "Ошибка: " + e.getMessage());
        }

        return "redirect:/user/products/details/" + dto.getProductId();
    }

    @PostMapping("/delete/{id}")
    public String deleteReview(@PathVariable Long id,
                               @AuthenticationPrincipal User currentUser,
                               RedirectAttributes redirectAttributes) {
        try {
            Review review = reviewRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));

            // Проверяем права
            if (!review.getUser().getId().equals(currentUser.getId())
                    && !currentUser.getRole().equals("ROLE_ADMIN")) {
                redirectAttributes.addFlashAttribute("reviewError", "Недостаточно прав");
                return "redirect:/user/products/catalog";
            }

            Long productId = review.getProduct().getId();

            // Удаляем отзыв
            reviewRepository.delete(review);

            // ОБНОВЛЯЕМ РЕЙТИНГ ПОСЛЕ УДАЛЕНИЯ
            productRatingService.updateProductRating(productId);

            redirectAttributes.addFlashAttribute("reviewSuccess", "Отзыв удалён");
            return "redirect:/user/products/details/" + productId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("reviewError", "Ошибка: " + e.getMessage());
            return "redirect:/user/products/catalog";
        }
    }
}
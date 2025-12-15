package baji.lab1.controller;

import baji.lab1.dto.ReviewCreateDto;
import baji.lab1.entity.Review;
import baji.lab1.entity.User;
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

    private final ReviewService reviewService;

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
            reviewService.createReview(currentUser, dto);
            redirectAttributes.addFlashAttribute("reviewSuccess", "Отзыв успешно добавлен!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("reviewError", e.getMessage());
        }

        return "redirect:/user/products/details/" + dto.getProductId();
    }

    @PostMapping("/delete/{id}")
    public String deleteReview(@PathVariable Long id,
                               @AuthenticationPrincipal User currentUser,
                               RedirectAttributes redirectAttributes) {
        try {
            // Получаем productId перед удалением
            Review review = reviewService.getReviewById(id); // Нужно добавить этот метод в ReviewService
            Long productId = review.getProduct().getId();

            reviewService.deleteReview(id, currentUser);
            redirectAttributes.addFlashAttribute("reviewSuccess", "Отзыв удалён");
            return "redirect:/user/products/details/" + productId;

        } catch (IllegalArgumentException | SecurityException e) {
            redirectAttributes.addFlashAttribute("reviewError", e.getMessage());
            return "redirect:/user/products/catalog"; // или на главную, если не найден товар
        }
    }
}
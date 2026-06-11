package baji.lab1.controller;

import baji.lab1.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public String reviewsPage(Model model) {
        model.addAttribute("pendingReviews", reviewService.getPendingReviews());
        return "admin/reviews";
    }

    @PostMapping("/{id}/approve")
    public String approveReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reviewService.approveReview(id);
        redirectAttributes.addFlashAttribute("successMessage", "Отзыв одобрен");
        return "redirect:/admin/reviews";
    }

    @PostMapping("/{id}/reject")
    public String rejectReview(@PathVariable Long id,
                               @RequestParam String reason,
                               RedirectAttributes redirectAttributes) {
        reviewService.rejectReview(id, reason);
        redirectAttributes.addFlashAttribute("successMessage", "Отзыв отклонён");
        return "redirect:/admin/reviews";
    }
}
package baji.lab1.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewDto {
    private Long id;
    private Long userId;
    private Long productId;
    private String text;
    private Integer rating;
    private String createdAt;
    private String username;
}
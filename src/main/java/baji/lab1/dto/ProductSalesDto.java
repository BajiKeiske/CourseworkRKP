package baji.lab1.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
class ProductSalesDto {
    private Long productId;
    private String productName;
    private Long quantity;
    private BigDecimal revenue;
}

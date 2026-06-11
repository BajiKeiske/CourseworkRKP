package baji.lab1.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class ReportDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalOrders;
    private BigDecimal totalSales;
    private BigDecimal averageCheck;
    private Long totalItemsSold;
    private List<ProductSalesDto> topProducts;
    private Map<LocalDate, BigDecimal> dailySales;
}


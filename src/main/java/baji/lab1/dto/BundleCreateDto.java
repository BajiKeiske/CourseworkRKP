package baji.lab1.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import lombok.*;

@Getter
@Setter
public class BundleCreateDto {

    private String name;
    private String description;
    private String imageUrl;

    private String discountType;
    private Double discountValue;

    private Map<Long, Integer> products;
}

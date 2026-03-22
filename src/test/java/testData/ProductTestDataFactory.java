package testData;

import payloads.request.ProductsPOJO;

public class ProductTestDataFactory {

    public static ProductsPOJO validProduct() {
        return ProductsPOJO.builder()
                .title("Product-" + System.currentTimeMillis())
                .price(221.10)
                .image("https://image.jpg")
                .category("electronics")
                .description("Valid product")
                .build();
    }

    public static ProductsPOJO negativePriceProduct() {
        return ProductsPOJO.builder()
                .title("Invalid Product")
                .price(-221.10)
                .image("https://image.jpg")
                .category("electronics")
                .description("Invalid price")
                .build();
    }

    public static ProductsPOJO productWithoutTitle() {
        return ProductsPOJO.builder()
                .price(200)
                .image("https://image.jpg")
                .category("electronics")
                .description("Missing title")
                .build();
    }

    public static ProductsPOJO xssProduct() {
        return ProductsPOJO.builder()
                .title("Safe Product")
                .price(200)
                .description("<script>alert('XSS')</script>")
                .build();
    }

    public static String productPriceAsString() {
        return """
        {
          "title": "Test Product",
          "price": "1000",
          "description": "Test desc"
        }
        """;
    }
}
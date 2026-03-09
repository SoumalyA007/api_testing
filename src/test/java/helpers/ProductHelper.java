package helpers;

import endpoints.Categories;
import endpoints.Products;
import enums.UserRole;
import payloads.request.ProductsPOJO;

import java.util.List;
import java.util.Random;

public class ProductHelper {

    public static ProductsPOJO validProduct() {
        return ProductsPOJO.builder()
                .title("Samsung Galaxy S20 FE")
                .price(221.10)
                .image("https://image.jpg")
                .category("electronics")
                .description("It is a very good flagship mobile")
                .build();
    }

    public static ProductsPOJO negativePriceProduct() {
        return ProductsPOJO.builder()
                .title("Samsung Galaxy S20 FE")
                .price(-221.10)
                .image("https://image.jpg")
                .category("electronics")
                .description("Invalid price product")
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

//    public static int getLastProductId() {
//
//        List<Integer> ids = Products.getAllProducts(UserRole.USER)
//                .then()
//                .extract()
//                .jsonPath()
//                .getList("id", Integer.class);
//
//        return ids.get(ids.size() - 1);
//    }

    public static int getRandomProductId(){

        List<Integer> ids = Products.getAllProducts(null)
                .then()
                .extract()
                .jsonPath()
                .getList("id", Integer.class);

        return ids.get(new Random().nextInt(ids.size()));
    }

    public static String getRandomCategory() {

        List<String> categories = Categories.getCategories(null)
                .then()
                .extract()
                .jsonPath()
                .getList("name", String.class);

        return categories.get(new Random().nextInt(categories.size()));
    }

    public static ProductsPOJO xssProduct(){

        return ProductsPOJO.builder()
                .title("Safe Product")
                .price(200)
                .description("<script>alert('XSS')</script>")
                .build();
    }

    public static String productPriceAsString(){

        return """
        {
          "title": "Test Product",
          "price": "1000",
          "description": "Test desc"
        }
        """;
    }

    public static int createTestProduct() {

        return Products.createProduct(validProduct(), UserRole.ADMIN)
                .then()
                .extract()
                .path("id");
    }
}
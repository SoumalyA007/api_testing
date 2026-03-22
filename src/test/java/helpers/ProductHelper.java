package helpers;

import endpoints.Categories;
import endpoints.Products;
import enums.UserRole;
import testData.ProductTestDataFactory;

import java.util.List;
import java.util.Random;

public class ProductHelper {

    private static List<Integer> cachedProductIds;
    private static List<String> cachedCategories;

    // ================= PRODUCT IDS =================

    public static List<Integer> getAllProductIds() {
        if (cachedProductIds == null) {
            cachedProductIds = Products.getAllProducts(null)
                    .then()
                    .extract()
                    .jsonPath()
                    .getList("id", Integer.class);
        }
        return cachedProductIds;
    }

    public static int getRandomProductId() {
        List<Integer> ids = getAllProductIds();
        return ids.get(new Random().nextInt(ids.size()));
    }

    // ================= CATEGORIES =================

    public static List<String> getAllCategories() {
        if (cachedCategories == null) {
            cachedCategories = Categories.getCategories(null)
                    .then()
                    .extract()
                    .jsonPath()
                    .getList("name", String.class);
        }
        return cachedCategories;
    }

    public static String getRandomCategory() {
        List<String> categories = getAllCategories();
        return categories.get(new Random().nextInt(categories.size()));
    }

    // ================= TEST SETUP =================

    public static int createTestProduct() {
        return Products.createProduct(
                        ProductTestDataFactory.validProduct(),
                        UserRole.ADMIN
                )
                .then()
                .extract()
                .path("id");
    }

    // ================= CLEANUP =================

    public static void deleteProductIfExists(int productId) {
        try {
            Products.deleteProduct(productId, UserRole.ADMIN);
        } catch (Exception ignored) {
            // Safe cleanup (avoid test failure)
        }
    }
}
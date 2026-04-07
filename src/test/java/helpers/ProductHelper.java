package helpers;

import endpoints.Categories;
import endpoints.Orders;
import endpoints.Products;
import enums.UserRole;
import io.restassured.common.mapper.TypeRef;
import payloads.response.ProductResponsePOJO;
import testData.ProductTestDataFactory;

import java.util.List;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.summary.Product;

public class ProductHelper {

    private static List<Integer> cachedProductIds;
    private static List<String> cachedCategories;
    private static List<ProductResponsePOJO> cachedProductsPayloads;

    // ================= PRODUCT IDS =================

    public static List<Integer> getAllProductIds(UserRole role) {
        if (cachedProductIds == null) {
            cachedProductIds = Products.getAllProducts(role)
                    .then()
                    .extract()
                    .jsonPath()
                    .getList("id", Integer.class);
        }
        return cachedProductIds;
    }

    public static List<ProductResponsePOJO> getAllProducts(UserRole role) {
        if (cachedProductsPayloads == null) {
            cachedProductsPayloads = Products.getAllProducts(UserRole.ADMIN)
                    .then()
                    .extract()
                    .as(new TypeRef<List<ProductResponsePOJO>>(){});
        }
        return cachedProductsPayloads;
    }

    public static int getRandomProductId(UserRole role) {
        List<Integer> ids = getAllProductIds(role);
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

    public static void deleteOrderIfExists(int productId) {
        try {
            Products.deleteProduct(productId, UserRole.ADMIN);
        } catch (Exception ignored) {}
    }

}
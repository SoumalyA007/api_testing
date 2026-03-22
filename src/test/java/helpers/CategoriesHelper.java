package helpers;

import endpoints.Categories;
import enums.UserRole;
import io.restassured.response.Response;
import payloads.request.CategoryPOJO;

public class CategoriesHelper {

    public static Response createCategory(CategoryPOJO category, UserRole role) {
        return Categories.createCategories(category, role);
    }

    public static Response createCategory(String rawJson, UserRole role) {
        return Categories.createCategories(role, rawJson);
    }

    public static void ensureCategoryExists(String name, UserRole role) {
        CategoryPOJO category = CategoryPOJO.builder()
                .name(name)
                .build();

        Categories.createCategories(category, role);
    }
}
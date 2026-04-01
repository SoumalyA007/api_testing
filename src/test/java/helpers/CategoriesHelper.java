package helpers;

import endpoints.Categories;
import enums.UserRole;
import io.restassured.response.Response;
import payloads.request.CategoryPOJO;
import payloads.response.CategoryResponsePOJO;

public class CategoriesHelper {

    public static CategoryResponsePOJO createCategory(CategoryPOJO category, UserRole role) {
        return Categories.createCategories(category, role).then().extract().as(CategoryResponsePOJO.class);
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
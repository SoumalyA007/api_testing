package helpers;

import endpoints.Categories;
import enums.UserRole;
import io.restassured.response.Response;
import payloads.request.CategoryPOJO;
import payloads.response.CategoryResponsePOJO;
import testData.CategoriesTestDataFactory;

public class CategoriesHelper {

    public static Response createCategory(String name, UserRole role) {
        CategoryPOJO category = CategoriesTestDataFactory.createCategory(name);
        return Categories.createCategories(category, role).then().extract().response();
    }

}
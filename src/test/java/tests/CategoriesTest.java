package tests;

import dataproviders.CategoriesDataProvider;
import endpoints.Categories;
import endpoints.Products;
import enums.UserRole;
import helpers.CategoriesHelper;
import org.testng.Assert;
import org.testng.annotations.Test;
import payloads.request.CategoryPOJO;
import testBase.BaseClass;
import testData.CategoriesTestDataFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;

public class CategoriesTest extends BaseClass {

    @Test(groups = {"smoke", "categories"})
    public void getAllCategories() {

        Categories.getCategories(UserRole.USER)
                .then().spec(success200())
                .body("name", notNullValue())
                .body("id", greaterThan(0));
    }

    // ✅ Create category (Data Driven)
    @Test(dataProvider = "validCategoryData", dataProviderClass = CategoriesDataProvider.class,
            groups = {"crud", "categories"})
    public void createCategories(String name, UserRole role) {

        CategoryPOJO category = CategoriesTestDataFactory.validCategory(name);

        int catergoryId = Categories.createCategories(category, role)
                .then().spec(success200())
                .body("name", equalTo(name))
                        .extract()
                                .jsonPath()
                                        .getInt("id");

        Categories.deleteCategories(catergoryId,role);
    }

    // ✅ Duplicate category test
    @Test(dataProvider = "duplicateCategoryData", dataProviderClass = CategoriesDataProvider.class,
            groups = {"negative", "categories"})
    public void uniqueCategoriesNameTest(String name, UserRole role) {

        // Ensure category already exists
        CategoriesHelper.ensureCategoryExists(name, role);

        CategoriesHelper.createCategory()

        String body = CategoriesTestDataFactory.duplicateCategoryJson(name);

        Categories.createCategories(role, body)
                .then().spec(fail409())
                .body("name", equalTo(name));
    }

    // ❌ Invalid payload test
    @Test(dataProvider = "invalidCategoryData", dataProviderClass = CategoriesDataProvider.class,
            groups = {"negative", "categories"})
    public void invalidCategoryTest(CategoryPOJO category, UserRole role) {

        Categories.createCategories(category, role)
                .then()
                .statusCode(400);
    }

    // ✅ Cross-check: Product categories vs Categories API
    @Test(groups = {"integration", "categories"})
    public void productCategoriesMatchesCategories() {

        // Get categories from products
        List<String> categoriesProduct =
                Products.getAllProducts(UserRole.USER)
                        .then()
                        .extract()
                        .jsonPath()
                        .getList("category", String.class);

        Set<String> uniqueProductCategories = new HashSet<>(categoriesProduct);

        // Get categories from categories API
        List<String> categoriesList =
                Categories.getCategories(UserRole.USER)
                        .then().spec(success200())
                        .extract()
                        .jsonPath()
                        .getList("name", String.class);

        Set<String> uniqueCategoriesList = new HashSet<>(categoriesList);

        // Compare sets (FIXED)
        Assert.assertEquals(uniqueProductCategories,
                uniqueCategoriesList,
                "Categories did not match exactly");
    }
}
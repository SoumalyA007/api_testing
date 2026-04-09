package tests;

import dataproviders.CategoriesDataProvider;
import endpoints.Categories;
import enums.UserRole;
import helpers.CategoriesHelper;
import helpers.ProductHelper;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import payloads.request.CategoryPOJO;
import payloads.response.CategoryResponsePOJO;
import testBase.BaseClass;
import testData.CategoriesTestDataFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;

public class CategoriesTest extends BaseClass {

    @Test(groups = {"smoke", "categories"}, priority = 1)
    public void getAllCategories() {

        Categories.getCategories(UserRole.USER)
                .then().spec(success200())
                .body("name", notNullValue())
                .body("id", greaterThan(0));
    }

    // ✅ Create category (Data Driven)
    @Test(
        dataProvider = "validCategoryData",
        dataProviderClass = CategoriesDataProvider.class,
        groups = {"crud", "categories"},
        priority = 2
      )
    public void createCategories(String name, UserRole role) {
        String catergoryId=null;

        try{
            CategoryPOJO category = CategoriesTestDataFactory.createCategory(name);

            catergoryId = Categories.createCategories(category, role)
                    .then().spec(success201())
                    .body("name", equalTo(name))
                    .extract()
                    .jsonPath()
                    .get("id");
        }finally {
            if (catergoryId != null) {
                Categories.deleteCategories(catergoryId,role);
            }
        }


    }

    // ✅ Duplicate category test
    @Test(
        dataProvider = "duplicateCategoryData",
        dataProviderClass = CategoriesDataProvider.class,
        groups = {"negative", "categories"},
        priority = 3
      )
    public void uniqueCategoriesNameTest(String name, UserRole role) {

        String categoriesId=null;
        try{
             Response resp=  CategoriesHelper.createCategory(name,UserRole.ADMIN);
            CategoryResponsePOJO responseCategories = resp.then().extract().as(CategoryResponsePOJO.class);

            categoriesId = responseCategories.getId();
            CategoryPOJO body = CategoriesTestDataFactory.createCategory(name);

            Categories.createCategories(body, role)
                    .then().spec(fail409());

        }finally {
            Categories.deleteCategories(categoriesId,UserRole.ADMIN);
        }

    }

    // ❌ Invalid payload test
    @Test(
        dataProvider = "invalidCategoryData",
        dataProviderClass = CategoriesDataProvider.class,
        groups = {"negative", "categories"},
        priority = 4
      )
    public void invalidCategoryTest(String name, UserRole role) {

        Response resp = CategoriesHelper.createCategory(name,role);
        resp.then().spec(fail400());

    }

    // ✅ Cross-check: Product categories vs Categories API
    @Test(groups = {"integration", "regression", "categories"}, priority = 5)
    public void productCategoriesMatchesCategories() {

        // Get categories from products
        List<String> categoriesProduct =
                ProductHelper.getAllCategories();

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

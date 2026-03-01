package tests;

import endpoints.Categories;
import endpoints.Products;
import enums.UserRole;
import org.testng.Assert;
import org.testng.annotations.Test;
import payloads.request.CategoryPOJO;
import testBase.BaseClass;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;

public class CategoriesTest extends BaseClass {

    @Test
    public void getAllCategories(){

        Categories.getCategories(UserRole.USER).then().spec(success200())
                .body("name",notNullValue())
                .body("id",greaterThan(0));

    }

    @Test
    public void createCategories(){

        CategoryPOJO categoryPOJO = CategoryPOJO.builder()
                        .name("Health and Household")
                        .build();


        Categories.createCategories(categoryPOJO , UserRole.ADMIN)
                .then().spec(success200())
                .body("name" , equalTo("Health and Household"));

    }

    @Test
    public void uniqueCategoriesNameTest(){

        String name = """
                {
                    name:"Health and Household"
                }
                """;



        Categories.createCategories( UserRole.ADMIN , name)
                .then().spec(fail409())
                .body("name" , equalTo("Health and Household"));

    }

    @Test
    public void productCategoriesMatchesCategories(){

        List<String> categoriesProduct = Products.getAllProducts(UserRole.USER).then().extract().jsonPath().getList("category", String.class);
        Set<String> uniqueCategories = new HashSet<>(categoriesProduct);

        List<String> categoriesList = Categories.getCategories(UserRole.USER).then().spec(success200())
                .extract().jsonPath().getList("name" , String.class);
        Assert.assertEquals(categoriesProduct , new HashSet<>(categoriesList),"Categories did not match exactly");

    }




}

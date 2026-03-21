package tests;

import dataproviders.CartDataProvider;
import endpoints.Carts;
import endpoints.Products;
import enums.UserRole;
import helpers.CartHelper;
import helpers.ProductHelper;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import payloads.request.CartPOJO;
import payloads.request.CartProductPOJO;
import payloads.response.CartResponsePOJO;
import testBase.BaseClass;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.*;

public class CartsTest extends BaseClass {



    @Test
    public void getAllCarts(){

        Carts.getCarts(UserRole.USER).then().spec(success200())
                .body("date",notNullValue());

    }

    @Test
    public void getCartByValidId(){

        List<Integer> cartId = Carts.getCarts(UserRole.USER).then().spec(success200())
                .extract().jsonPath().getList("id",Integer.class);
        int randId = cartId.get(0);
        Carts.getCartById(randId, UserRole.USER).then().spec(success200());

    }

    @Test(dataProvider = "createCart", dataProviderClass = CartDataProvider.class)
    public void addToCart(String message , int numberOfProducts , UserRole role , ResponseSpecification resp){

        List<CartProductPOJO> cartProductPOJOList = CartHelper.createTestCartProduct(numberOfProducts);

        CartPOJO cartPOJO = CartHelper.createTestCart(cartProductPOJOList,role);

        Carts.createCart(cartPOJO,role)
                .then()
                .spec(resp);
    }

    @Test(dataProvider = "negativeTestCart",dataProviderClass = CartDataProvider.class)
    public void negativeCartTests(String message,UserRole role,ResponseSpecification resp){

        List<CartProductPOJO> cartProductPOJOList = CartHelper.negativeTestCartProduct(message);

        CartPOJO cartPOJO = CartHelper.createTestCart(cartProductPOJOList,role);

        Carts.createCart(cartPOJO,role)
                .then()
                .spec(resp);

    }

    @Test
    public void updateCartQuantity(){

        Response resp = Carts.getCarts(UserRole.USER).then().extract().response();
        int cartId = resp.then().extract().jsonPath().getInt("[0].id");
        int quantity  = resp.then().extract().jsonPath().getInt("[0].products[0].quantity");

        CartPOJO cartPOJO = CartHelper.updateCartQuantity(cartId,UserRole.USER);

        Response response = Carts.updateCart(cartId , cartPOJO , UserRole.USER).then().extract().response();

        response.then().spec(success200())
                .body("products[0].quantity" , equalTo(quantity+1) );


    }


    @Test(dataProvider = "deleteCart", dataProviderClass = CartDataProvider.class)
    public void deleteCart(String message , int numberOfProducts , UserRole role , ResponseSpecification resp) {


        List<CartProductPOJO> cartProductPOJOList = CartHelper.createTestCartProduct(numberOfProducts);

        CartPOJO cartPOJO = CartHelper.createTestCart(cartProductPOJOList,role);

        Response createCartResponse = Carts.createCart(cartPOJO,role);

        int cartId = createCartResponse.jsonPath().getInt("id");


        Carts.deleteCart(cartId, UserRole.USER)
                .then()
                .statusCode(200);


        Carts.getCartById(cartId, UserRole.USER)
                .then()
                .statusCode(404);
    }

    @Test(dataProvider = "invalidCartId", dataProviderClass = CartDataProvider.class)
    public void deleteCartByInvalidId(UserRole role){

        int invalidId = CartHelper.getCartId(role) + 1000;

        Carts.deleteCart(invalidId, role)
                .then()
                .spec(fail404());
    }

    @Test(dataProvider = "invalidCartId", dataProviderClass = CartDataProvider.class)
    public void getCartByInvalidId(String message,UserRole role, ResponseSpecification resp){

        int invalidId = CartHelper.getCartId(role) + 1000;

        Carts.getCartById(invalidId, role)
                .then()
                .spec(resp);
    }


    @Test(dataProvider = "AccessTest", dataProviderClass = CartDataProvider.class)
    public void CaertAccessTest(String message , int numberOfProducts , UserRole role , UserRole accessedBy, ResponseSpecification resp ) {

        List<CartProductPOJO> cartProductPOJOList = CartHelper.createTestCartProduct(numberOfProducts);

        CartPOJO cartPOJO = CartHelper.createTestCart(cartProductPOJOList,role);

        Response createResp = Carts.createCart(cartPOJO, role);
        int cartId = createResp.jsonPath().getInt("id");

        // Try accessing as USER (not owner)
        Carts.getCartById(cartId, accessedBy)
                .then()
                .spec(resp);
    }


    @Test(dataProvider = "updateByAccessTest", dataProviderClass = CartDataProvider.class)
    public void UpdateCartByAccess(int numberOfProducts, UserRole updatingOf , UserRole updatingBy,ResponseSpecification resp) {

        List<CartProductPOJO> cartProductPOJOList = CartHelper.createTestCartProduct(numberOfProducts);

        CartPOJO cartPOJO = CartHelper.createTestCart(cartProductPOJOList,updatingOf);

        Response createResp = Carts.createCart(cartPOJO, updatingOf);
        int cartId = createResp.jsonPath().getInt("id");
        cartPOJO.setDate(LocalDate.now().toString());

        Carts.updateCart(cartId, cartPOJO, updatingBy)
                .then()
                .spec(resp);
    }

    @Test(dataProvider = "duplicateProductTest", dataProviderClass = CartDataProvider.class)
    public void duplicateProductShouldMergeQuantity(int numberOfProducts, UserRole role) {

        // Step 2: Create cart with duplicate product entries
        List<CartProductPOJO> cartProductPOJOList = CartHelper.createTestCartProduct(numberOfProducts);

        CartProductPOJO first = cartProductPOJOList.get(0);
        cartProductPOJOList.add(first);
        int duplicatedProductId = first.getProductId();
        int requestQty = first.getQuantity();

        CartPOJO cartPOJO = CartHelper.createTestCart(cartProductPOJOList,role);

        Response createResponse =
                Carts.createCart(cartPOJO, role);

        int cartId = createResponse.jsonPath().getInt("id");

        // Step 3: Verify quantity merged
        Carts.getCartById(cartId, UserRole.USER)
                .then()
                .statusCode(200)
                .body("products.size()", greaterThan(0))
                .body("products.find { it.productId == " + duplicatedProductId + " }.quantity",
                        equalTo(2*requestQty));
    }

    @Test(dataProvider = "numberOfCartsTest", dataProviderClass = CartDataProvider.class)
    public void userShouldHaveOnlyOneCart(int numberOfProducts , UserRole role) {


        List<CartProductPOJO> cartProductPOJOList = CartHelper.createTestCartProduct(numberOfProducts);
        CartPOJO cartPOJO = CartHelper.createTestCart(cartProductPOJOList,role);

        // First creation
        Carts.createCart(cartPOJO, role);

        CartProductPOJO first = cartPOJO.getProducts().getFirst();
        first.setQuantity(first.getQuantity()+1);
        // Second creation
        Carts.createCart(cartPOJO, role);// updated, not new

        // Verify only 1 cart exists
        Carts.getCarts(role)
                .then()
                .body("size()", equalTo(1));
    }


}

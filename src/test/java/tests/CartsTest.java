package tests;

import dataproviders.CartDataProvider;
import endpoints.Carts;
import enums.UserRole;
import helpers.CartHelper;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.Test;
import payloads.request.CartPOJO;
import payloads.request.CartProductPOJO;
import payloads.response.CartResponsePOJO;
import testBase.BaseClass;
import testData.CartTestDataFactory;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.*;

public class CartsTest extends BaseClass {



    @Test(groups = {"smoke", "carts"})
    public void shouldGetAllCarts() {
        Carts.getCarts(UserRole.USER)
                .then()
                .spec(success200())
                .body("size()", greaterThanOrEqualTo(0));
    }

    @Test(groups = {"smoke", "carts"})
    public void getCartByValidId(){

        List<Integer> cartId = Carts.getCarts(UserRole.USER).then().spec(success200())
                .extract().jsonPath().getList("id",Integer.class);
        int randId = cartId.get(0);
        Carts.getCartById(randId, UserRole.USER).then().spec(success200());

    }

    @Test(dataProvider = "createCart", dataProviderClass = CartDataProvider.class,
            groups = {"crud", "regression", "carts"})
    public void addToCart(String message , int numberOfProducts , UserRole role , ResponseSpecification resp){

        List<CartProductPOJO> products =
                CartHelper.randomProducts(numberOfProducts, role);

        CartPOJO cart = CartTestDataFactory.createTestCart(products, role);

        Carts.createCart(cart,role)
                .then()
                .spec(resp);
    }

    @Test(dataProvider = "negativeTestCart",dataProviderClass = CartDataProvider.class,
            groups = {"negative", "carts"})
    public void negativeCartTests(String message,UserRole role,ResponseSpecification resp){

        List<CartProductPOJO> products =
                CartHelper.negativeProducts(message, role);

        CartPOJO cart = CartTestDataFactory.createTestCart(products, role);

        Carts.createCart(cart,role)
                .then()
                .spec(resp);

    }

    @Test(groups = {"crud", "carts"})
    public void updateCartQuantity(){

        int cartId = Carts.createCart(
                        CartTestDataFactory.createTestCart(
                                CartHelper.randomProducts(1, UserRole.USER),
                                UserRole.USER), UserRole.USER)
                .then().extract().jsonPath().getInt("id");

        CartResponsePOJO cart =
                Carts.getCartById(cartId, UserRole.USER)
                        .then().extract().as(CartResponsePOJO.class);

        int oldQty = cart.getProducts().get(0).getQuantity();

        CartPOJO updated = CartTestDataFactory.updateCartQuantity(cart, UserRole.USER);

        Carts.updateCart(cartId, updated, UserRole.USER)
                .then()
                .spec(success200())
                .body("products[0].quantity", equalTo(oldQty + 1));


    }


    @Test(dataProvider = "deleteCart", dataProviderClass = CartDataProvider.class,
            groups = {"crud", "carts"})
    public void deleteCart(String message , int numberOfProducts , UserRole role , ResponseSpecification resp) {


        List<CartProductPOJO> products =
                CartHelper.randomProducts(numberOfProducts, role);

        CartPOJO cart = CartTestDataFactory.createTestCart(products, role);

        Response createCartResponse = Carts.createCart(cart,role);

        int cartId = createCartResponse.jsonPath().getInt("id");


        Carts.deleteCart(cartId, UserRole.USER)
                .then()
                .statusCode(200);


        Carts.getCartById(cartId, UserRole.USER)
                .then()
                .statusCode(404);
    }

    @Test(dataProvider = "invalidCartId", dataProviderClass = CartDataProvider.class,
            groups = {"negative", "carts"})
    public void deleteCartByInvalidId(UserRole role){

        int invalidId = new Random().nextInt(100000) + 99999;

        Carts.deleteCart(invalidId, role)
                .then()
                .spec(fail404());
    }

    @Test(dataProvider = "invalidCartId", dataProviderClass = CartDataProvider.class,
            groups = {"negative", "carts"})
    public void getCartByInvalidId(String message,UserRole role, ResponseSpecification resp){

        int invalidId = new Random().nextInt(100000) + 99999;

        Carts.getCartById(invalidId, role)
                .then()
                .spec(resp);
    }


    @Test(dataProvider = "AccessTest", dataProviderClass = CartDataProvider.class,
            groups = {"security", "carts"})
    public void CarrtAccessTest(String message , int numberOfProducts , UserRole role , UserRole accessedBy, ResponseSpecification resp ) {

        List<CartProductPOJO> products =
                CartHelper.randomProducts(numberOfProducts, role);

        CartPOJO cart = CartTestDataFactory.createTestCart(products, role);

        Response createResp = Carts.createCart(cart, role);
        int cartId = createResp.jsonPath().getInt("id");

        // Try accessing as USER (not owner)
        Carts.getCartById(cartId, accessedBy)
                .then()
                .spec(resp);
    }


    @Test(dataProvider = "updateByAccessTest", dataProviderClass = CartDataProvider.class,
            groups = {"security", "carts"})
    public void UpdateCartByAccess(int numberOfProducts, UserRole updatingOf , UserRole updatingBy,ResponseSpecification resp) {

        List<CartProductPOJO> products =
                CartHelper.randomProducts(numberOfProducts, updatingOf);

        CartPOJO cart = CartTestDataFactory.createTestCart(products, updatingOf);

        Response createResp = Carts.createCart(cart, updatingOf);
        int cartId = createResp.jsonPath().getInt("id");
        cart.setDate(LocalDate.now().toString());

        Carts.updateCart(cartId, cart, updatingBy)
                .then()
                .spec(resp);
    }

    @Test(dataProvider = "duplicateProductTest", dataProviderClass = CartDataProvider.class,
            groups = {"security", "carts"})
    public void duplicateProductShouldMergeQuantity(int numberOfProducts, UserRole role) {

        List<CartProductPOJO> products =
                CartHelper.randomProducts(numberOfProducts, role);

        CartProductPOJO first = products.get(0);
        products.add(first);

        int productId = first.getProductId();
        int qty = first.getQuantity();

        CartPOJO cart = CartTestDataFactory.createTestCart(products, role);

        int cartId = Carts.createCart(cart, role)
                .then()
                .extract()
                .jsonPath()
                .getInt("id");

        Carts.getCartById(cartId, role)
                .then()
                .spec(success200())
                .body("products.find { it.productId == " + productId + " }.quantity",
                        equalTo(qty * 2));
    }

    @Test(dataProvider = "numberOfCartsTest", dataProviderClass = CartDataProvider.class,
            groups = {"integration", "carts"})
    public void userShouldHaveOnlyOneCart(int numberOfProducts , UserRole role) {

        List<CartProductPOJO> products =
                CartHelper.randomProducts(numberOfProducts, role);

        CartPOJO cart = CartTestDataFactory.createTestCart(products, role);

        // First creation
        Carts.createCart(cart, role).then().spec(success200());

        // Modify quantity
        CartProductPOJO first = cart.getProducts().get(0);
        first.setQuantity(first.getQuantity() + 1);

        // Second creation (should update, not create new)
        Carts.createCart(cart, role).then().spec(success200());

        // Verify only 1 cart exists
        Carts.getCarts(role)
                .then()
                .spec(success200())
                .body("size()", equalTo(1));
    }


}

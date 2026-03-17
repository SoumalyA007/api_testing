package tests;

import endpoints.Carts;
import endpoints.Products;
import enums.UserRole;
import helpers.CartHelper;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import payloads.request.CartPOJO;
import payloads.request.CartProductPOJO;
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

    @Test
    public void addToCart(){

        List<Integer> productId = Products.getAllProducts(UserRole.USER).then().extract().jsonPath().getList("id",Integer.class);
        int randProductId = productId.get(new Random().nextInt(productId.size()-1));

        List<CartProductPOJO> cartProducts = new ArrayList<>();


        CartProductPOJO cartProductPOJO  = CartProductPOJO.builder()
                .productId(randProductId)
                .quantity(2)
                .build();

        cartProducts.add(cartProductPOJO);

        CartPOJO cartPOJO = CartPOJO.builder()
                .userId(2)
                .date(LocalDate.now().toString())
                .products(cartProducts)
                .build();

        Carts.createCart(cartPOJO,UserRole.USER)
                .then()
                .spec(success200or201());
    }


    @Test
    public void validateProductIdExists(){
        //List<Integer> productId = Products.getAllProducts(UserRole.USER).then().extract().jsonPath().getList("id",Integer.class);


        List<CartProductPOJO> cartProducts = new ArrayList<>();


        CartProductPOJO cartProductPOJO  = CartProductPOJO.builder()
                .productId(99999999)
                .quantity(2)
                .build();

        cartProducts.add(cartProductPOJO);

        CartPOJO cartPOJO = CartPOJO.builder()
                .userId(2)
                .date(LocalDate.now().toString())
                .products(cartProducts)
                .build();

        Carts.createCart(cartPOJO,UserRole.USER)
                .then()
                .spec(fail400());

    }

    @Test
    public void quantityGreaterThanZero(){

        List<CartProductPOJO> cartProducts = new ArrayList<>();

        CartProductPOJO cartProductPOJO  = CartProductPOJO.builder()
                .productId(101)
                .quantity(0)
                .build();

        cartProducts.add(cartProductPOJO);

        CartPOJO cartPOJO = CartPOJO.builder()
                .userId(2)
                .date(LocalDate.now().toString())
                .products(cartProducts)
                .build();

        Carts.createCart(cartPOJO,UserRole.USER)
                .then()
                .spec(fail400());

    }

    @Test
    public void updateCartQuantity() {

        int cartId = CartHelper.createTestCart();

        CartPOJO updatedCart = CartPOJO.builder()
                .date(LocalDate.now().toString())
                .products(List.of(
                        CartProductPOJO.builder()
                                .productId(101)
                                .quantity(3)
                                .build()
                ))
                .build();

        Carts.updateCart(cartId, updatedCart, UserRole.USER)
                .then()
                .statusCode(200)
                .body("products[0].quantity", equalTo(3));
    }


    @Test
    public void deleteCart() {


        List<CartProductPOJO> cartProducts = new ArrayList<>();

        cartProducts.add(
                CartProductPOJO.builder()
                        .productId(101)
                        .quantity(1)
                        .build()
        );

        CartPOJO cartPOJO = CartPOJO.builder()
                .userId(2)
                .date(LocalDate.now().toString())
                .products(cartProducts)
                .build();

        Response createResponse =
                Carts.createCart(cartPOJO, UserRole.USER);

        int cartId = createResponse.jsonPath().getInt("id");


        Carts.deleteCart(cartId, UserRole.USER)
                .then()
                .statusCode(200);


        Carts.getCartById(cartId, UserRole.USER)
                .then()
                .statusCode(404);
    }

    @Test
    public void deleteInvalidCart() {
        List<Integer> cartId = Carts.getCarts(UserRole.USER).then().spec(success200())
                .extract().jsonPath().getList("id",Integer.class);
        int invalidId = Collections.max(cartId) + 1000;

        Carts.deleteCart(invalidId,UserRole.USER).then().spec(fail404());

    }

    @Test
    public void getCartByInvalidId(){
        List<Integer> cartId = Carts.getCarts(UserRole.USER).then().spec(success200())
                .extract().jsonPath().getList("id",Integer.class);

        int invalidId = Collections.max(cartId) + 1000;

        Carts.getCartById(invalidId, UserRole.USER).then().spec(fail404());

    }

    @Test
    public void userCannotAccessOtherUserCart() {


        CartPOJO cartPOJO = CartPOJO.builder()
                .userId(1)
                .date(LocalDate.now().toString())
                .products(List.of(
                        CartProductPOJO.builder()
                                .productId(101)
                                .quantity(1)
                                .build()
                ))
                .build();

        Response createResp = Carts.createCart(cartPOJO, UserRole.ADMIN);
        int cartId = createResp.jsonPath().getInt("id");

        // Try accessing as USER (not owner)
        Carts.getCartById(cartId, UserRole.USER)
                .then()
                .statusCode(403);
    }


    @Test
    public void adminCanAccessAnyCart() {

        List<Integer> cartIds = Carts.getCarts(UserRole.ADMIN)
                .then()
                .extract()
                .jsonPath()
                .getList("id", Integer.class);

        int randomId = cartIds.get(new Random().nextInt(cartIds.size()));

        Carts.getCartById(randomId, UserRole.ADMIN)
                .then()
                .statusCode(200);
    }

    @Test
    public void userCannotUpdateOtherCart() {

        CartPOJO cartPOJO = CartPOJO.builder()
                .userId(5)
                .date(LocalDate.now().toString())
                .products(List.of(
                        CartProductPOJO.builder()
                                .productId(101)
                                .quantity(1)
                                .build()
                ))
                .build();

        Response createResp = Carts.createCart(cartPOJO, UserRole.ADMIN);
        int cartId = createResp.jsonPath().getInt("id");

        Carts.updateCart(cartId, cartPOJO, UserRole.USER)
                .then()
                .statusCode(403);
    }

    @Test
    public void duplicateProductShouldMergeQuantity() {

        // Step 1: Get valid product ID dynamically
        int productId = Products.getAllProducts(UserRole.USER)
                .then()
                .extract()
                .jsonPath()
                .getList("id", Integer.class)
                .get(1);

        // Step 2: Create cart with duplicate product entries
        CartPOJO cartPOJO = CartPOJO.builder()
                .date(LocalDate.now().toString())
                .products(List.of(
                        CartProductPOJO.builder()
                                .productId(productId)
                                .quantity(2)
                                .build(),
                        CartProductPOJO.builder()
                                .productId(productId)
                                .quantity(3)
                                .build()
                ))
                .build();

        Response createResponse =
                Carts.createCart(cartPOJO, UserRole.USER);

        int cartId = createResponse.jsonPath().getInt("id");

        // Step 3: Verify quantity merged
        Carts.getCartById(cartId, UserRole.USER)
                .then()
                .statusCode(200)
                .body("products.size()", greaterThan(0))
                .body("products.find { it.productId == " + productId + " }.quantity",
                        equalTo(5));
    }

    @Test
    public void userShouldHaveOnlyOneCart() {

        int productId = Products.getAllProducts(UserRole.USER)
                .then()
                .extract()
                .jsonPath()
                .getList("id", Integer.class)
                .get(0);

        CartPOJO cart = CartPOJO.builder()
                .products(List.of(
                        CartProductPOJO.builder()
                                .productId(productId)
                                .quantity(2)
                                .build()
                ))
                .build();

        // First creation
        Carts.createCart(cart, UserRole.USER)
                .then()
                .statusCode(201);

        // Second creation
        Carts.createCart(cart, UserRole.USER)
                .then()
                .statusCode(201); // updated, not new

        // Verify only 1 cart exists
        Carts.getCarts(UserRole.USER)
                .then()
                .body("size()", equalTo(1));
    }


}

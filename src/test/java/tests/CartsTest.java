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
import payloads.response.CartProductResponsePOJO;
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

        int randId = CartHelper.getRandomCartId();
        Carts.getCartById(randId, UserRole.USER).then().spec(success200());

    }

    @Test(dataProvider = "createCart", dataProviderClass = CartDataProvider.class,
            groups = {"crud", "regression", "carts"})
    public void addToCart(String message , int numberOfProducts , UserRole role , ResponseSpecification resp){

        Integer id = null;
        try{
            List<CartProductPOJO> products =
                    CartHelper.randomProducts(numberOfProducts, role);

            CartPOJO cart = CartTestDataFactory.createTestCart(products, role);

            //creation
            id = Carts.createCart(cart,role)
                    .then()
                    .spec(resp)
                    .extract()
                    .jsonPath()
                    .getInt("id");
        }finally {
            //cleanup
            if(id!=null){
                Carts.deleteCart(id,UserRole.ADMIN);
            }

        }


    }

    @Test(dataProvider = "negativeTestCart",dataProviderClass = CartDataProvider.class,
            groups = {"negative", "carts"})
    public void negativeCartTests(String message,UserRole role,ResponseSpecification resp){

        Integer cartId =  null;
        try{
                List<CartProductPOJO> products =
                CartHelper.negativeProducts(message, role);

                CartPOJO cart = CartTestDataFactory.createTestCart(products, role);

                Response response = Carts.createCart(cart,role);
                response.then().spec(resp);
                cartId = response.then().extract().jsonPath().getInt("id");

        }finally{

                if(cartId!=null){
                    Carts.deleteCart(cartId,UserRole.ADMIN);
                }
        }

    }

    @Test(groups = {"crud", "carts"})
    public void updateCartQuantity(){

        Integer cartId = null;

        try{
                //create cart
                cartId = CartHelper.createCart(1,UserRole.USER).getId();
                CartResponsePOJO cart =
                Carts.getCartById(cartId, UserRole.USER)
                        .then().extract().as(CartResponsePOJO.class);

        int oldQty = cart.getProducts().get(0).getQuantity();

        CartPOJO updated = CartTestDataFactory.updateCartQuantity(cart, UserRole.USER);

        Carts.updateCart(cartId, updated, UserRole.USER)
                .then()
                .spec(success200())
                .body("products[0].quantity", equalTo(oldQty + 1));
        }finally{
                //delete cart
                Carts.deleteCart(cartId,UserRole.ADMIN);
        }

        
    }

    @Test(dataProvider = "deleteCart", dataProviderClass = CartDataProvider.class,
            groups = {"crud", "carts"})
    public void deleteCart(String message , int numberOfProducts , UserRole role , ResponseSpecification resp) {

        int cartId = CartHelper.createCart(numberOfProducts,role).getId();

        Carts.deleteCart(cartId, UserRole.USER)
                .then()
                .spec(resp);

        Carts.getCartById(cartId, UserRole.USER)
                .then()
                .statusCode(404);
    }

    @Test(dataProvider = "invalidCartId", dataProviderClass = CartDataProvider.class,
            groups = {"negative", "carts"})
    public void deleteCartByInvalidId(UserRole role,ResponseSpecification resp){

        int invalidId = new Random().nextInt(100000) + 99999;

        Carts.deleteCart(invalidId, role)
                .then()
                .spec(resp);
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

        int cartId = CartHelper.createCart(numberOfProducts,role).getId();

        // Try accessing as USER (not owner)
        Carts.getCartById(cartId, accessedBy)
                .then()
                .spec(resp);
    }

    @Test(dataProvider = "updateByAccessTest", dataProviderClass = CartDataProvider.class,
            groups = {"security", "carts"})
    public void UpdateCartByAccess(int numberOfProducts, UserRole updatingOf , UserRole updatingBy,ResponseSpecification resp) {

        Integer cartId = null;
        try{
                CartResponsePOJO cart=CartHelper.createCart(numberOfProducts,updatingOf);
                cartId = CartHelper.createCart(numberOfProducts,updatingOf).getId();
                cart.setDate(LocalDate.now().toString());
                Carts.updateCart(cartId, cart, updatingBy)
                        .then()
                        .spec(resp);
        }finally{
                //delete cart
                if (cartId != null) {
                    Carts.deleteCart(cartId,UserRole.ADMIN);
                }
        }
        
    }

    @Test(dataProvider = "duplicateProductTest", dataProviderClass = CartDataProvider.class,
            groups = {"security", "carts"})
    public void duplicateProductShouldMergeQuantity(int numberOfProducts, UserRole role) {

        Integer cartId = null;
        try{
                List<CartProductPOJO> products =
                CartHelper.randomProducts(numberOfProducts, role);

        CartProductPOJO first = products.get(0);
        products.add(first);

        int productId = first.getProductId();
        int qty = first.getQuantity();

        CartPOJO cart = CartTestDataFactory.createTestCart(products, role);

        cartId = Carts.createCart(cart, role)
                .then()
                .extract()
                .jsonPath()
                .getInt("id");

        Carts.getCartById(cartId, role)
                .then()
                .spec(success200())
                .body("products.find { it.productId == " + productId + " }.quantity",
                        equalTo(qty * 2));
        }finally{
                //delete cart
                if (cartId != null) {
                    Carts.deleteCart(cartId,UserRole.ADMIN);
                }
        }
    }

    @Test(dataProvider = "numberOfCartsTest", dataProviderClass = CartDataProvider.class,
            groups = {"integration", "carts"})
    public void userShouldHaveOnlyOneCart(int numberOfProducts , UserRole role) {

        Integer cartId = null;
        try{
                // First creation
                CartResponsePOJO cartResponse = CartHelper.createCart(numberOfProducts, role);
        
                // Modify quantity
                CartProductResponsePOJO first = cartResponse.getProducts().get(0);
                first.setQuantity(first.getQuantity() + 1);
        
                // Second creation (should update, not create new)
                cartId = Carts.createCart(cartResponse, role).then().spec(success200())
                        .extract()
                        .jsonPath()
                        .getInt("id");
        
                // Verify only 1 cart exists
                Carts.getCarts(role)
                        .then()
                        .spec(success200())
                        .body("size()", equalTo(1));
        }
        finally{
                //delete cart
                if (cartId != null) {
                    Carts.deleteCart(cartId,UserRole.ADMIN);
                }
        }
        
    }


}

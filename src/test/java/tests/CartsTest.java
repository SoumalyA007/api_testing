package tests;

import endpoints.Carts;
import endpoints.Products;
import enums.UserRole;
import org.checkerframework.checker.units.qual.C;
import org.testng.annotations.Test;
import payloads.request.CartPOJO;
import payloads.request.CartProductPOJO;
import testBase.BaseClass;

import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.*;

public class CartsTest extends BaseClass {

    @Test
    public void getAllCarts(){

        Carts.getAllCarts(UserRole.USER).then().spec(success200())
                .body("date",notNullValue());

    }

    @Test
    public void getCartByValidId(){
        List<Integer> cartId = Carts.getAllCarts(UserRole.USER).then().spec(success200())
                .extract().jsonPath().getList("id",Integer.class);

        int randId = cartId.get(new Random().nextInt(cartId.size()));

        Carts.getCartById(randId, UserRole.USER).then().spec(success200());

    }

    @Test
    public void addToCart(){

        List<Integer> productId = Products.getAllProducts(UserRole.USER).then().extract().jsonPath().getList("id",Integer.class);
        int randProductId = productId.get(new Random().nextInt(productId.size()));


        CartProductPOJO cartProductPOJO  = CartProductPOJO.builder()
                .productId(randProductId)
                .quantity(2)
                .build();

        CartPOJO cartPOJO = CartPOJO.builder()
                .

    }






}

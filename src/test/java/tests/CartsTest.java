package tests;

import endpoints.Carts;
import endpoints.Products;
import enums.UserRole;
import org.checkerframework.checker.units.qual.C;
import org.testng.annotations.Test;
import payloads.request.CartPOJO;
import payloads.request.CartProductPOJO;
import testBase.BaseClass;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
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
        int randProductId = productId.get(new Random().nextInt(productId.size()-1));

        List<CartProductPOJO> cartProducts = new ArrayList<>();


        CartProductPOJO cartProductPOJO  = CartProductPOJO.builder()
                .productId(randProductId)
                .quantity(2)
                .build();

        cartProducts.add(cartProductPOJO);

        CartPOJO cartPOJO = CartPOJO.builder()
                .userId(1)
                .date(LocalDate.now().toString())
                .products(cartProducts)
                .build();

        Carts.createCart(cartPOJO,UserRole.USER)
                .then()
                .spec(fail403());
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


}

package helpers;

import endpoints.Carts;
import endpoints.Products;
import enums.UserRole;
import io.restassured.response.Response;
import payloads.request.CartPOJO;
import payloads.request.CartProductPOJO;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

public class CartHelper {

    public static int createTestCart() {

        CartPOJO cart = CartPOJO.builder()
                .date(LocalDate.now().toString())
                .products(List.of(
                        createTestCartProduct()
                ))
                .build();

        Response response = Carts.createCart(cart, UserRole.USER);

        return response.jsonPath().getInt("id");
    }

    public static CartProductPOJO createTestCartProduct() {

        List<Integer> productIds = Products.getAllProducts(UserRole.USER).then().extract().jsonPath().getList("id");
        int randomId = productIds.get(new Random().nextInt(productIds.size()));
        

        CartProductPOJO cartProductPOJO = CartProductPOJO.builder()
                .productId(randomId)
                .quantity(1)
                .build();


        return cartProductPOJO;



    }



}

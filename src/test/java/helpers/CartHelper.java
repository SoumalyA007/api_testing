package helpers;

import endpoints.Carts;
import endpoints.Inventory;
import endpoints.Products;
import enums.UserRole;
import io.restassured.response.Response;
import payloads.request.CartPOJO;
import payloads.request.CartProductPOJO;
import payloads.response.CartProductResponsePOJO;
import payloads.response.CartResponsePOJO;
import testBase.BaseClass;
import testData.CartTestDataFactory;
import utilities.TokenManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class CartHelper extends BaseClass {



    public static CartProductPOJO randomProduct(UserRole role) {

        List<Integer> ids = Products.getAllProducts(role)
                .then()
                .extract()
                .jsonPath()
                .getList("id");

        int productId = ids.get(new Random().nextInt(ids.size()));

        int maxQty = Inventory.getInventoryByProductId(productId, role)
                .then()
                .extract()
                .jsonPath()
                .getInt("[0].quantity");

        int qty = ThreadLocalRandom.current().nextInt(1, maxQty + 1);

        return CartProductPOJO.builder()
                .productId(productId)
                .quantity(qty)
                .build();
    }

    public static List<CartProductPOJO> randomProducts(int count, UserRole role) {
        List<CartProductPOJO> list = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            list.add(randomProduct(role));
        }

        return list;
    }

    public static List<CartProductPOJO> negativeProducts(String type, UserRole role) {

        List<CartProductPOJO> list = new ArrayList<>();

        if(type.equals("productId")){
            list.add(CartProductPOJO.builder()
                    .productId(999999999)
                    .quantity(1)
                    .build());
        }

        if(type.equals("zeroQuantity")){
            int productId = randomProduct(role).getProductId();

            list.add(CartProductPOJO.builder()
                    .productId(productId)
                    .quantity(0)
                    .build());
        }

        return list;
    }

    public static CartResponsePOJO createCart(int count , UserRole role){

        return Carts.createCart(
                CartTestDataFactory.createTestCart(
                        CartHelper.randomProducts(count, role),
                        role), role)
                .then()
                .extract()
                .as(CartResponsePOJO.class);

    }


}

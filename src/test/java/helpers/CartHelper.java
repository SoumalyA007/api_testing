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
import utilities.TokenManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class CartHelper extends BaseClass {

    public static CartPOJO createTestCart(List<CartProductPOJO> cartProducts) {

        CartPOJO cart = CartPOJO.builder()
                .date(LocalDate.now().toString())
                .products(cartProducts)
                .userId(TokenManager.getUserId(UserRole.USER))
                .build();

        return cart;

    }


    public static List<CartProductPOJO> createTestCartProduct(int noOfProduct) {

        int randomProductId=0;
        List<CartProductPOJO> cartProductPOJOList = new ArrayList<>();
        for(int i=0;i<noOfProduct;i++){
            randomProductId = randProductId();
            int productQuantity = Inventory.getInventoryByProductId(randomProductId,UserRole.ADMIN).then().spec(success200())
                    .extract()
                    .jsonPath()
                    .getInt("[0].quantity");

            System.out.println(productQuantity);

            int randomQuantity =  ThreadLocalRandom.current().nextInt(1, productQuantity + 1);

            CartProductPOJO cartProductPOJO = CartProductPOJO.builder()
                    .productId(randomProductId)
                    .quantity(randomQuantity)
                    .build();

            cartProductPOJOList.add(cartProductPOJO);

        }

        return cartProductPOJOList;


    }


    public static List<CartProductPOJO> negativeTestCartProduct(String message) {

        List<CartProductPOJO> cartProductPOJOList = new ArrayList<>();
        CartProductPOJO cartProductPOJO;
            if(message.equals("productId")){
                cartProductPOJO = CartProductPOJO.builder()
                        .productId(999999999)
                        .quantity(1)
                        .build();
                cartProductPOJOList.add(cartProductPOJO);

            }else if(message.equals("zeroQuantity")){

                int randomProductId = randProductId();
                cartProductPOJO = CartProductPOJO.builder()
                        .productId(randomProductId)
                        .quantity(0)
                        .build();
                cartProductPOJOList.add(cartProductPOJO);
        }
        return cartProductPOJOList;
    }

    public static int randProductId(){
        List<Integer> productIds = Products.getAllProducts(UserRole.USER).then().extract().jsonPath().getList("id");
        int randomProductId = productIds.get(new Random().nextInt(productIds.size()));

        return randomProductId;

    }

    public static CartPOJO updateCartQuantity(int cartId, UserRole role){

        CartResponsePOJO cartResponsePOJO =
                Carts.getCartById(cartId, role)
                        .then()
                        .extract()
                        .as(CartResponsePOJO.class);

        // Update only first product
        List<CartProductPOJO> updatedProducts = cartResponsePOJO.getProducts()
                .stream()
                .map(p -> {
                    if (p.getProductId() == cartResponsePOJO.getProducts().get(0).getProductId()) {
                        return CartProductPOJO.builder()
                                .productId(p.getProductId())
                                .quantity(p.getQuantity() + 1)
                                .build();
                    }
                    return CartProductPOJO.builder()
                            .productId(p.getProductId())
                            .quantity(p.getQuantity())
                            .build();
                })
                .toList();

        return CartPOJO.builder()
                .userId(TokenManager.getUserId(role))
                .date(LocalDate.now().toString())
                .products(updatedProducts)
                .build();
    }

}

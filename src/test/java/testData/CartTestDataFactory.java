package testData;


import endpoints.Inventory;
import endpoints.Products;
import enums.UserRole;
import payloads.request.CartPOJO;
import payloads.request.CartProductPOJO;
import payloads.response.CartResponsePOJO;
import utilities.TokenManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class CartTestDataFactory {

    public static CartPOJO createTestCart(List<CartProductPOJO> cartProducts, UserRole role) {

        CartPOJO cart = CartPOJO.builder()
                .date(LocalDate.now().toString())
                .products(cartProducts)
                .userId(TokenManager.getUserId(role))
                .build();

        return cart;

    }


    public static CartPOJO updateCartQuantity(CartResponsePOJO cart, UserRole role){

        List<CartProductPOJO> updatedProducts = cart.getProducts()
                .stream()
                .map(p -> CartProductPOJO.builder()
                        .productId(p.getProductId())
                        .quantity(p.getQuantity() + 1)
                        .build())
                .toList();

        return CartPOJO.builder()
                .userId(TokenManager.getUserId(role))
                .date(LocalDate.now().toString())
                .products(updatedProducts)
                .build();
    }

}




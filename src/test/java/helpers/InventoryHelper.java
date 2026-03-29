package helpers;

import endpoints.Inventory;
import enums.UserRole;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import payloads.response.InventoryResponsePOJO;

import java.util.List;

public class InventoryHelper {

    public static List<InventoryResponsePOJO> getAllInventory(UserRole role) {
        List<InventoryResponsePOJO> inventory = Inventory.getInventory(role).then().extract().as(new TypeRef<List<InventoryResponsePOJO>>() {
        });

        return inventory;
    }



    public static Response createInventory(String payload, UserRole role) {
        return Inventory.createInventory(payload, role);
    }

    public static int getInventoryIdByProductId(int productId){
        return Inventory.getInventoryByFiltering("productId",productId,UserRole.ADMIN).then().extract().jsonPath().getInt("id");
    }


    public static Long getInventoryIdByCreatingProduct(){
        // 1. Create product
        int productId = ProductHelper.createTestProduct();

        // 2. Get inventoryId
        Long inventoryId = InventoryHelper.getAllInventory(UserRole.ADMIN).stream()
                .filter(inv -> inv.getProductId() == productId)
                .map(InventoryResponsePOJO::getId)
                .findFirst()
                .orElse(null);

        return inventoryId;
    }

    public static InventoryResponsePOJO getInventoryById(Object inventoryId){

        return Inventory.getInventoryById(inventoryId, UserRole.ADMIN)
                .then()
                .extract()
                .as(InventoryResponsePOJO.class);
    }

    public static int getProductIdByInventoryId(Object inventoryId){

        return Inventory.getInventoryById(inventoryId,UserRole.ADMIN).then().extract().jsonPath().getInt("productId");

    }

}
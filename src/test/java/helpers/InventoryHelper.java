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
}
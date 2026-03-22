package helpers;

import endpoints.Inventory;
import enums.UserRole;
import io.restassured.response.Response;

public class InventoryHelper {

    public static Response getAllInventory(UserRole role) {
        return Inventory.getInventory(role);
    }

    public static Response createInventory(String payload, UserRole role) {
        return Inventory.createInventory(payload, role);
    }
}
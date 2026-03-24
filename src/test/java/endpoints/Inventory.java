package endpoints;

import enums.UserRole;
import io.restassured.response.Response;
import payloads.request.InventoryPOJO;
import testBase.BaseClass;

import static io.restassured.RestAssured.given;

public class Inventory {

    // ✅ GET all inventory
    public static Response getInventory(UserRole role) {
        return given()
                .spec(BaseClass.get(role))
                .basePath("/inventory")
                .when()
                .get();
    }

    // ✅ GET all inventory
    public static Response getInventory(String token) {
        return given()
                .spec(BaseClass.getWithToken(token))
                .basePath("/inventory")
                .when()
                .get();
    }


    // ✅ GET inventory by ID
    public static Response getInventoryById(int id, UserRole role) {
        return given()
                .spec(BaseClass.get(role))
                .basePath("/inventory/{id}")
                .pathParam("id", id)
                .when()
                .get();
    }

    // ✅ GET inventory by productId
    public static Response getInventoryByProductId(int productId, UserRole role) {
        return given()
                .spec(BaseClass.get(role))
                .basePath("/inventory")
                .queryParam("productId", productId)
                .when()
                .get();
    }

    // ✅ CREATE inventory (POJO)
    public static Response createInventory(InventoryPOJO inventory, UserRole role) {
        return given()
                .spec(BaseClass.get(role))
                .basePath("/inventory")
                .body(inventory)
                .when()
                .post();
    }

    // ✅ CREATE inventory (Raw JSON) 🔥 needed for negative tests
    public static Response createInventory(String payload, UserRole role) {
        return given()
                .spec(BaseClass.get(role))
                .basePath("/inventory")
                .body(payload)
                .when()
                .post();
    }

    // ✅ UPDATE inventory
    public static Response updateInventory(int id, InventoryPOJO inventory, UserRole role) {
        return given()
                .spec(BaseClass.get(role))
                .basePath("/inventory/{id}")
                .pathParam("id", id)
                .body(inventory)
                .when()
                .put();
    }

    // ✅ PATCH (optional - partial update)
    public static Response patchInventory(int id, Object payload, UserRole role) {
        return given()
                .spec(BaseClass.get(role))
                .basePath("/inventory/{id}")
                .pathParam("id", id)
                .body(payload)
                .when()
                .patch();
    }

    // ✅ DELETE inventory
    public static Response deleteInventory(int id, UserRole role) {
        return given()
                .spec(BaseClass.get(role))
                .basePath("/inventory/{id}")
                .pathParam("id", id)
                .when()
                .delete();
    }
}
package endpoints;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

import payloads.request.InventoryPOJO;
import testBase.BaseClass;

public class Inventory {

    public static Response getAllInventory(){
        return given()
                .spec(BaseClass.get())
                .basePath("/inventory")
                .when()
                .get();
    }

    public static Response getInventoryById(int id){
        return given()
                .spec(BaseClass.get())
                .basePath("/inventory/{id}")
                .pathParam("id", id)
                .when()
                .get();
    }

    public static Response updateInventory(int id, InventoryPOJO inventory){
        return given()
                .spec(BaseClass.get())
                .basePath("/inventory/{id}")
                .pathParam("id", id)
                .body(inventory)
                .when()
                .put();
    }
}

package tests;

import dataproviders.InventoryDataProvider;
import endpoints.Inventory;
import endpoints.Products;
import enums.UserRole;
import helpers.InventoryHelper;
import helpers.ProductHelper;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.testng.Assert;
import org.testng.annotations.Test;
import payloads.request.InventoryPOJO;
import payloads.response.InventoryResponsePOJO;
import payloads.response.ProductResponsePOJO;
import testBase.BaseClass;
import testData.InventoryTestDataFactory;
import utilities.TokenManager;

import java.util.*;
import java.util.stream.Collectors;
import static org.hamcrest.Matchers.*;

public class InventoryTest extends BaseClass {

    //  1. Get all inventory as Admin
    @Test
    public void getAllInventoryAsAdmin() {

        Inventory.getInventory(UserRole.ADMIN)
                .then()
                .spec(success200())
                .body("id", everyItem(greaterThan(0)))
                .body("productId", everyItem(notNullValue()))
                .body("quantity", everyItem(greaterThanOrEqualTo(0)))
                .body("warehouse", everyItem(notNullValue()));
    }

    //  2. Get all inventory as User
    @Test
    public void getAllInventoryAsUser() {

        Inventory.getInventory(UserRole.USER)
                .then()
                .spec(fail403());
    }

    //  3. Get all inventory with Invalid Token
    @Test
    public void getAllInventoryWithInvalidToken() {

        String expiredToken = TokenManager.generateExpiredToken(UserRole.ADMIN);

        Inventory.getInventory(expiredToken)
                .then()
                .spec(fail403());
    }

    // 4. Get Inventory by Valid Id
    @Test
    public void getInventoryByValidId(){

        List<InventoryResponsePOJO> inventory = InventoryHelper.getAllInventory(UserRole.ADMIN);
        Long firstId = inventory.get(0).getId();
        int firstProductId = inventory.get(0).getProductId();

        Inventory.getInventoryById(firstId,UserRole.ADMIN)
                .then()
                .spec(success200())
                .body("productId",equalTo(firstProductId))
                .body("stockCount",greaterThanOrEqualTo(0))
                .body("warehouse",greaterThanOrEqualTo(0))
                .body("minThreshold",greaterThanOrEqualTo(0));

    }

    // 5. Get Inventory by Invalid Id
    @Test(dataProvider = "invalidInventoryData",dataProviderClass = InventoryDataProvider.class)
    public void getInventoryByInvalidId(Object obj,UserRole role){

        Inventory.getInventoryById(obj,role)
                .then()
                .spec(fail404());
    }

    // 5. Get Inventory by Filtering with ProductID as query parameter
    @Test(dataProvider = "filteringInventoryData",dataProviderClass = InventoryDataProvider.class)
    public void getInventoryByFiltering(String paramKey , String value, UserRole role){

        int productId = ProductHelper.getRandomProductId(role);

        Inventory.getInventoryByFiltering(paramKey,value,role)
                .then()
                .spec(success200())
                .body(paramKey, everyItem(equalTo(value)));

    }

    // 6. Get Inventory by Filtering with invalid values as query parameter
    @Test(dataProvider = "filteringByInvalidInventoryData",dataProviderClass = InventoryDataProvider.class)
    public void getInventoryByFilteringWithInvalidValue(String paramKey , String value, UserRole role, ResponseSpecification responseStatus){

        int productId = ProductHelper.getRandomProductId(role);

        Inventory.getInventoryByFiltering(paramKey,value,role)
                .then()
                .spec(responseStatus);

    }

    //  7. Inventory should be created automatically on product creation
    @Test
    public void inventoryShouldBeCreatedAutomatically(){

        int productId = -1;
        try{
            productId = ProductHelper.createTestProduct();

            final int productFinalId = productId;

            List<InventoryResponsePOJO> inventoryList =
                    InventoryHelper.getAllInventory(UserRole.ADMIN);

            boolean found = inventoryList.stream()
                    .anyMatch(inv -> inv.getProductId() == productFinalId);

            Assert.assertTrue(found, "Inventory not created for product");
        }finally {
            int inventoryId = InventoryHelper.getInventoryIdByProductId(productId);

            if (inventoryId>0) {
                Inventory.deleteInventory(inventoryId, UserRole.ADMIN);
            }
            Products.deleteProduct(productId, UserRole.ADMIN);
        }

    }

    // 8. Creating inventory with already existing inventory of productId
    @Test(dataProvider = "createInventory",dataProviderClass = InventoryDataProvider.class)
    public void inventoryCreationOfAlreadyExistingProductId(String warehouse, int threshold, int quantity){

        boolean isPass = false;
        int inventoryId = 0;
        try{
            int productId = ProductHelper.getRandomProductId(UserRole.USER);

            System.out.println(productId);

            InventoryPOJO inventory =   InventoryTestDataFactory.validInventoryPayload(productId,warehouse,threshold,quantity);

            Response resp = Inventory.createInventory(inventory,UserRole.ADMIN).then().extract().response();

            if(resp.statusCode()==409){
                isPass = true;
            }

            resp.then().spec(fail409());

        }finally {
            if(isPass!=true){
                Inventory.deleteInventory(inventoryId , UserRole.ADMIN);
            }
        }


    }

    // 9. Creating inventory with  nonexisting  productId
    @Test(dataProvider = "createInventory",dataProviderClass = InventoryDataProvider.class)
    public void inventoryCreationOfNonProductId(int stock, String warehouse, int threshold, int quantity){

        boolean isPass = false;
        int inventoryId = 0;
        try{
            int productId = ProductHelper.getRandomProductId(UserRole.USER)+999999;

            System.out.println(productId);

            InventoryPOJO inventory =   InventoryTestDataFactory.validInventoryPayload(productId,warehouse,threshold,quantity);

            Response resp = Inventory.createInventory(inventory,UserRole.ADMIN).then().extract().response();

            if(resp.statusCode()==400){
                isPass = true;
            }

            resp.then().spec(fail400());

        }finally {
            if(isPass!=true){
                Inventory.deleteInventory(inventoryId , UserRole.ADMIN);
            }
        }


    }

    //10. Unique Inventory IDs
    @Test
    public void uniqueInventoryIdTest(){

        List<Integer> inventoryId = Inventory.getInventory(UserRole.ADMIN).then().extract().jsonPath().getList("id", Integer.class);

        Set<Integer> uniqueInventoryId = new HashSet<>(inventoryId);

        Assert.assertEquals(inventoryId.size(),uniqueInventoryId.size(),"The inventory ids are not unique");

    }

    //11. Check for productId of inventory available in inventory or not
    @Test
    public void everyProductShouldHaveInventory() {

        // 1. Get all products
        List<ProductResponsePOJO> products =
                ProductHelper.getAllProducts(UserRole.ADMIN);


        // 2. Get all inventory
        List<InventoryResponsePOJO> inventoryList =
                InventoryHelper.getAllInventory(UserRole.ADMIN);

        // 3. Convert inventory → Set of productIds
        Set<Integer> inventoryProductIds = inventoryList.stream()
                .map(InventoryResponsePOJO::getProductId)
                .collect(Collectors.toSet());

        // 4. Find missing productIds
        List<Integer> missingProductIds = products.stream()
                .map(ProductResponsePOJO::getId)
                .filter(id -> !inventoryProductIds.contains(id))
                .toList();

        // 5. Assertion
        Assert.assertTrue(missingProductIds.isEmpty(),
                "Missing inventory for productIds: " + missingProductIds);
    }

    //  12. Warehouse validation
    @Test
    public void warehouseValidationTest() {

        List<String> warehouses = Inventory.getInventory(UserRole.USER)
                .then()
                .extract()
                .jsonPath()
                .getList("warehouse", String.class);

        for (String warehouse : warehouses) {
            Assert.assertNotNull(warehouse);
            Assert.assertFalse(warehouse.trim().isEmpty());
        }
    }

    //13. Update a field of inventory data
    @Test(dataProvider = "patchInventoryData",dataProviderClass = InventoryDataProvider.class)
    public void patchInventory_dynamicFieldUpdate(String field, Object value) {

        Long inventoryId = null;
        Integer productId = null;

        try{
            // 1. Get inventoryId
            inventoryId = InventoryHelper.getInventoryIdByCreatingProduct();

            productId = InventoryHelper.getProductIdByInventoryId(inventoryId);

            Assert.assertNotNull(inventoryId, "Inventory not found");

            // 2. Create dynamic payload
            Map<String, Object> payload = new HashMap<>();
            payload.put(field, value);

            // 3. PATCH call
            Response response = Inventory.patchInventory(inventoryId, payload, UserRole.ADMIN);

            response.then().statusCode(200);

            // 4. Get updated inventory
            InventoryResponsePOJO updated =
                    InventoryHelper.getInventoryById(inventoryId);

            // 6. Dynamic assertion
            switch (field) {
                case "quantity":
                    Assert.assertEquals(updated.getQuantity(), value);
                    break;

                case "threshold":
                    Assert.assertEquals(updated.getMinThreshold(), value);
                    break;

                case "warehouse":
                    Assert.assertEquals(updated.getWarehouse(), value);
                    break;

                default:
                    Assert.fail("Unknown field: " + field);
            }
        }finally {
            Inventory.deleteInventory(inventoryId, UserRole.ADMIN);
            Products.deleteProduct(productId,UserRole.ADMIN);
        }


    }



    //13. Delete inventory ( data provider to handle positive and negative tests in a single method
    @Test(dataProvider = "deleteInventory",dataProviderClass = InventoryDataProvider.class)
    public void deleteInventory(UserRole role, ResponseSpecification resp){

        Long inventoryId = InventoryHelper.getInventoryIdByCreatingProduct();

        Inventory.deleteInventory(inventoryId , UserRole.ADMIN).then().spec(resp);

    }



}
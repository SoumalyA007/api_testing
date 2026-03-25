package tests;

import dataproviders.InventoryDataProvider;
import endpoints.Inventory;
import endpoints.Products;
import enums.UserRole;
import helpers.InventoryHelper;
import helpers.ProductHelper;
import org.testng.Assert;
import org.testng.annotations.Test;
import payloads.response.InventoryResponsePOJO;
import testBase.BaseClass;
import testData.InventoryTestDataFactory;
import utilities.TokenManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                .body("stockCount", everyItem(greaterThanOrEqualTo(0)))
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
        int firstId = inventory.get(0).getId();
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
    public void getInventoryByFilteringByProductId(String paramKey , String value, UserRole role){

        int productId = ProductHelper.getRandomProductId();

        Inventory.getInventoryByFiltering(paramKey,value,role)
                .then()
                .spec(success200())
                .body(paramKey, everyItem(equalTo(value)));

    }




    //  4. Create inventory (Data Driven + Cleanup)
//    @Test(dataProvider = "validInventoryData", dataProviderClass = InventoryDataProvider.class)
//    public void createInventoryTest(int stock, String warehouse,
//                                    int threshold, int quantity, UserRole role) {
//
//        int productId = ProductHelper.createTestProduct();
//        Integer inventoryId = null;
//
//        try {
//            String payload = InventoryTestDataFactory
//                    .validInventoryJson(productId, stock, warehouse, threshold, quantity);
//
//            var json = InventoryHelper.createInventory(payload, role)
//                    .then()
//                    .spec(success200())
//                    .body("productId", equalTo(productId))
//                    .body("stockCount", equalTo(stock))
//                    .body("quantity", equalTo(quantity))
//                    .body("warehouse", equalTo(warehouse))
//                    .extract()
//                    .jsonPath();
//
//            inventoryId = json.getInt("id");
//
//        } finally {
//            // 🔥 Cleanup
//            if (inventoryId != null) {
//                InventoryHelper.deleteInventory(inventoryId, role);
//            }
//            Products.deleteProduct(productId, role);
//        }
//    }


    //  4. Quantity exceeds stock (with cleanup)
    @Test(dataProvider = "exceedStockData", dataProviderClass = InventoryDataProvider.class)
    public void quantityExceedsStockTest(UserRole role) {

        int productId = ProductHelper.createTestProduct();

        try {
            String payload = InventoryTestDataFactory.quantityExceedsStockJson(productId);

            InventoryHelper.createInventory(payload, role)
                    .then()
                    .statusCode(400);

        } finally {
            // 🔥 Cleanup
            Products.deleteProduct(productId, role);
        }
    }

    //  5. Unique Inventory IDs
    @Test
    public void uniqueInventoryIdTest() {

        List<Integer> ids = InventoryHelper.getAllInventory(UserRole.USER)
                .then()
                .extract()
                .jsonPath()
                .getList("id", Integer.class);

        Set<Integer> uniqueIds = new HashSet<>(ids);

        Assert.assertEquals(ids.size(), uniqueIds.size(),
                "Duplicate inventory IDs found");
    }

    //  6. Quantity <= Stock validation (optimized)
    @Test
    public void quantityLessThanStockTest() {

        var json = InventoryHelper.getAllInventory(UserRole.USER)
                .then()
                .extract()
                .jsonPath();

        List<Integer> stockList = json.getList("stockCount", Integer.class);
        List<Integer> quantityList = json.getList("quantity", Integer.class);

        for (int i = 0; i < stockList.size(); i++) {
            Assert.assertTrue(quantityList.get(i) <= stockList.get(i),
                    "Quantity exceeds stock at index: " + i);
        }
    }

    //  7. Product mapping validation
    @Test
    public void productMappingTest() {

        List<Integer> inventoryProductIds = InventoryHelper.getAllInventory(UserRole.USER)
                .then()
                .extract()
                .jsonPath()
                .getList("productId", Integer.class);

        List<Integer> productIds = Products.getAllProducts(UserRole.USER)
                .then()
                .extract()
                .jsonPath()
                .getList("id", Integer.class);

        Set<Integer> productSet = new HashSet<>(productIds);

        for (Integer productId : inventoryProductIds) {
            Assert.assertTrue(productSet.contains(productId),
                    "Invalid productId in inventory: " + productId);
        }
    }

    //  8. Warehouse validation
    @Test
    public void warehouseValidationTest() {

        List<String> warehouses = InventoryHelper.getAllInventory(UserRole.USER)
                .then()
                .extract()
                .jsonPath()
                .getList("warehouse", String.class);

        for (String warehouse : warehouses) {
            Assert.assertNotNull(warehouse);
            Assert.assertFalse(warehouse.trim().isEmpty());
        }
    }
}
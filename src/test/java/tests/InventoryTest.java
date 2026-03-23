package tests;

import dataproviders.InventoryDataProvider;
import endpoints.Products;
import enums.UserRole;
import helpers.InventoryHelper;
import org.testng.Assert;
import org.testng.annotations.Test;
import testBase.BaseClass;
import testData.InventoryTestDataFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;

public class InventoryTest extends BaseClass {

    //  1. Get all inventory
    @Test
    public void getAllInventoryTest() {

        InventoryHelper.getAllInventory(UserRole.ADMIN)
                .then()
                .spec(success200())
                .body("id", everyItem(greaterThan(0)))
                .body("productId", everyItem(notNullValue()))
                .body("stockCount", everyItem(greaterThanOrEqualTo(0)))
                .body("quantity", everyItem(greaterThanOrEqualTo(0)))
                .body("warehouse", everyItem(notNullValue()));
    }

    //  2. Create inventory (Data Driven)
    @Test(dataProvider = "validInventoryData", dataProviderClass = InventoryDataProvider.class)
    public void createInventoryTest(int productId, int stock, String warehouse,
                                    int threshold, int quantity, UserRole role) {

        String payload = InventoryTestDataFactory
                .validInventoryJson(productId, stock, warehouse, threshold, quantity);

        InventoryHelper.createInventory(payload, role)
                .then()
                .spec(success200())
                .body("productId", equalTo(productId));
    }

    //  3. Invalid payload
    @Test(dataProvider = "invalidInventoryData", dataProviderClass = InventoryDataProvider.class)
    public void invalidInventoryTest(UserRole role) {

        String payload = InventoryTestDataFactory.invalidInventoryJson();

        InventoryHelper.createInventory(payload, role)
                .then()
                .statusCode(400);
    }

    //  4. Quantity exceeds stock
    @Test(dataProvider = "exceedStockData", dataProviderClass = InventoryDataProvider.class)
    public void quantityExceedsStockTest(int productId, UserRole role) {

        String payload = InventoryTestDataFactory.quantityExceedsStockJson(productId);

        InventoryHelper.createInventory(payload, role)
                .then()
                .statusCode(400);
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

    //  6. Quantity <= Stock validation
    @Test
    public void quantityLessThanStockTest() {

        var response = InventoryHelper.getAllInventory(UserRole.USER);

        List<Integer> stockList = response.then()
                .extract()
                .jsonPath()
                .getList("stockCount", Integer.class);

        List<Integer> quantityList = response.then()
                .extract()
                .jsonPath()
                .getList("quantity", Integer.class);

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
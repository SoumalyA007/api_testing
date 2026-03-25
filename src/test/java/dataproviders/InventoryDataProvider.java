package dataproviders;

import enums.UserRole;
import org.testng.annotations.DataProvider;
import utilities.ProductIdGenerator;

public class InventoryDataProvider {

    @DataProvider(name = "validInventoryData")
    public Object[][] validInventoryData() {
        return new Object[][]{
                {ProductIdGenerator.getUniqueProductId(), 50, "Virtual", 5, 10, UserRole.ADMIN},
                {ProductIdGenerator.getUniqueProductId(), 100, "North-Zone", 10, 20, UserRole.ADMIN}
        };
    }

    @DataProvider(name = "invalidInventoryData")
    public Object[][] invalidInventoryData() {
        return new Object[][]{
                {99999999,UserRole.ADMIN},
                {0,UserRole.ADMIN},
                {-999999,UserRole.ADMIN},
                {"99999",UserRole.ADMIN}
        };
    }

    @DataProvider(name = "filteringInventoryData")
    public Object[][] filteringInventoryData() {
        return new Object[][]{
                {"productId",101,UserRole.ADMIN},
                {"warehouse","Virtual",UserRole.ADMIN},
                {"warehouse","North-Zone",UserRole.ADMIN}
        };
    }

    @DataProvider(name = "exceedStockData")
    public Object[][] exceedStockData() {
        return new Object[][]{
                {ProductIdGenerator.getUniqueProductId(), UserRole.ADMIN}
        };
    }
}
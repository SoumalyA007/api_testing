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
                {UserRole.ADMIN}
        };
    }

    @DataProvider(name = "exceedStockData")
    public Object[][] exceedStockData() {
        return new Object[][]{
                {ProductIdGenerator.getUniqueProductId(), UserRole.ADMIN}
        };
    }
}
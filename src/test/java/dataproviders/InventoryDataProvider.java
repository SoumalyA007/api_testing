package dataproviders;

import enums.UserRole;
import org.testng.annotations.DataProvider;

public class InventoryDataProvider {

    @DataProvider(name = "validInventoryData")
    public Object[][] validInventoryData() {
        return new Object[][]{
                {101, 50, "Virtual", 5, 10, UserRole.ADMIN},
                {102, 100, "North-Zone", 10, 20, UserRole.ADMIN}
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
                {103, UserRole.ADMIN}
        };
    }
}
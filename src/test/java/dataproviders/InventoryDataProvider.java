package dataproviders;

import com.github.javafaker.Faker;
import enums.UserRole;
import org.testng.annotations.DataProvider;

public class InventoryDataProvider {

    private static final Faker faker = new Faker();
    @DataProvider(name = "validInventoryData")
    public Object[][] validInventoryData() {
        return new Object[][]{
                {(int)faker.number().randomNumber(), 50, "Virtual", 5, 10, UserRole.ADMIN},
                {(int)faker.number().randomNumber(), 100, "North-Zone", 10, 20, UserRole.ADMIN}
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
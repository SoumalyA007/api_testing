package dataproviders;

import enums.UserRole;
import org.testng.annotations.DataProvider;
import testBase.BaseClass;
import utilities.ProductIdGenerator;

public class InventoryDataProvider {



    @DataProvider(name = "validInventoryData")
    public Object[][] validInventoryData() {
        return new Object[][]{
                {ProductIdGenerator.getUniqueProductId(),"Virtual", 5, 99, UserRole.ADMIN},
                {ProductIdGenerator.getUniqueProductId(),"North-Zone", 10, 90, UserRole.ADMIN}
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

    @DataProvider(name = "filteringByInvalidInventoryData")
    public Object[][] filteringByInvalidInventoryData() {
        return new Object[][]{
                {"productId",1010121021,UserRole.ADMIN, BaseClass.fail404()},
                {"productId","101",UserRole.ADMIN, BaseClass.fail404()},
                {"warehouse","Physical",UserRole.ADMIN,BaseClass.fail404()},
                {"warehouseee","Virtual",UserRole.ADMIN,BaseClass.fail400()},
        };
    }

    @DataProvider(name = "createInventory")
    public Object[][] createInventory(){
        return new Object[][]{
                {"South-West",5,90}

        };

    }


    @DataProvider(name = "exceedStockData")
    public Object[][] exceedStockData() {
        return new Object[][]{
                {ProductIdGenerator.getUniqueProductId(), UserRole.ADMIN}
        };
    }

    @DataProvider(name = "patchInventoryData")
    public Object[][] patchInventoryData() {
        return new Object[][]{
                {"quantity", 200},
                {"minThreshold", 50},
                {"warehouse", "B2"}
        };
    }

    @DataProvider(name = "deleteInventory")
    public Object[][] deleteInventory() {
        return new Object[][]{
                {UserRole.ADMIN,BaseClass.success200()},
                {UserRole.USER,BaseClass.fail403()}
        };
    }

}
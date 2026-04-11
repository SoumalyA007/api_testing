package dataproviders;

import enums.UserRole;
import org.testng.annotations.DataProvider;
import testBase.BaseClass;

public class CartDataProvider {

    @DataProvider(name = "createCart",parallel = true)
    public Object[][] createCart(){

        return new Object[][]{
                {"Add 1 product in cart", 1, UserRole.ADMIN, BaseClass.success200or201()},
                {"Add 2 product in cart", 2, UserRole.ADMIN, BaseClass.success200or201()},
                {"Add 3 product in cart", 3, UserRole.ADMIN, BaseClass.success200or201()}

        };


    }

    @DataProvider(name = "negativeTestCart",parallel = true)
    public Object[][] negativeTestCart(){

        return new Object[][]{
                {"productId",  UserRole.ADMIN, BaseClass.fail400()},
                {"zeroQuantity", UserRole.ADMIN, BaseClass.fail400()}

        };
    }

    @DataProvider(name = "deleteCart",parallel = true)
    public Object[][] deleteCart(){

        return new Object[][]{
                {"delete 1 product in cart", 1, UserRole.ADMIN, BaseClass.success200()},
                {"delete 2 product in cart", 2, UserRole.ADMIN, BaseClass.success200()}
        };

    }

    @DataProvider(name = "invalidCartId",parallel = true)
    public Object[][] invalidCartId(){

        return new Object[][]{
                {"1st Test", UserRole.ADMIN, BaseClass.fail404()},
                {"2nd Test", UserRole.ADMIN, BaseClass.fail404()}
        };

    }

    @DataProvider(name = "AccessTest",parallel = true)
    public Object[][] unauthorisedAccess(){

        return new Object[][]{
                {"Accesing as User", 1, UserRole.ADMIN, UserRole.USER, BaseClass.fail403()},
                {"Accessing as Admin", 1, UserRole.USER, UserRole.ADMIN, BaseClass.success200()}
        };

    }

    @DataProvider(name = "updateByAccessTest",parallel = true)
    public Object[][] updateByAccessTest(){

        return new Object[][]{
                { 1, UserRole.ADMIN, UserRole.USER, BaseClass.fail403()},
                { 2, UserRole.USER, UserRole.ADMIN, BaseClass.success200()}
        };

    }

    @DataProvider(name = "duplicateProductTest",parallel = true)
    public Object[][] duplicateProductTest(){

        return new Object[][]{
                { 1,UserRole.USER },
                { 2, UserRole.USER }
        };

    }

    @DataProvider(name = "numberOfCartsTest",parallel = true)
    public Object[][] numberOfCartsTest(){

        return new Object[][]{
                { 1,UserRole.USER },
                { 1, UserRole.USER }
        };

    }

}

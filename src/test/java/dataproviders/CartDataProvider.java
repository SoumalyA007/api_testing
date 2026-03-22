package dataproviders;

import enums.UserRole;
import helpers.CartHelper;
import org.testng.annotations.DataProvider;
import testBase.BaseClass;

public class CartDataProvider {

    @DataProvider(name = "createCart")
    public Object[][] createCart(){

        return new Object[][]{
                {"Add 1 product in cart", 1, UserRole.ADMIN, BaseClass.success200or201()},
                {"Add 2 product in cart", 2, UserRole.ADMIN, BaseClass.success200or201()},
                {"Add 3 product in cart", 3, UserRole.ADMIN, BaseClass.success200or201()}

        };


    }

    @DataProvider(name = "negativeTestCart")
    public Object[][] negativeTestCart(){

        return new Object[][]{
                {"productId",  UserRole.ADMIN, BaseClass.fail400()},
                {"zeroQuantity", UserRole.ADMIN, BaseClass.fail400()}

        };
    }

    @DataProvider(name = "deleteCart")
    public Object[][] deleteCart(){

        return new Object[][]{
                {"Add 1 product in cart", 1, UserRole.ADMIN, BaseClass.success200or201()},
                {"Add 2 product in cart", 2, UserRole.ADMIN, BaseClass.success200or201()}
        };

    }

    @DataProvider(name = "invalidCartId")
    public Object[][] invalidCartId(){

        return new Object[][]{
                {"1st Test", UserRole.ADMIN, BaseClass.fail404()},
                {"2nd Test", UserRole.ADMIN, BaseClass.fail404()}
        };

    }

    @DataProvider(name = "AccessTest")
    public Object[][] unauthorisedAccess(){

        return new Object[][]{
                {"Accesing as User", 1, UserRole.ADMIN, UserRole.USER, BaseClass.fail403()},
                {"Accessing as Admin", 1, UserRole.USER, UserRole.ADMIN, BaseClass.success200()}
        };

    }

    @DataProvider(name = "updateByAccessTest")
    public Object[][] updateByAccessTest(){

        return new Object[][]{
                { 1, UserRole.ADMIN, UserRole.USER, BaseClass.fail403()},
                { 2, UserRole.USER, UserRole.ADMIN, BaseClass.success200()}
        };

    }

    @DataProvider(name = "duplicateProductTest")
    public Object[][] duplicateProductTest(){

        return new Object[][]{
                { 1,UserRole.USER },
                { 2, UserRole.USER }
        };

    }

    @DataProvider(name = "numberOfCartsTest")
    public Object[][] numberOfCartsTest(){

        return new Object[][]{
                { 1,UserRole.USER },
                { 1, UserRole.USER }
        };

    }

}

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


}

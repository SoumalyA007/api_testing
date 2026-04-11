package dataproviders;

import enums.UserRole;
import org.testng.annotations.DataProvider;

public class CategoriesDataProvider {

    @DataProvider(name = "validCategoryData",parallel = true)
    public Object[][] validCategoryData() {
        return new Object[][]{
                {"Health and Household", UserRole.ADMIN},
                {"Electronics", UserRole.ADMIN},
                {"Groceries", UserRole.ADMIN}
        };
    }

    @DataProvider(name = "duplicateCategoryData",parallel = true)
    public Object[][] duplicateCategoryData() {
        return new Object[][]{
                {"Health and Household", UserRole.ADMIN}
        };
    }

    @DataProvider(name = "invalidCategoryData",parallel = true)
    public Object[][] invalidCategoryData() {
        return new Object[][]{
                {"", UserRole.ADMIN},
                {null, UserRole.ADMIN}
        };
    }
}
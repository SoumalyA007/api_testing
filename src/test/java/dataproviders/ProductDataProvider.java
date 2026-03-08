package dataproviders;

import helpers.ProductHelper;
import org.testng.annotations.DataProvider;

public class ProductDataProvider {

    @DataProvider(name = "invalidProductPayloads")
    public Object[][] invalidProductPayloads() {
        return new Object[][]{

                {ProductHelper.negativePriceProduct()},
                {ProductHelper.productWithoutTitle()}

        };
    }

}
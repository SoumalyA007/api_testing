package dataproviders;

import enums.UserRole;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.DataProvider;
import payloads.request.OrderItemPOJO;
import payloads.request.OrderPOJO;
import testBase.BaseClass;
import utilities.TokenManager;

import java.util.List;

public class OrdersDataProvider {

    @DataProvider(name = "invalidOrderPayloads")
    public Object[][] invalidOrderPayloads() {

        int userId = TokenManager.getUserId(UserRole.USER);

        ResponseSpecification spec = BaseClass.fail400();

        return new Object[][]{

                {"Negative Quantity",
                        OrderPOJO.builder()
                                .userId(userId)
                                .items(List.of(new OrderItemPOJO(101, -1)))
                                .build(),
                        spec
                },

                {"Invalid Product ID",
                        OrderPOJO.builder()
                                .userId(userId)
                                .items(List.of(new OrderItemPOJO(Integer.MAX_VALUE, 1)))
                                .build(),
                        spec
                },

                {"Negative Product ID",
                        OrderPOJO.builder()
                                .userId(userId)
                                .items(List.of(new OrderItemPOJO(-100, 1)))
                                .build(),
                        spec
                },

                {"Empty Item List",
                        OrderPOJO.builder()
                                .userId(userId)
                                .build(),
                        spec
                }
        };
    }




}

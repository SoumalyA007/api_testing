package dataproviders;

import com.github.javafaker.Faker;
import org.testng.annotations.DataProvider;
import testBase.BaseClass;

public class UserDataProvider extends BaseClass {

    private static final Faker faker = new Faker();

    @DataProvider(name = "createUserData")
    public Object[][] createUserData(){

        int numberOfUsers = Integer.parseInt(p.getProperty("numberOfCreateUser"));   // number of datasets you want

        Object[][] data = new Object[numberOfUsers][5];
        for(int i = 0; i < numberOfUsers; i++){

            data[i][0] = faker.name().firstName();
            data[i][1] = faker.name().lastName();
            data[i][2] = faker.internet().emailAddress();
            data[i][3] = faker.name().username();
            data[i][4] = faker.internet().password(8,12,true,true);

        }

        return data;
    }

    @DataProvider(name = "updateUserFields")
    public Object[][] updateUserFields(){

        return new Object[][]{

                {"firstname",faker.name().firstName(), faker.name().firstName(),faker.name().lastName(),faker.internet().emailAddress(),faker.name().username(),faker.internet().password(8,12,true,true)},
                {"lastname",faker.name().lastName(),faker.name().firstName(),faker.name().lastName(),faker.internet().emailAddress(),faker.name().username(),faker.internet().password(8,12,true,true)},
                {"email", faker.internet().emailAddress(),faker.name().firstName(),faker.name().lastName(),faker.internet().emailAddress(),faker.name().username(),faker.internet().password(8,12,true,true)},
                {"username",faker.name().username(), faker.name().firstName(),faker.name().lastName(),faker.internet().emailAddress(),faker.name().username(),faker.internet().password(8,12,true,true)},
                {"password",faker.internet().password(8,12,true,true), faker.name().firstName(),faker.name().lastName(),faker.internet().emailAddress(),faker.name().username(),faker.internet().password(8,12,true,true)}

        };
    }


}

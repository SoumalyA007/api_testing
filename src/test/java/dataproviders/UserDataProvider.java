package dataproviders;

import com.github.javafaker.Faker;
import enums.UserRole;
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

    @DataProvider(name = "deleteUserFields")
    public Object[][] deleteUserFields(){

        return new Object[][]{

                {faker.name().firstName(),faker.name().lastName(),faker.internet().emailAddress(),faker.name().username(),faker.internet().password(8,12,true,true), UserRole.ADMIN,BaseClass.success200()},
                {faker.name().firstName(),faker.name().lastName(),faker.internet().emailAddress(),faker.name().username(),faker.internet().password(8,12,true,true), UserRole.USER,BaseClass.fail403()},

        };
    }

    @DataProvider(name = "deleteUserByInvalidIdFields")
    public Object[][] deleteUserByInvalidIdFields(){

        return new Object[][]{

                {faker.name().firstName(),faker.name().lastName(),faker.internet().emailAddress(),faker.name().username(),faker.internet().password(8,12,true,true),BaseClass.fail404()},
                {faker.name().firstName(),faker.name().lastName(),faker.internet().emailAddress(),faker.name().username(),faker.internet().password(8,12,true,true),BaseClass.fail404()},

        };
    }

    @DataProvider(name = "createWithOutEmailField")
    public Object[][] createUserWithoutEmailField(){

        return new Object[][]{

                {faker.name().firstName(),faker.name().lastName(),faker.name().username(),faker.internet().password(8,12,true,true), UserRole.ADMIN,BaseClass.success200()},
                {faker.name().firstName(),faker.name().lastName(),faker.name().username(),faker.internet().password(8,12,true,true), UserRole.USER,BaseClass.fail403()},

        };
    }

    @DataProvider(name = "validateUserData")
    public Object[][] validateUserData(){

        return new Object[][]{

                {"invalid email", faker.name().firstName(),faker.name().lastName(),faker.internet().emailAddress().replace("@","a"),faker.name().username(),faker.internet().password(8,12,true,true),UserRole.ADMIN,BaseClass.fail400()},
                {"invalid password",faker.name().firstName(),faker.name().lastName(),faker.internet().emailAddress(),faker.name().username(),faker.internet().password(8,12,true,true).substring(0,3),UserRole.ADMIN, BaseClass.fail400()},
                {"unauthorized role",faker.name().firstName(),faker.name().lastName(),faker.internet().emailAddress(),faker.name().username(),faker.internet().password(8,12,true,true),UserRole.MANAGER ,  BaseClass.fail403()},
                {"negative id",faker.name().firstName(),faker.name().lastName(),faker.internet().emailAddress(),faker.name().username(),faker.internet().password(8,12,true,true),UserRole.ADMIN,BaseClass.fail400()}

        };
    }

    @DataProvider(name = "deleteUserData")
    public Object[][] deleteUserData(){

        return new Object[][]{

                { faker.name().firstName(),faker.name().lastName(),faker.internet().emailAddress(),faker.name().username(),faker.internet().password(8,12,true,true),UserRole.ADMIN,BaseClass.fail400()},
                {faker.name().firstName(),faker.name().lastName(),faker.internet().emailAddress(),faker.name().username(),faker.internet().password(8,12,true,true),UserRole.ADMIN, BaseClass.fail400()},

        };
    }





}

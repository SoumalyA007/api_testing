package testBase;

import enums.UserRole;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.*;
import utilities.*;
import static org.hamcrest.Matchers.*;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
public class BaseClass {
    public static Properties p = new Properties();
    public Logger logger = LogManager.getLogger(this.getClass());

    static {
        try {
            FileReader file = new FileReader("./src/test/resources/config.properties");
            p.load(file);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to load config.properties", e);
        }
    }

    public static RequestSpecification get(UserRole role){

        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(p.getProperty("baseURI"))
                .addHeader("Content-Type","application/json")
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter());

        if (role != null) {
            builder.addHeader("Authorization",
                    "Bearer " + TokenManager.getToken(role));
        }
        builder.addHeaders(TestContext.getHeaders());

        return builder.build();
    }

    public static RequestSpecification getWithToken(String token){

        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(p.getProperty("baseURI"))
                .addHeader("Content-Type","application/json")
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter());

        if (token != null) {
            builder.addHeader("Authorization", "Bearer " + token);
        }

        builder.addHeaders(TestContext.getHeaders());

        return builder.build();
    }


    public static ResponseSpecification success200(){

        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectHeader("Content-Type","application/json; charset=utf-8")
                .build();

    }

    public static ResponseSpecification success201(){
        return new ResponseSpecBuilder()
                .expectStatusCode(201)
                .expectHeader("Content-Type","application/json; charset=utf-8")
                .build();


    }

    public static ResponseSpecification success200or201(){
        return new ResponseSpecBuilder()
                .expectHeader("Content-Type","application/json; charset=utf-8")
                .expectStatusCode(anyOf(is(200), is(201)))
                .build();
    }

    public static ResponseSpecification fail400(){

        return new ResponseSpecBuilder()
                .expectStatusCode(400)
                .build();
    }

    public static ResponseSpecification fail401(){

        return new ResponseSpecBuilder()
                .expectStatusCode(401)
                .build();
    }

    public static ResponseSpecification fail403(){

        return new ResponseSpecBuilder()
                .expectStatusCode(403)
                .build();
    }

    public static ResponseSpecification fail404(){

        return new ResponseSpecBuilder()
                .expectStatusCode(404)
                .build();
    }
    public static ResponseSpecification fail415(){

        return new ResponseSpecBuilder()
                .expectStatusCode(415)
                .build();
    }

    public static ResponseSpecification fail409(){

        return new ResponseSpecBuilder()
                .expectStatusCode(409)
                .build();
    }




    @AfterMethod(alwaysRun = true)
    public void cleanUp(){
        TestContext.clearHeaders();
        TestContext.clear();
        TokenManager.clear(); // 🔥 THIS IS CRITICAL
        TestContext.remove();
    }

}

package testBase;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.BeforeClass;
import utilities.*;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;


public class BaseClass {
    public static Properties p;


    @BeforeClass
    public void setUp() throws IOException {

        FileReader file = new FileReader("./src//test//resources//config.properties");
        p = new Properties();
        p.load(file);


    }

    public static RequestSpecification get(){
        return new RequestSpecBuilder()
                .setBaseUri(p.getProperty("baseURI"))
                .addHeader("Content-Type","application/json")
                .addHeader("Authorization",
                        TokenManager.getToken() == null ? "" :
                                "Bearer " + TokenManager.getToken())
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
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

}

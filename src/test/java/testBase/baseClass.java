package testBase;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.BeforeClass;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;


public class baseClass {
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
                .addHeader("contentType","application/json")
                .build();
    }


    public static ResponseSpecification success200(){

        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectHeader("contentType","application/json")
                .build();

    }

    public static ResponseSpecification success201(){
        return new ResponseSpecBuilder()
                .expectStatusCode(201)
                .expectHeader("contentType","application/json")
                .build();


    }

    public static ResponseSpecification fail400(){

        return new ResponseSpecBuilder()
                .expectStatusCode(400)
                .build();
    }




}

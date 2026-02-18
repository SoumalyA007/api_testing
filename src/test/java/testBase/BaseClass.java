package testBase;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.BeforeClass;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;




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




}

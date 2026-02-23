package utilities;


import io.restassured.response.Response;

import java.util.function.Supplier;

public class RetryUtil {

    public static Response retry(Supplier<Response> request, int maxAttempts){

        int attempt = 0;
        Response response = null;

        while(attempt < maxAttempts){
            response = request.get();

            if(response.statusCode() < 500){
                return response;
            }

            attempt++;
        }

        return response;
    }
}

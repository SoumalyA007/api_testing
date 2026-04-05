package testData;

import payloads.request.CategoryPOJO;

public class CategoriesTestDataFactory {

    public static CategoryPOJO createCategory(String name) {
        return CategoryPOJO.builder()
                .name(name)
                .build();
    }

}
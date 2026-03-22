package testData;

import payloads.request.CategoryPOJO;

public class CategoriesTestDataFactory {

    public static CategoryPOJO validCategory(String name) {
        return CategoryPOJO.builder()
                .name(name)
                .build();
    }

    public static String duplicateCategoryJson(String name) {
        return String.format("""
                {
                    "name": "%s"
                }
                """, name);
    }

    public static CategoryPOJO emptyNameCategory() {
        return CategoryPOJO.builder()
                .name("")
                .build();
    }

    public static CategoryPOJO nullNameCategory() {
        return CategoryPOJO.builder()
                .name(null)
                .build();
    }
}
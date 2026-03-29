package testData;

import payloads.request.InventoryPOJO;

public class InventoryTestDataFactory {

    public static String validInventoryJson(int productId, String warehouse, int threshold, int quantity) {
        return String.format("""
                {
                    "productId": %d,
                    "warehouse": "%s",
                    "minThreshold": %d,
                    "quantity": %d
                }
                """, productId, warehouse, threshold, quantity);
    }


    public static InventoryPOJO validInventoryPayload(int productId, String warehouse, int threshold, int quantity) {

        InventoryPOJO inventory = InventoryPOJO.builder()
                .productId(productId)
                .warehouse(warehouse)
                .minThreshold(threshold)
                .build();

        return inventory;
    }

//    public static String patchInventory( String field , Object value) {
//        return InventoryPOJO.builder()
//                .
//    }

    public static String quantityExceedsStockJson(int productId) {
        return validInventoryJson(productId,"Virtual", 2, 50);
    }
}
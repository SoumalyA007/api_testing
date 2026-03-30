package testData;

import payloads.request.InventoryPOJO;

public class InventoryTestDataFactory {



    public static InventoryPOJO validInventoryPayload(int productId, String warehouse, int threshold, int quantity) {

        InventoryPOJO inventory = InventoryPOJO.builder()
                .productId(productId)
                .warehouse(warehouse)
                .minThreshold(threshold)
                .quantity(quantity)
                .build();

        return inventory;
    }

//    public static String patchInventory( String field , Object value) {
//        return InventoryPOJO.builder()
//                .
//    }


}
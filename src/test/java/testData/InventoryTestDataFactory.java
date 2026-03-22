package testData;

public class InventoryTestDataFactory {

    public static String validInventoryJson(int productId, int stock, String warehouse, int threshold, int quantity) {
        return String.format("""
                {
                    "productId": %d,
                    "stockCount": %d,
                    "warehouse": "%s",
                    "minThreshold": %d,
                    "quantity": %d
                }
                """, productId, stock, warehouse, threshold, quantity);
    }

    public static String invalidInventoryJson() {
        return """
                {
                    "productId": null,
                    "stockCount": -10,
                    "warehouse": "",
                    "minThreshold": -1,
                    "quantity": -5
                }
                """;
    }

    public static String quantityExceedsStockJson(int productId) {
        return validInventoryJson(productId, 10, "Virtual", 2, 50);
    }
}
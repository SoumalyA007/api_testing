package utilities;

import java.util.concurrent.atomic.AtomicInteger;

public class ProductIdGenerator {

    private static final AtomicInteger productIdCounter = new AtomicInteger(1000);

    public static int getUniqueProductId() {
        return productIdCounter.incrementAndGet();
    }
}
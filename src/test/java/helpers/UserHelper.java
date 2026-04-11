package helpers;

import endpoints.Users;
import enums.UserRole;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class UserHelper {

    private static List<Integer> cachedUserIds;

    // ================= USER IDS =================

    public static List<Integer> getAllUserIds() {
        if (cachedUserIds == null) {
            cachedUserIds = Users.getAllUsers(UserRole.USER)
                    .then()
                    .extract()
                    .jsonPath()
                    .getList("id", Integer.class);
        }
        return cachedUserIds;
    }

    public static int getRandomUserId() {
        List<Integer> ids = getAllUserIds();
        return ids.get(new Random().nextInt(ids.size()));
    }

    // ✅ FIXED VERSION (if you still want "last")
    public static int getLastUserId() {
        List<Integer> ids = getAllUserIds();

        return ids.stream()
                .max(Comparator.naturalOrder())
                .orElseThrow(() -> new RuntimeException("No users found"));
    }

    // ================= TEST SETUP =================

    public static int createTestUser(payloads.request.UserPOJO userPOJO) {
        return Users.createUser(userPOJO, UserRole.ADMIN)
                .then()
                .extract()
                .path("id");
    }

    // ================= CLEANUP =================

    public static void deleteUserIfExists(int userId) {
        try {
            if(userId != 0) {
                Users.deleteUser(userId, UserRole.ADMIN);
            }
        } catch (Exception ignored) {
            // safe cleanup
        }
    }
}

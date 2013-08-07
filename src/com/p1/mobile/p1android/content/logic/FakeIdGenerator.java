package com.p1.mobile.p1android.content.logic;

public class FakeIdGenerator {

    private static int idCounter = 0;

    public static String getNextFakeId() {
        idCounter--;
        return String.valueOf(idCounter);

    }

    public static boolean isFakeId(String id) {
        int intId;
        try {
            intId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return false;
        }
        return intId < 0;
    }

}

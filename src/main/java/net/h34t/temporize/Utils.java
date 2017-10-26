package net.h34t.temporize;

import java.util.List;

public class Utils {

    public static <A> boolean containsDuplicates(List<A> collection) {
        if (collection == null || collection.size() < 2)
            return false;

        for (int i = 0; i < collection.size() - 1; i++) {
            for (int j = i + 1; j < collection.size(); j++) {
                if (collection.get(i).equals(collection.get(j)))
                    return true;
            }
        }

        return false;
    }

    public static String ucFirst(String input) {
        return input == null || input.length() == 0
                ? ""
                : input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static String toClassName(String name) {
        return ucFirst(name);
    }

    public static String normalizeVarName(String name) {
        return ucFirst(name);
    }

}
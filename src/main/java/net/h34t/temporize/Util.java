package net.h34t.temporize;

import java.util.List;

public class Util {

    public static <A> boolean containsDuplicates(List<A> collection) {
        if (collection == null || collection.isEmpty())
            return false;

        for (int i = 0; i < collection.size() - 1; i++) {
            for (int j = i + 1; i < collection.size(); j++) {
                if (collection.get(i).equals(collection.get(j)))
                    return true;
            }

        }

        return false;
    }
}
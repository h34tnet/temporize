package net.h34t.temporize;

public class Ident {

    public static String of(int level) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < level; i++)
            sb.append("    ");

        return sb.toString();
    }
}

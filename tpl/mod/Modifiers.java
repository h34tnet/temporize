package mod;

public class Modifiers {

    public static String html(String raw) {
        return raw.replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");
    }

}

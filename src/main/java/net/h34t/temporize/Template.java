package net.h34t.temporize;

public class Template {

    public final String className;
    public final String packageName;
    public final String code;

    public Template(String packageName, String className, String code) {
        this.className = className;
        this.packageName = packageName;
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }
}

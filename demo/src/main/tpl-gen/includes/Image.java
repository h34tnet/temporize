package includes;

import java.io.Writer;

import static net.h34t.temporizedemo.Modifiers.*;

public class Image {

    private String src = "";
    private String title = "";

    public Image() {
    }

    public Image(String src, String title) {
        this.src = src;
        this.title = title;
    }

    public Image setSrc(String src) {
        this.src = src;
        return this;
    }

    public Image setTitle(String title) {
        this.title = title;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<img src=\"");
        sb.append(this.src);
        sb.append("\"");

        if (this.title != null && !title.isEmpty()) {
            sb.append(" title=\"");
            sb.append(this.title);
            sb.append("\"");
        }

        sb.append(">\n");

        return sb.toString();
    }

    public void write(Writer w) throws java.io.IOException {
        w.write("<img src=\"");
        w.write(this.src);
        w.write("\"");

        if (this.title != null && !title.isEmpty()) {
            w.write(" title=\"");
            w.write(this.title);
            w.write("\"");
        }

        w.write(">\n");
    }
}

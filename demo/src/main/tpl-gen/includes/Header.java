package includes;

import java.io.Writer;

import static net.h34t.temporizedemo.Modifiers.*;

public class Header {

    private String header = "";
    private String username = "";
    private boolean isLoggedIn;

    public Header() {
    }

    public Header(String header, String username, boolean isLoggedIn) {
        this.header = header;
        this.username = username;
        this.isLoggedIn = isLoggedIn;
    }

    public Header setHeader(String header) {
        this.header = header;
        return this;
    }

    public Header setUsername(String username) {
        this.username = username;
        return this;
    }

    public Header setIsLoggedIn(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"header\">");
        sb.append(this.header);
        sb.append(" - ");

        if (this.isLoggedIn) {
            sb.append("welcome, <a href=\"/user/");
            sb.append(urlenc(this.username));
            sb.append("\">");
            sb.append(html(this.username));
            sb.append("</a>");

        } else {
            sb.append("{include\n    includes.login.Login as $login}");
        }

        sb.append("\n</div>\n");

        return sb.toString();
    }

    public void write(Writer w) throws java.io.IOException {
        w.write("<div class=\"header\">");
        w.write(this.header);
        w.write(" - ");

        if (this.isLoggedIn) {
            w.write("welcome, <a href=\"/user/");
            w.write(urlenc(this.username));
            w.write("\">");
            w.write(html(this.username));
            w.write("</a>");

        } else {
            w.write("{include\n    includes.login.Login as $login}");
        }

        w.write("\n</div>\n");
    }
}

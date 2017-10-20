package index;

import java.io.Writer;

import java.util.List;
import java.util.ArrayList;

import static net.h34t.temporizedemo.Modifiers.*;

public class Index {

    private String intro = "";
    private String title = "";
    private List<Authors> authors = new ArrayList<>();
    private includes.Header header;

    public Index() {
    }

    public Index(String intro, String title, List<Authors> authors, includes.Header header) {
        this.intro = intro;
        this.title = title;
        this.authors = authors;
        this.header = header;
    }

    public Index setIntro(String intro) {
        this.intro = intro;
        return this;
    }

    public Index setTitle(String title) {
        this.title = title;
        return this;
    }

    public Index setAuthors(List<Authors> authors) {
        this.authors = authors;
        return this;
    }

    public Index setHeader(includes.Header header) {
        this.header = header;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html>\n<head>\n    <title>");
        sb.append(this.title);
        sb.append("</title>\n</head>\n<body>\n");
        sb.append(header.toString());
        sb.append("\n<h1>");
        sb.append(html(this.title));
        sb.append("</h1>\n");

        if (this.authors != null && !authors.isEmpty()) {
            sb.append("\n<p>written by:</p>p>\n<ul>");

            for (Authors _block : authors)
                sb.append(_block.toString());
            sb.append("\n</ul>\n");

        } else {
            sb.append("\n<p>no authors</p>\n");
        }

        sb.append("\n");

        if (this.intro != null && !intro.isEmpty()) {
            sb.append("\n<p>");
            sb.append(this.intro);
            sb.append("</p>\n");
        }

        sb.append("\n</body>\n</html>\n");

        return sb.toString();
    }

    public void write(Writer w) throws java.io.IOException {
        w.write("<!DOCTYPE html>\n<html>\n<head>\n    <title>");
        w.write(this.title);
        w.write("</title>\n</head>\n<body>\n");
        header.write(w);
        w.write("\n<h1>");
        w.write(html(this.title));
        w.write("</h1>\n");

        if (this.authors != null && !authors.isEmpty()) {
            w.write("\n<p>written by:</p>p>\n<ul>");

            for (Authors _block : authors)
                _block.write(w);
            w.write("\n</ul>\n");

        } else {
            w.write("\n<p>no authors</p>\n");
        }

        w.write("\n");

        if (this.intro != null && !intro.isEmpty()) {
            w.write("\n<p>");
            w.write(this.intro);
            w.write("</p>\n");
        }

        w.write("\n</body>\n</html>\n");
    }

    public static class Authors {

        private String link = "";
        private String name = "";
        private String rating = "";
        private String age = "";

        public Authors() {
        }

        public Authors(String age, String link, String name, String rating) {
            this.link = link;
            this.name = name;
            this.rating = rating;
            this.age = age;
        }

        public Authors setLink(String link) {
            this.link = link;
            return this;
        }

        public Authors setName(String name) {
            this.name = name;
            return this;
        }

        public Authors setRating(String rating) {
            this.rating = rating;
            return this;
        }

        public Authors setAge(String age) {
            this.age = age;
            return this;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n    <li>");

            if (this.link != null && !link.isEmpty()) {
                sb.append("<a href=\"");
                sb.append(urlenc(this.link));
                sb.append("\">");
                sb.append(html(this.name));
                sb.append("</a>");

            } else {
                sb.append(html(this.name));
            }

            sb.append(",\n        age ");
            sb.append(this.age);
            sb.append(", Rating: ");
            sb.append(this.rating);
            sb.append("\n    </li>\n    ");
    
            return sb.toString();
        }

        public void write(Writer w) throws java.io.IOException {
            w.write("\n    <li>");

            if (this.link != null && !link.isEmpty()) {
                w.write("<a href=\"");
                w.write(urlenc(this.link));
                w.write("\">");
                w.write(html(this.name));
                w.write("</a>");

            } else {
                w.write(html(this.name));
            }

            w.write(",\n        age ");
            w.write(this.age);
            w.write(", Rating: ");
            w.write(this.rating);
            w.write("\n    </li>\n    ");
        }
    }
}

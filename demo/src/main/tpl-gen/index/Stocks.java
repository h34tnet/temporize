package index;

import java.io.Writer;

import java.util.List;
import java.util.ArrayList;

import static net.h34t.temporizedemo.Modifiers.*;

public class Stocks {

    private String bar = "";
    private List<Items> items = new ArrayList<>();

    public Stocks() {
    }

    public Stocks(String bar, List<Items> items) {
        this.bar = bar;
        this.items = items;
    }

    public Stocks setBar(String bar) {
        this.bar = bar;
        return this;
    }

    public Stocks setItems(List<Items> items) {
        this.items = items;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html>\n<head>\n    <title>Stock Prices</title>\n    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n    <meta http-equiv=\"Content-Style-Type\" content=\"text/css\" />\n    <meta http-equiv=\"Content-Script-Type\" content=\"text/javascript\" />\n    <link rel=\"shortcut icon\" href=\"/images/favicon.ico\" />\n    <link rel=\"stylesheet\" type=\"text/css\" href=\"/css/style.css\" media=\"all\" />\n    <script type=\"text/javascript\" src=\"/js/util.js\"></script>\n    <style type=\"text/css\">\n\t\t/*<![CDATA[*/\n\t\tbody {\n\t\t\tcolor: #333333;\n\t\t\tline-height: 150%;\n\t\t}\n\n\t\tthead {\n\t\t\tfont-weight: bold;\n\t\t\tbackground-color: #CCCCCC;\n\t\t}\n\n\t\t.odd {\n\t\t\tbackground-color: #FFCCCC;\n\t\t}\n\n\t\t.even {\n\t\t\tbackground-color: #CCCCFF;\n\t\t}\n\n\t\t.minus {\n\t\t\tcolor: #FF0000;\n\t\t}\n\n\t\t/*]]>*/\n\t</style>\n\n</head>\n\n<body>\n\n<h1>Stock Prices</h1>\n\n<table>\n    <thead>\n    <tr>\n        <th>#</th>\n        <th>symbol</th>\n        <th>name</th>\n        <th>price</th>\n        <th>change</th>\n        <th>ratio</th>\n    </tr>\n    </thead>\n    <tbody>\n    ");

        for (Items _block : items)
            sb.append(_block.toString());
        sb.append("\n    </tbody>\n</table>\n");
        sb.append(this.bar);
        sb.append("\n</body>\n</html>\n");

        return sb.toString();
    }

    public void write(Writer w) throws java.io.IOException {
        w.write("<!DOCTYPE html>\n<html>\n<head>\n    <title>Stock Prices</title>\n    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n    <meta http-equiv=\"Content-Style-Type\" content=\"text/css\" />\n    <meta http-equiv=\"Content-Script-Type\" content=\"text/javascript\" />\n    <link rel=\"shortcut icon\" href=\"/images/favicon.ico\" />\n    <link rel=\"stylesheet\" type=\"text/css\" href=\"/css/style.css\" media=\"all\" />\n    <script type=\"text/javascript\" src=\"/js/util.js\"></script>\n    <style type=\"text/css\">\n\t\t/*<![CDATA[*/\n\t\tbody {\n\t\t\tcolor: #333333;\n\t\t\tline-height: 150%;\n\t\t}\n\n\t\tthead {\n\t\t\tfont-weight: bold;\n\t\t\tbackground-color: #CCCCCC;\n\t\t}\n\n\t\t.odd {\n\t\t\tbackground-color: #FFCCCC;\n\t\t}\n\n\t\t.even {\n\t\t\tbackground-color: #CCCCFF;\n\t\t}\n\n\t\t.minus {\n\t\t\tcolor: #FF0000;\n\t\t}\n\n\t\t/*]]>*/\n\t</style>\n\n</head>\n\n<body>\n\n<h1>Stock Prices</h1>\n\n<table>\n    <thead>\n    <tr>\n        <th>#</th>\n        <th>symbol</th>\n        <th>name</th>\n        <th>price</th>\n        <th>change</th>\n        <th>ratio</th>\n    </tr>\n    </thead>\n    <tbody>\n    ");

        for (Items _block : items)
            _block.write(w);
        w.write("\n    </tbody>\n</table>\n");
        w.write(this.bar);
        w.write("\n</body>\n</html>\n");
    }

    public static class Items {

        private String symbol = "";
        private String rowClass = "";
        private String price = "";
        private String negativeClass = "";
        private String change = "";
        private String name = "";
        private String url = "";
        private String ratio = "";

        public Items() {
        }

        public Items(String change, String name, String negativeClass, String price, String ratio, String rowClass, String symbol, String url) {
            this.symbol = symbol;
            this.rowClass = rowClass;
            this.price = price;
            this.negativeClass = negativeClass;
            this.change = change;
            this.name = name;
            this.url = url;
            this.ratio = ratio;
        }

        public Items setSymbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Items setRowClass(String rowClass) {
            this.rowClass = rowClass;
            return this;
        }

        public Items setPrice(String price) {
            this.price = price;
            return this;
        }

        public Items setNegativeClass(String negativeClass) {
            this.negativeClass = negativeClass;
            return this;
        }

        public Items setChange(String change) {
            this.change = change;
            return this;
        }

        public Items setName(String name) {
            this.name = name;
            return this;
        }

        public Items setUrl(String url) {
            this.url = url;
            return this;
        }

        public Items setRatio(String ratio) {
            this.ratio = ratio;
            return this;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n    <tr class=\"");
            sb.append(this.rowClass);
            sb.append("\">\n        <td>{{index}}</td>\n        <td>\n            <a href=\"/stocks/");
            sb.append(this.symbol);
            sb.append("\">");
            sb.append(this.symbol);
            sb.append("</a>\n        </td>\n        <td>\n            <a href=\"");
            sb.append(this.url);
            sb.append("\">");
            sb.append(this.name);
            sb.append("</a>\n        </td>\n        <td>\n            <strong>");
            sb.append(this.price);
            sb.append("</strong>\n        </td>\n        <td");
            sb.append(this.negativeClass);
            sb.append(">");
            sb.append(this.change);
            sb.append("</td>\n        <td");
            sb.append(this.negativeClass);
            sb.append(">");
            sb.append(this.ratio);
            sb.append("</td>\n    </tr>\n    ");
    
            return sb.toString();
        }

        public void write(Writer w) throws java.io.IOException {
            w.write("\n    <tr class=\"");
            w.write(this.rowClass);
            w.write("\">\n        <td>{{index}}</td>\n        <td>\n            <a href=\"/stocks/");
            w.write(this.symbol);
            w.write("\">");
            w.write(this.symbol);
            w.write("</a>\n        </td>\n        <td>\n            <a href=\"");
            w.write(this.url);
            w.write("\">");
            w.write(this.name);
            w.write("</a>\n        </td>\n        <td>\n            <strong>");
            w.write(this.price);
            w.write("</strong>\n        </td>\n        <td");
            w.write(this.negativeClass);
            w.write(">");
            w.write(this.change);
            w.write("</td>\n        <td");
            w.write(this.negativeClass);
            w.write(">");
            w.write(this.ratio);
            w.write("</td>\n    </tr>\n    ");
        }
    }
}

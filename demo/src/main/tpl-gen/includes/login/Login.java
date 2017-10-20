package includes.login;

import java.io.Writer;

import static net.h34t.temporizedemo.Modifiers.*;

public class Login {


    public Login() {
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<form action=\"/login\" method=\"POST\">\n    Login:\n    <input type=\"text\" name=\"login\"> <input type=\"password\" name=\"password\">\n    <input type=\"submit\" value=\"log in\">\n</form>\n");

        return sb.toString();
    }

    public void write(Writer w) throws java.io.IOException {
        w.write("<form action=\"/login\" method=\"POST\">\n    Login:\n    <input type=\"text\" name=\"login\"> <input type=\"password\" name=\"password\">\n    <input type=\"submit\" value=\"log in\">\n</form>\n");
    }
}

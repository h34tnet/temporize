package includes.special;

import java.io.Writer;

import static net.h34t.temporizedemo.Modifiers.*;

public class Empty {


    public Empty() {
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        return sb.toString();
    }

    public void write(Writer w) throws java.io.IOException {
    }
}

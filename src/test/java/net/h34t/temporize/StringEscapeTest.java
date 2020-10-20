package net.h34t.temporize;

import org.junit.Assert;
import org.junit.Test;

public class StringEscapeTest {

    @Test
    public void testStringEscaping() {
        ASTNode.ConstantValue node = new ASTNode.ConstantValue(null, "Öü");
        String str = Compiler.createStringOutput(node, s -> s, s -> s, 0);
        Assert.assertEquals("\"Öü\";\n", str);
    }
}

package net.h34t.temporize;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class Runner {

    @Test
    public void testRunner() throws IOException {
        List<Token> tokens = Parser.FULL.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("testRunner.html"));
        ASTNode root = new ASTBuilder().build(tokens);

        Template output = new Compiler().compile("net.h34t.temporize", "Temporizer", root);

        System.out.println(output);
    }
}

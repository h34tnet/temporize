package net.h34t.temporize;

import java.io.IOException;
import java.util.List;

public class Runner {

    public static void main(String... args) throws IOException {
        List<Token> tokens = Parser.FULL.parse("Hello {$world}, {$bar} {if $world}{$bar}{else}is \"{$flarb}\"!{/if} wheeoo.\n" +
                "{for $blocky}{$blockthing}{/for}");
        ASTNode root = new ASTBuilder().build(tokens);

        Template output = new Compiler().compile("net.h34t.temporize", "Temporizer", root);

        System.out.println(output);
    }
}

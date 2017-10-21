package net.h34t.temporize;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class SkipTest {

    @Test
    public void testSkip() throws IOException {
        List<Token> tokens = Parser.FULL.parse("{skip}{$foo}{/skip}");
        Assert.assertTrue(tokens.get(0) instanceof Token.Literal);
        Assert.assertEquals("{$foo}", tokens.get(0).contents);
    }

    @Test
    public void testSkipMultiline() throws IOException {
        List<Token> tokens = Parser.FULL.parse("{skip}{$foo}\n{$bar}{/skip}");
        Assert.assertTrue(tokens.get(0) instanceof Token.Literal);
        Assert.assertEquals("{$foo}\n{$bar}", tokens.get(0).contents);
    }


}

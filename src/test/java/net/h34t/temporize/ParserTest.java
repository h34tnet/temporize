package net.h34t.temporize;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * Created by Stefan Schallerl on 25.09.2016.
 */
public class ParserTest {

    public static final Token.Creator[] TOKEN_CREATORS = new Token.Creator[]{
            Token.Variable.getCreator(),
            Token.Block.getCreator(),
            Token.BlockEnd.getCreator(),
            Token.Include.getCreator(),
            Token.Conditional.getCreator(),
            Token.ConditionalElse.getCreator(),
            Token.ConditionalEnd.getCreator()
    };

    public static final File RESOURCES_DIRECTORY = new File("src/test/resources");

    @Test
    public void testParseFile() throws Exception {
        List<Token> tokens = new Parser(TOKEN_CREATORS).parse(new File(RESOURCES_DIRECTORY, "testParse.html"));
        testTokens(tokens);

        Assert.assertEquals(1, tokens.get(0).line);
        Assert.assertEquals(2, tokens.get(1).line);
        Assert.assertEquals(3, tokens.get(2).line);
    }

    @Test
    public void testParseString() throws Exception {
        List<Token> tokens = new Parser(TOKEN_CREATORS).parse("<html>\n{$value}\n</html>");
        testTokens(tokens);

        Assert.assertEquals(1, tokens.get(0).line);
        Assert.assertEquals(2, tokens.get(1).line);
        Assert.assertEquals(3, tokens.get(2).line);
    }

    @Test
    public void testParseStream() throws Exception {
        try (InputStream is = new ByteArrayInputStream("<html>\n{$value}\n</html>".getBytes())) {
            List<Token> tokens = new Parser(TOKEN_CREATORS).parse(is);
            testTokens(tokens);
        }
    }

    @Test
    public void testParseLine() throws Exception {
        List<Token> tokens = new Parser(TOKEN_CREATORS).parseLine("<html>{$value}</html>", 0);
        testTokens(tokens);
    }

    @Test
    public void testVariableModifiers() throws Exception {
        List<Token> tokens = new Parser(TOKEN_CREATORS).parse("<html>{$value|foo|bar}</html>");

        Assert.assertTrue(tokens.get(1) instanceof Token.Variable);

        Token.Variable v = (Token.Variable) tokens.get(1);

        Assert.assertEquals(v.variableName, "value");
        Assert.assertEquals(v.modifiers[0], "foo");
        Assert.assertEquals(v.modifiers[1], "bar");
        Assert.assertEquals(v.modifiers.length, 2);
    }

    public void testTokens(List<Token> tokens) {
        Assert.assertEquals(tokens.size(), 3);
        Assert.assertTrue(tokens.get(0) instanceof Token.Literal);
        Assert.assertTrue(tokens.get(1) instanceof Token.Variable);
        Assert.assertTrue(tokens.get(2) instanceof Token.Literal);
        Assert.assertEquals(((Token.Variable) tokens.get(1)).variableName, "value");

    }
}
package net.h34t.temporize;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

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

    static InputStream getResource(String name) {
        return Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(name);
    }

    @Test
    public void testParseFile() throws Exception {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("testParse.html");
        List<Token> tokens = new Parser(TOKEN_CREATORS).parse(is);
        testTokens(tokens);

        Assert.assertEquals(1, tokens.get(0).line);
        Assert.assertEquals(2, tokens.get(1).line);
        Assert.assertEquals(3, tokens.get(2).line);
    }

    @Test
    public void testParseString() throws Exception {
        List<Token> tokens = new Parser(TOKEN_CREATORS).parse("<html>\n{$value}\n</html>");
        testTokens(tokens);

        tokens.forEach(System.out::println);

        Assert.assertEquals(3, tokens.size());

        Assert.assertEquals(1, tokens.get(0).line);
        Assert.assertEquals(2, tokens.get(1).line);
        Assert.assertEquals(3, tokens.get(2).line);

        Assert.assertEquals(Token.Literal.class, tokens.get(0).getClass());
        Assert.assertEquals("<html>\n", tokens.get(0).contents);

        Assert.assertEquals(Token.Variable.class, tokens.get(1).getClass());
        Assert.assertEquals("{$value}", tokens.get(1).contents);
        Assert.assertEquals("value", ((Token.Variable) tokens.get(1)).variableName);

        Assert.assertEquals(Token.Literal.class, tokens.get(2).getClass());
        Assert.assertEquals("\n</html>\n", tokens.get(2).contents);
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

        Assert.assertEquals("value", v.variableName);
        Assert.assertEquals("foo", v.modifiers[0]);
        Assert.assertEquals("bar", v.modifiers[1]);
        Assert.assertEquals(2, v.modifiers.length);
    }

    private void testTokens(List<Token> tokens) {
        Assert.assertEquals(3, tokens.size());
        Assert.assertTrue(tokens.get(0) instanceof Token.Literal);
        Assert.assertTrue(tokens.get(1) instanceof Token.Variable);
        Assert.assertTrue(tokens.get(2) instanceof Token.Literal);
        Assert.assertEquals("value", ((Token.Variable) tokens.get(1)).variableName);
    }

    @Test
    public void testParseConditionals() throws IOException {

        List<Token> tokens = new Parser(TOKEN_CREATORS)
                .parse(getResource("testParseConditionals.html"));

        Assert.assertEquals(9, tokens.size());

        System.out.println(tokens.stream().map(Token::toString).collect(Collectors.joining("\n")));
    }

    @Test
    public void testParseStocks() throws IOException {

        List<Token> tokens = new Parser(TOKEN_CREATORS)
                .parse(new File("tpl/index/Stocks.temporize.html"));

        ASTNode root = new ASTBuilder().build(tokens);

        // System.out.println(tokens.stream().map(Token::toString).collect(Collectors.joining("\n")));

        // System.out.println(root.print(0));

        Template t = new Compiler().compile("test", "test", "", root, s -> {
        });


        Assert.assertEquals("test", t.className);
        Assert.assertEquals("test", t.packageName);
        Assert.assertNotNull(t.code);
        Assert.assertFalse(t.code.trim().isEmpty());
    }
}
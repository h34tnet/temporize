package net.h34t.temporize;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class CommentTest {

    @Test
    public void testComment() throws IOException {
        List<Token> tokens = Parser.FULL.parse("{comment}hello world{/comment}");
        Assert.assertEquals(0, tokens.size());
    }

    @Test
    public void testComment2() throws IOException {
        List<Token> tokens = Parser.FULL.parse("foo\n{comment}hello world{/comment}\nbar");
        Assert.assertEquals(1, tokens.size());
        Assert.assertEquals("foo\n\nbar", tokens.get(0).contents);
    }

    @Test
    public void testCommentMultiline() throws IOException {
        List<Token> tokens = Parser.FULL.parse("{comment}hello\nworld{/comment}");
        Assert.assertEquals(0, tokens.size());
    }


    @Test
    public void testCommentMultiline2() throws IOException {
        List<Token> tokens = Parser.FULL.parse("foo\n{comment}hello\nworld{/comment}\nbar");
        Assert.assertEquals(1, tokens.size());
        Assert.assertEquals("foo\n\nbar", tokens.get(0).contents);
    }
}

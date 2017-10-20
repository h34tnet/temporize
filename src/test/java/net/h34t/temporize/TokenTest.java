package net.h34t.temporize;

import org.junit.Test;

/**
 *
 */
public class TokenTest {
    @Test
    public void checkValidity() throws Exception {
        new Token.Variable("{$foo}", "foo", "", "unknown", 1, 0);
    }

    @Test(expected = RuntimeException.class)
    public void checkValidityReserved() throws Exception {
        Parser.FULL.parse("{$package}");
    }


    @Test(expected = RuntimeException.class)
    public void checkValidityModifierReserved() throws Exception {
        Parser.FULL.parse("{$foo|package}");
    }
}
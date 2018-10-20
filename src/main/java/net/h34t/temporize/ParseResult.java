package net.h34t.temporize;

import java.util.ArrayList;
import java.util.Collection;

public class ParseResult extends ArrayList<Token> {

    private final byte[] hash;

    public ParseResult(Collection<? extends Token> c, byte[] hash) {
        super(c);
        this.hash = hash;
    }

    public byte[] getHash() {
        return hash;
    }
}

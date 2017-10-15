package net.h34t.temporize;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

public class Parser {

    public static final Parser FULL = new Parser(new Token.Creator[]{
            Token.Variable.getCreator(),
            Token.Block.getCreator(),
            Token.BlockEnd.getCreator(),
            Token.Include.getCreator(),
            Token.Conditional.getCreator(),
            Token.ConditionalElse.getCreator(),
            Token.ConditionalEnd.getCreator()
    });

    private final Token.Creator[] creators;

    public Parser(Token.Creator[] creators) {
        this.creators = creators;
    }

    public List<Token> parse(File file) throws IOException {
        return parse(new FileInputStream(file));
    }

    public List<Token> parse(String contents) throws IOException {
        return parse(new ByteArrayInputStream(contents.getBytes()));
    }

    public List<Token> parse(InputStream is) throws IOException {
        List<Token> tokens = new ArrayList<>();

        try (LineNumberReader reader = new LineNumberReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null)
                tokens.addAll(parseLine(line + "\n", reader.getLineNumber()));

            return join(tokens);
        }
    }

    /**
     * Parses a line into a list of tokens. Single tokens may not span multiple lines, except
     * for a LiteralToken, which might be combined
     *
     * @param line       the line to be parsed
     * @param lineNumber the line number for debugging purposes
     * @return a list of tokens
     */
    protected List<Token> parseLine(String line, int lineNumber) {
        List<Token> tokens = new ArrayList<>();

        int offs = 0;

        while (true) {
            TokenMatchResult nextToken = null;

            for (Token.Creator c : creators) {
                Matcher matcher = c.getPattern().matcher(line);

                if (matcher.find(offs)) {
                    if (nextToken == null || nextToken.start > matcher.start()) {
                        nextToken = new TokenMatchResult(matcher.toMatchResult(), c, matcher.start(), matcher.end());
                    }
                }
            }

            if (nextToken != null) {
                tokens.add(new Token.Literal(line.substring(offs, nextToken.start), lineNumber, offs));
                tokens.add(nextToken.create(lineNumber));
                offs = nextToken.end;
            } else
                break;
        }

        // if nothing is found the remainder of the input must be a literal
        tokens.add(new Token.Literal(line.substring(offs), lineNumber, offs));

        return tokens;
    }

    /**
     * @param tokens the list of tokens
     * @return a list of tokens where consecutive literals are joined together
     */
    private List<Token> join(List<Token> tokens) {
        Stack<Token> joinedToken = new Stack<>();

        for (Token token : tokens) {
            if (token.contents.isEmpty()) {
                // do nothing
                
            } else if (joinedToken.isEmpty()) {
                joinedToken.push(token);

            } else if (joinedToken.peek() instanceof Token.Literal && token instanceof Token.Literal) {
                Token top = joinedToken.pop();
                joinedToken.push(new Token.Literal(top.contents + token.contents, token.line, top.offs));

            } else {
                joinedToken.push(token);
            }
        }

        return joinedToken;
    }

    private static class TokenMatchResult {

        public final MatchResult matchResult;
        public final Token.Creator creator;
        public final int start;
        public final int end;

        TokenMatchResult(MatchResult matchResult, Token.Creator creator, int start, int end) {
            this.matchResult = matchResult;
            this.creator = creator;
            this.start = start;
            this.end = end;
        }

        public Token create(int lineNumber) {
            return creator.create(matchResult, lineNumber);
        }

        @Override
        public String toString() {
            return String.format("%s from %d to %d\n", matchResult.group(), start, end);
        }
    }

}

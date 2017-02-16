package net.h34t.temporize;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

public class Parser {

    private final Token.Creator[] creators;

    public Parser(Token.Creator[] creators) {
        this.creators = creators;
    }

    public static void main(String... args) throws IOException {

        Token.Creator[] tokenCreators = new Token.Creator[]{
                Token.Variable.getCreator(),
                Token.Block.getCreator(),
                Token.BlockEnd.getCreator(),
                Token.Include.getCreator(),
                Token.Conditional.getCreator(),
                Token.ConditionalElse.getCreator(),
                Token.ConditionalEnd.getCreator()
        };

        List<Token> tokens = new Parser(tokenCreators).parse(new File("tpl/test.html"));

        tokens.forEach(System.out::println);
    }

    public List<Token> parse(File file) throws IOException {
        List<Token> tokens = new ArrayList<>();

        try (LineNumberReader reader = new LineNumberReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null)
                tokens.addAll(parseLine(line, reader.getLineNumber()));

            return join(tokens);
        }
    }

    public List<Token> parse(String contents) throws IOException {
        List<Token> tokens = new ArrayList<>();

        try (LineNumberReader reader = new LineNumberReader(new StringReader(contents))) {
            String line;
            while ((line = reader.readLine()) != null)
                tokens.addAll(parseLine(line, reader.getLineNumber()));

            return join(tokens);
        }
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
     * for a LiteralToken, which, which might be combined
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

            // if nothing is found the remainder of the input must be a literal
            if (nextToken != null) {
                tokens.add(new Token.Literal(line.substring(offs, nextToken.start), lineNumber, offs));
                tokens.add(nextToken.create(lineNumber));
                offs = nextToken.end;
            } else
                break;
        }

        tokens.add(new Token.Literal(line.substring(offs), lineNumber, offs));

        return tokens;
    }

    private List<Token> join(List<Token> tokens) {

        Stack<Token> joinedToken = new Stack<>();

        for (Token token : tokens) {
            if (token.contents.isEmpty()) {

            } else if (joinedToken.isEmpty()) {
                joinedToken.push(token);

            } else if (joinedToken.peek() instanceof Token.Literal && token instanceof Token.Literal) {
                Token top = joinedToken.pop();
                joinedToken.push(new Token.Literal(top.contents + token.contents, top.line, top.offs));

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

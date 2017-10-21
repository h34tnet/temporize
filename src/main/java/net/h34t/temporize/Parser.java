package net.h34t.temporize;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class Parser {

    public static final Parser FULL = new Parser(new TokenCreator[]{
            TokenCreator.Variable.getCreator(),
            TokenCreator.DefaultVariable.getCreator(),
            TokenCreator.Block.getCreator(),
            TokenCreator.BlockEnd.getCreator(),
            TokenCreator.Include.getCreator(),
            TokenCreator.Conditional.getCreator(),
            TokenCreator.ConditionalElse.getCreator(),
            TokenCreator.ConditionalEnd.getCreator(),
            TokenCreator.Skip.getCreator(),
            TokenCreator.Comment.getCreator(),
    });

    private final TokenCreator[] creators;

    public Parser(TokenCreator[] creators) {
        this.creators = creators;
    }


    /**
     * Parses the contents of a File into a list of tokens
     *
     * @param file the File to read
     * @return the list of tokens
     * @throws IOException on read errors or if the File doesn't exist
     */
    public List<Token> parse(File file) throws IOException {
        return parse(file.getName(), new FileInputStream(file));
    }

    /**
     * Parses a String into a list of tokens
     *
     * @param contents the String to parse
     * @return the list of tokens
     * @throws IOException
     */
    public List<Token> parse(String contents) throws IOException {
        return parse("?string", new ByteArrayInputStream(contents.getBytes()));
    }

    /**
     * Parses a stream into a list of tokens
     *
     * @param is the inputstream to parse
     * @return the list of tokens
     * @throws IOException on read errors
     */
    public List<Token> parse(InputStream is) throws IOException {
        return parse("?stream", is);
    }

    /**
     * Parses an InputStream into a list of tokens
     *
     * @param source the name of the source
     * @param is     the InputStream
     * @return the list of tokens
     * @throws IOException on read errors
     */
    protected List<Token> parse(String source, InputStream is) throws IOException {
        List<Token> tokens = new ArrayList<>();

        try (LineNumberReader reader = new LineNumberReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null)
                tokens.addAll(parseLine(line + "\n", source, reader.getLineNumber()));

            // remove comment tokens
            tokens = tokens.stream()
                    .filter(token -> !(token instanceof Token.Comment))
                    .collect(Collectors.toList());

            // join consecutive literal tokens
            return join(tokens);
        }
    }

    /**
     * Parses a line into a list of tokens. Single tokens may not span multiple lines, except
     * for a LiteralToken, which might be combined
     *
     * @param line       the line to be parsed
     * @param lineNumber the line number for logging purposes
     * @return a list of tokens
     */
    protected List<Token> parseLine(String line, int lineNumber) {
        return parseLine(line, "?line", lineNumber);
    }

    /**
     * The actual parsing method. Returns all tokens found in a single line.
     * <p>
     * TODO currently the parser is broken for SKIPs and COMMENTs
     *
     * @param line       the line to be parsed
     * @param source     the source identifier for debugging and error reporting purposes
     * @param lineNumber the line number in the source
     * @return a list of tokens
     */
    protected List<Token> parseLine(String line, String source, int lineNumber) {
        List<Token> tokens = new ArrayList<>();

        int offs = 0;

        while (true) {
            TokenMatchResult nextToken = null;

            // find the next token
            // note that
            for (TokenCreator c : creators) {
                Matcher matcher = c.getPattern().matcher(line);

                if (matcher.find(offs)) {
                    if (nextToken == null || nextToken.start > matcher.start()) {
                        nextToken = new TokenMatchResult(matcher.toMatchResult(), c, matcher.start(), matcher.end());
                    }
                }
            }

            if (nextToken != null) {
                // add a literal token from the current parsing position
                // to the beginning of the next token
                tokens.add(new Token.Literal(line.substring(offs, nextToken.start), source, lineNumber, offs));
                tokens.add(nextToken.create(source, lineNumber));
                offs = nextToken.end;
            } else
                break;
        }

        // if nothing is found the remainder of the input must be a literal
        tokens.add(new Token.Literal(line.substring(offs), source, lineNumber, offs));

        return tokens;
    }

    /**
     * Joins two consecutive LiteralTokens into one LiteralToken to reduce
     * method calls later on. A side effect is that line numbers aren't exact anymore because the joined token has
     * either the line number of the first one or the second one.
     * <p>
     * This could be prevented by implementing fromLine-toLine instead of just line, but it's probably not worth it.
     *
     * @param tokens the list of tokens
     * @return a list of tokens where consecutive literals are joined together
     */
    private List<Token> join(List<Token> tokens) {
        Stack<Token> joinedToken = new Stack<>();

        for (Token token : tokens) {
            if (!token.contents.isEmpty()) {
                if (joinedToken.isEmpty()) {
                    joinedToken.push(token);

                } else if (joinedToken.peek() instanceof Token.Literal && token instanceof Token.Literal) {
                    // If both are literals, they can be joined. The line number is taken from the lower one.
                    Token top = joinedToken.pop();
                    joinedToken.push(new Token.Literal(
                            top.contents + token.contents,
                            token.source,
                            token.line,
                            top.offs));

                } else {
                    joinedToken.push(token);
                }
            }
        }

        return joinedToken;
    }

    /**
     * Represents a match result by a certain TokenCreator.
     */
    private static class TokenMatchResult {

        public final MatchResult matchResult;
        public final TokenCreator creator;
        public final int start;
        public final int end;

        TokenMatchResult(MatchResult matchResult, TokenCreator creator, int start, int end) {
            this.matchResult = matchResult;
            this.creator = creator;
            this.start = start;
            this.end = end;
        }

        public Token create(String source, int lineNumber) {
            return creator.create(matchResult, source, lineNumber);
        }

        @Override
        public String toString() {
            return String.format("%s from %d to %d\n", matchResult.group(), start, end);
        }
    }

}

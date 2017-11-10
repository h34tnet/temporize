package net.h34t.temporize;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class Token {

    public static Set<String> RESERVERD_KEYWORDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "assert",
            "abstract", "boolean", "break", "byte",
            "case", "catch", "char", "class",
            "const", "continue", "default", "do",
            "double", "else", "enum", "extends", "final",
            "finally", "float", "for", "goto",
            "if", "implements", "import",
            "instanceof", "int", "interface",
            "long", "native", "new", "package",
            "private", "protected", "public",
            "return", "short", "static", "super",
            "switch", "synchronized", "this",
            "throw", "throws", "transient",
            "try", "void", "volatile", "while"
    )));

    public final String contents;

    public final String source;
    public final int line;
    public final int offs;

    public Token(String contents, String source, int line, int offs) {
        this.contents = contents;
        this.source = source;
        this.line = line;
        this.offs = offs;
    }

    public static void checkValidity(String name, Token token) {
        if (RESERVERD_KEYWORDS.contains(name.toLowerCase()))
            throw new RuntimeException("Identifier name \"" + name + "\" is a reserved keyword at " + token.getPosition());
    }

    public String getPosition() {
        return String.format("<%s>%d:%d", source, line, offs);
    }

    @Override
    public String toString() {
        return String.format("@<%s> %5d|%5d %s (%s)",
                source,
                line,
                offs,
                this.getClass().getSimpleName(),
                contents.replace("\n", "\\n"));
    }

    /**
     * Variables are later compiled to placeholders.
     */
    public static class Variable extends Token {

        public final String variableName;
        public final String[] modifiers;

        public Variable(String contents, String variableName, String modifiers, String source, int line, int offs) {
            super(contents, source, line, offs);
            this.variableName = variableName;
            this.modifiers = modifiers.isEmpty()
                    ? new String[]{}
                    : modifiers.substring(1).split("\\|");

            checkValidity(variableName, this);
            for (String mod : this.modifiers)
                checkValidity(mod, this);
        }

        @Override
        public String toString() {
            return String.format("@%5d|%5d %s:%s[%s] (%s)", line, offs, this.getClass().getSimpleName(),
                    this.variableName,
                    String.join(":", this.modifiers),
                    contents.replace("\n", "\\n"));
        }
    }

    public static class Block extends Token {

        public final String blockName;

        public Block(String contents, String blockName, String source, int line, int offs) {
            super(contents, source, line, offs);
            this.blockName = blockName;
            checkValidity(blockName, this);
        }

        @Override
        public String toString() {
            return String.format("@%5d|%5d %s:%s (%s)",
                    line,
                    offs,
                    this.getClass().getSimpleName(),
                    this.blockName,
                    contents.replace("\n", "\\n"));
        }
    }

    public static class BlockEnd extends Token {

        public BlockEnd(String contents, String source, int line, int offs) {
            super(contents, source, line, offs);
        }

        @Override
        public String toString() {
            return String.format("@%5d|%5d %s (%s)",
                    line,
                    offs,
                    this.getClass().getSimpleName(),
                    contents.replace("\n", "\\n"));
        }
    }

    public static class Include extends Token {

        public final String includeName;
        public final String instanceName;

        public Include(String contents, String includeName, String instanceName, String source, int line, int offs) {
            super(contents, source, line, offs);
            this.includeName = includeName;
            this.instanceName = instanceName;
        }

        @Override
        public String toString() {
            return String.format("@%5d|%5d %s:%s (%s)", line, offs, this.getClass().getSimpleName(), this.includeName, contents.replace("\n", "\\n"));
        }
    }

    public static class Conditional extends Token {

        public final String conditionalVariable;

        public Conditional(String contents, String conditionalVariable, String source, int line, int offs) {
            super(contents, source, line, offs);
            this.conditionalVariable = conditionalVariable;
        }

        @Override
        public String toString() {
            return String.format("@%5d|%5d %s:%s (%s)", line, offs, this.getClass().getSimpleName(), this.conditionalVariable, contents.replace("\n", "\\n"));
        }
    }

    public static class ConditionalElse extends Token {

        public ConditionalElse(String contents, String source, int line, int offs) {
            super(contents, source, line, offs);
        }
    }

    public static class ConditionalEnd extends Token {

        public ConditionalEnd(String contents, String source, int line, int offs) {
            super(contents, source, line, offs);
        }
    }

    /**
     * Literals represent a static string.
     * <p>
     * Literals aren't found by RegExps, they're the fillers before, between and after matches.
     */
    public static class Literal extends Token {

        public Literal(String contents, String source, int line, int offs) {
            super(contents, source, line, offs);
        }
    }

    /**
     * Skip Tokens will skip parsing until the end-skip and emit a literal.
     */
    public static class Skip extends Token {

        public Skip(String contents, String source, int line, int offs) {
            super(contents, source, line, offs);
        }
    }

    /**
     * Skip Tokens will skip parsing until the end-skip and emit a literal.
     */
    public static class SkipEnd extends Token {

        public SkipEnd(String contents, String source, int line, int offs) {
            super(contents, source, line, offs);
        }
    }

    /**
     * Comment Tokens will be removed during parsing and don't anything.
     */
    public static class Comment extends Token {

        public Comment(String contents, String source, int line, int offs) {
            super(contents, source, line, offs);
        }
    }

    /**
     * Comment Tokens will be removed during parsing and don't anything.
     */
    public static class CommentEnd extends Token {

        public CommentEnd(String contents, String source, int line, int offs) {
            super(contents, source, line, offs);
        }
    }

}

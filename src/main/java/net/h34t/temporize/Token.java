package net.h34t.temporize;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

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

    public static class Variable extends Token {

        public static Pattern EXP = Pattern.compile("\\{\\$([\\w|]+)}");

        public final String variableName;
        public final String[] modifiers;

        public Variable(String contents, String variableName, String source, int line, int offs) {
            super(contents, source, line, offs);
            String[] parts = variableName.split("\\|");
            this.variableName = parts[0];
            this.modifiers = Arrays.copyOfRange(parts, 1, parts.length);

            checkValidity(variableName, this);
            for (String mod : this.modifiers)
                checkValidity(mod, this);
        }

        public static Creator getCreator() {
            return new Creator() {

                @Override
                public Pattern getPattern() {
                    return EXP;
                }

                @Override
                public Token create(MatchResult matchResult, String source, int line) {
                    return new Variable(matchResult.group(), matchResult.group(1), source, line, matchResult.start());
                }
            };
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

        final static Pattern pattern = Pattern.compile("\\{for\\s+\\$(\\w+)}");

        public final String blockName;

        public Block(String contents, String blockName, String source, int line, int offs) {
            super(contents, source, line, offs);
            this.blockName = blockName;
            checkValidity(blockName, this);
        }

        public static Creator getCreator() {
            return new Creator() {
                @Override
                public Pattern getPattern() {
                    return pattern;
                }

                @Override
                public Token create(MatchResult matchResult, String source, int line) {
                    return new Block(matchResult.group(), matchResult.group(1), source, line, matchResult.start());
                }
            };
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

        public static final Pattern PATTERN = Pattern.compile("\\{/for}");

        public BlockEnd(String contents, String source, int line, int offs) {
            super(contents, source, line, offs);
        }

        public static Creator getCreator() {
            return new Creator() {
                @Override
                public Pattern getPattern() {
                    return PATTERN;
                }

                @Override
                public Token create(MatchResult matchResult, String source, int line) {
                    return new BlockEnd(matchResult.group(), source, line, matchResult.start());
                }
            };
        }
    }

    public static class Include extends Token {

        public static final Pattern PATTERN = Pattern.compile("\\{\\+include\\s+(((\\w+.)*)([A-Z]\\w+))\\s+as\\s+(\\w+)}");

        public final String includeName;
        public final String instanceName;

        public Include(String contents, String includeName, String instanceName, String source, int line, int offs) {
            super(contents, source, line, offs);
            this.includeName = includeName;
            this.instanceName = instanceName;
        }

        public static Creator getCreator() {
            return new Creator() {
                @Override
                public Pattern getPattern() {
                    return PATTERN;
                }

                @Override
                public Token create(MatchResult matchResult, String source, int line) {
                    return new Include(matchResult.group(), matchResult.group(1), matchResult.group(5), source, line, matchResult.start());
                }
            };
        }

        @Override
        public String toString() {
            return String.format("@%5d|%5d %s:%s (%s)", line, offs, this.getClass().getSimpleName(), this.includeName, contents.replace("\n", "\\n"));
        }
    }

    public static class Conditional extends Token {

        public static final Pattern PATTERN = Pattern.compile("\\{if\\s+\\$(\\w+)}");

        public final String conditionalVariable;

        public Conditional(String contents, String conditionalVariable, String source, int line, int offs) {
            super(contents, source, line, offs);
            this.conditionalVariable = conditionalVariable;
        }

        public static Creator getCreator() {
            return new Creator() {
                @Override
                public Pattern getPattern() {
                    return PATTERN;
                }

                @Override
                public Token create(MatchResult matchResult, String source, int line) {
                    return new Conditional(matchResult.group(), matchResult.group(1), source, line, matchResult.start());
                }
            };
        }

        @Override
        public String toString() {
            return String.format("@%5d|%5d %s:%s (%s)", line, offs, this.getClass().getSimpleName(), this.conditionalVariable, contents.replace("\n", "\\n"));
        }
    }

    public static class ConditionalElse extends Token {

        public static final Pattern PATTERN = Pattern.compile("\\{else}");

        public ConditionalElse(String contents, String source, int line, int offs) {
            super(contents, source, line, offs);
        }

        public static Creator getCreator() {
            return new Creator() {
                @Override
                public Pattern getPattern() {
                    return PATTERN;
                }

                @Override
                public Token create(MatchResult matchResult, String source, int line) {
                    return new ConditionalElse(matchResult.group(), source, line, matchResult.start());
                }
            };
        }
    }

    public static class ConditionalEnd extends Token {

        public static final Pattern PATTERN = Pattern.compile("\\{/if}");

        public ConditionalEnd(String contents, String source, int line, int offs) {
            super(contents, source, line, offs);
        }

        public static Creator getCreator() {
            return new Creator() {
                @Override
                public Pattern getPattern() {
                    return PATTERN;
                }

                @Override
                public Token create(MatchResult matchResult, String source, int line) {
                    return new ConditionalEnd(matchResult.group(), source, line, matchResult.start());
                }
            };
        }
    }

    /**
     * Literals aren't found by regExps, they're the fillers before, between and after matches.
     */
    public static class Literal extends Token {

        public Literal(String contents, String source, int line, int offs) {
            super(contents, source, line, offs);
        }

    }

    public static abstract class Creator {

        public abstract Pattern getPattern();

        public abstract Token create(MatchResult matchResult, String source, int line);

    }
}

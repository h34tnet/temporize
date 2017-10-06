package net.h34t.temporize;

import java.util.Arrays;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public abstract class Token {

    public final String contents;
    public final int line;
    public final int offs;

    public Token(String contents, int line, int offs) {
        this.contents = contents;
        this.line = line;
        this.offs = offs;
    }

    @Override
    public String toString() {
        return String.format("@%5d|%5d %s (%s)",
                line,
                offs,
                this.getClass().getSimpleName(),
                contents.replace("\n", "\\n"));
    }

    public static class Variable extends Token {

        public static Pattern EXP = Pattern.compile("\\{\\$([\\w|]+)}");

        public final String variableName;
        public final String[] modifiers;

        public Variable(String contents, String variableName, int line, int offs) {
            super(contents, line, offs);
            String[] parts = variableName.split("\\|");
            this.variableName = parts[0];
            this.modifiers = Arrays.copyOfRange(parts, 1, parts.length);
        }

        public static Creator getCreator() {
            return new Creator() {

                @Override
                public Pattern getPattern() {
                    return EXP;
                }

                @Override
                public Token create(MatchResult matchResult, int line) {
                    return new Variable(matchResult.group(), matchResult.group(1), line, matchResult.start());
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

        public Block(String contents, String blockName, int line, int offs) {
            super(contents, line, offs);
            this.blockName = blockName;
        }

        public static Creator getCreator() {
            return new Creator() {
                @Override
                public Pattern getPattern() {
                    return pattern;
                }

                @Override
                public Token create(MatchResult matchResult, int line) {
                    return new Block(matchResult.group(), matchResult.group(1), line, matchResult.start());
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

        public BlockEnd(String contents, int line, int offs) {
            super(contents, line, offs);
        }

        public static Creator getCreator() {
            return new Creator() {
                @Override
                public Pattern getPattern() {
                    return PATTERN;
                }

                @Override
                public Token create(MatchResult matchResult, int line) {
                    return new BlockEnd(matchResult.group(), line, matchResult.start());
                }
            };
        }
    }

    public static class Include extends Token {

        public static final Pattern PATTERN = Pattern.compile("\\{\\+include\\s+(\\w+)}");

        public final String includeName;

        public Include(String contents, String includeName, int line, int offs) {
            super(contents, line, offs);
            this.includeName = includeName;
        }

        public static Creator getCreator() {
            return new Creator() {
                @Override
                public Pattern getPattern() {
                    return PATTERN;
                }

                @Override
                public Token create(MatchResult matchResult, int line) {
                    return new Include(matchResult.group(), matchResult.group(1), line, matchResult.start());
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

        private final String conditionalVariable;

        public Conditional(String contents, String conditionalVariable, int line, int offs) {
            super(contents, line, offs);
            this.conditionalVariable = conditionalVariable;
        }

        public static Creator getCreator() {
            return new Creator() {
                @Override
                public Pattern getPattern() {
                    return PATTERN;
                }

                @Override
                public Token create(MatchResult matchResult, int line) {
                    return new Conditional(matchResult.group(), matchResult.group(1), line, matchResult.start());
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

        public ConditionalElse(String contents, int line, int offs) {
            super(contents, line, offs);
        }

        public static Creator getCreator() {
            return new Creator() {
                @Override
                public Pattern getPattern() {
                    return PATTERN;
                }

                @Override
                public Token create(MatchResult matchResult, int line) {
                    return new ConditionalElse(matchResult.group(), line, matchResult.start());
                }
            };
        }
    }

    public static class ConditionalEnd extends Token {

        public static final Pattern PATTERN = Pattern.compile("\\{/if}");

        public ConditionalEnd(String contents, int line, int offs) {
            super(contents, line, offs);
        }

        public static Creator getCreator() {
            return new Creator() {
                @Override
                public Pattern getPattern() {
                    return PATTERN;
                }

                @Override
                public Token create(MatchResult matchResult, int line) {
                    return new ConditionalEnd(matchResult.group(), line, matchResult.start());
                }
            };
        }
    }

    /**
     * Literals aren't found by regExps, they're the fillers before, between and after matches.
     */
    public static class Literal extends Token {

        public Literal(String contents, int line, int offs) {
            super(contents, line, offs);
        }

    }

    public static abstract class Creator {

        public abstract Pattern getPattern();

        public abstract Token create(MatchResult matchResult, int line);

    }
}

package net.h34t.temporize;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Token creators create tokens, but not every token creator creates its own *class* of tokens.
 * <p>
 * E.g. Block tokens are always emitted by block creators, but variables can come from both the
 * Variable creator and the DefaultVariable creator.
 * <p>
 * Literal tokens can come from left-over matches (no creator) and skip tags.
 */
public abstract class TokenCreator {

    public abstract Pattern getPattern();

    public abstract Token create(MatchResult matchResult, String source, int line);

    /**
     * Emits a {@link Token.Variable} that are defined in the form of {$name|modifier1|modifier1|...},
     * where the modifiers are optional.
     * <p>
     * E.g. {$foo} or {$foo|modifier1|modifier2}
     * <p>
     * They must start with a letter and can contain letters and numbers
     */
    public static class Variable {
        public static Pattern EXP = Pattern.compile("\\{\\$([a-z][\\w]*)((\\|[\\w]+)*)}");

        public static TokenCreator getCreator() {
            return new TokenCreator() {

                @Override
                public Pattern getPattern() {
                    return EXP;
                }

                @Override
                public Token create(MatchResult matchResult, String source, int line) {
                    return new Token.Variable(matchResult.group(),
                            matchResult.group(1), matchResult.group(2), source, line, matchResult.start());
                }
            };
        }
    }

    /**
     * Emits a {@link Token.Variable} with a default modifier named "def".
     * <p>
     * This is nothing more than a shortcut notation;
     * <p>
     * <code>{*$def}</code> will create the same token as <code>{$var|def}</code>.
     * <p>
     * Note that there can still be other modifiers.
     */
    public static class DefaultVariable {

        public static Pattern EXP = Pattern.compile("\\{\\*\\$([a-z][\\w]*)((\\|[\\w]+)*)}");

        public static TokenCreator getCreator() {
            return new TokenCreator() {

                @Override
                public Pattern getPattern() {
                    return EXP;
                }

                @Override
                public Token create(MatchResult matchResult, String source, int line) {
                    return new Token.Variable(matchResult.group(),
                            matchResult.group(1), "|def" + matchResult.group(2),
                            source, line, matchResult.start());
                }
            };
        }
    }

    /**
     * Emits a {@link Token.Block} token in the form of {for $variable}.
     */
    public static class Block {

        final static Pattern pattern = Pattern.compile("\\{for\\s+\\$(\\w+)}");

        public static TokenCreator getCreator() {
            return new TokenCreator() {
                @Override
                public Pattern getPattern() {
                    return pattern;
                }

                @Override
                public Token create(MatchResult matchResult, String source, int line) {
                    return new Token.Block(matchResult.group(), matchResult.group(1), source, line, matchResult.start());
                }
            };
        }
    }

    /**
     * Emits a {@link Token.BlockEnd} token in the form of {/for}.
     */
    public static class BlockEnd {

        public static final Pattern PATTERN = Pattern.compile("\\{/for}");

        public static TokenCreator getCreator() {
            return new TokenCreator() {
                @Override
                public Pattern getPattern() {
                    return PATTERN;
                }

                @Override
                public Token create(MatchResult matchResult, String source, int line) {
                    return new Token.BlockEnd(matchResult.group(), source, line, matchResult.start());
                }
            };
        }
    }

    /**
     * Emits an {@link Token.Include} token in the form of {include a.b.c.Class as $variable}.
     */
    public static class Include {

        public static final Pattern PATTERN = Pattern.compile("\\{include\\s+(((\\w+.)*)([A-Z]\\w*))\\s+as\\s+\\$(\\w+)}");

        public static TokenCreator getCreator() {
            return new TokenCreator() {
                @Override
                public Pattern getPattern() {
                    return PATTERN;
                }

                @Override
                public Token create(MatchResult matchResult, String source, int line) {
                    return new Token.Include(matchResult.group(), matchResult.group(1), matchResult.group(5), source, line, matchResult.start());
                }
            };
        }
    }

    /**
     * Emits a {@link Token.Conditional} token in the form of <code>{if $condition}</code>.
     * <p>
     * Note that currently only a single condition is supported.
     */
    public static class Conditional {

        public static final Pattern PATTERN = Pattern.compile("\\{if\\s+\\$(\\w+)}");

        public static TokenCreator getCreator() {
            return new TokenCreator() {
                @Override
                public Pattern getPattern() {
                    return PATTERN;
                }

                @Override
                public Token create(MatchResult matchResult, String source, int line) {
                    return new Token.Conditional(matchResult.group(), matchResult.group(1), source, line, matchResult.start());
                }
            };
        }
    }

    /**
     * Emits a {@link Token.Conditional} token in the form of {else}.
     * <p>
     * Note that {else if} constructs are not supported (yet).
     */
    public static class ConditionalElse {

        public static final Pattern PATTERN = Pattern.compile("\\{else}");

        public static TokenCreator getCreator() {
            return new TokenCreator() {
                @Override
                public Pattern getPattern() {
                    return PATTERN;
                }

                @Override
                public Token create(MatchResult matchResult, String source, int line) {
                    return new Token.ConditionalElse(matchResult.group(), source, line, matchResult.start());
                }
            };
        }
    }

    /**
     * Emits a {@link Token.ConditionalEnd} token in the form of {/if}.
     */
    public static class ConditionalEnd {

        public static final Pattern PATTERN = Pattern.compile("\\{/if}");

        public static TokenCreator getCreator() {
            return new TokenCreator() {
                @Override
                public Pattern getPattern() {
                    return PATTERN;
                }

                @Override
                public Token create(MatchResult matchResult, String source, int line) {
                    return new Token.ConditionalEnd(matchResult.group(), source, line, matchResult.start());
                }
            };
        }
    }

    /**
     * Skips are special, as they don't create a skip token, but a {@link Token.Literal}.
     * <p>
     * TODO Skips are broken because they currently must open and close on a single line
     * <p>
     * Note that skip sections can't be nested.
     */
    public static class Skip {

        public static final Pattern PATTERN = Pattern.compile("\\{skip}(.*?)\\{/skip}");

        public static TokenCreator getCreator() {
            return new TokenCreator() {
                @Override
                public Pattern getPattern() {
                    return PATTERN;
                }

                @Override
                public Token create(MatchResult matchResult, String source, int line) {
                    return new Token.Literal(matchResult.group(1), source, line, matchResult.start());
                }
            };
        }
    }

    /**
     * Comment sections are completely ignored by the parser and don't emit anything.
     * <p>
     * TODO Comments are broken because they currently must open and close on a single line
     * <p>
     * Note that Comment sections can't be nested.
     */
    public static class Comment {

        public static final Pattern PATTERN = Pattern.compile("\\{comment}(.*?)\\{/comment}");

        public static TokenCreator getCreator() {
            return new TokenCreator() {
                @Override
                public Pattern getPattern() {
                    return PATTERN;
                }

                @Override
                public Token create(MatchResult matchResult, String source, int line) {
                    return new Token.Comment(matchResult.group(1), source, line, matchResult.start());
                }
            };
        }
    }
}

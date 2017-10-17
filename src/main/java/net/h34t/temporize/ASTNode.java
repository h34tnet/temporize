package net.h34t.temporize;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ASTNode {

    private final ASTNode prev;
    private ASTNode next;

    public ASTNode(ASTNode prev) {
        this.prev = prev;
    }

    private static String ident(int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++)
            sb.append("    ");

        return sb.toString();
    }

    public ASTNode getNext() {
        return next;
    }

    public void setNext(ASTNode next) {
        this.next = next;
    }

    public ASTNode getPrev() {
        return prev;
    }

    public abstract String print(int indentation);

    public boolean hasNext() {
        return this.next != null;
    }

    public ASTNode next() {
        return this.next;
    }

    public static class NoOp extends ASTNode {
        NoOp(ASTNode prev) {
            super(prev);
        }

        @Override
        public String print(int indentation) {
            return (getNext() != null) ? getNext().print(indentation) : "";
        }
    }

    public static class Conditional extends ASTNode {

        final String name;
        ASTNode consequent;
        ASTNode alternative;

        Conditional(ASTNode prev, String name) {
            super(prev);
            this.name = name;
        }

        @Override
        public String print(int indentation) {
            StringBuilder output = new StringBuilder();
            output.append(ident(indentation)).append("if ").append(name).append("\n");
            output.append(consequent.print(indentation + 1));

            if (alternative != null) {
                output.append(ident(indentation)).append("else\n");
                output.append(alternative.print(indentation + 1));
            }

            if (getNext() != null)
                output.append(getNext().print(indentation));

            return output.toString();
        }
    }

    public static class Variable extends ASTNode {

        final String name;
        final List<String> modifiers;

        Variable(ASTNode prev, String name, List<String> modifiers) {
            super(prev);
            this.name = name;
            this.modifiers = new ArrayList<>(modifiers);
        }

        @Override
        public String print(int indentation) {
            String output = ident(indentation) + "$" + name + ":" + modifiers.stream().collect(Collectors.joining("|")) + "\n";

            if (getNext() != null)
                output += getNext().print(indentation);

            return output;
        }
    }

    public static class Block extends ASTNode {

        final String blockName;
        final String blockClassName;

        ASTNode branch;

        Block(ASTNode prev, String blockName) {
            super(prev);
            this.blockName = blockName;
            this.blockClassName = Utils.toClassName(blockName);
        }

        void setBranch(ASTNode branch) {
            this.branch = branch;
        }

        @Override
        public String print(int indentation) {
            StringBuilder output = new StringBuilder();
            output.append(ident(indentation)).append("for ").append(blockName).append(":\n");
            output.append(branch.print(indentation + 1));
            if (getNext() != null)
                output.append(getNext().print(indentation));

            return output.toString();
        }
    }

    public static class Include extends ASTNode {

        final String classname;
        final String instance;

        Include(ASTNode prev, String classname, String instance) {
            super(prev);
            this.classname = classname;
            this.instance = instance;
        }

        @Override
        public String print(int indentation) {
            String output = ident(indentation) + "include(" + classname + " as " + instance + ")\n";

            if (getNext() != null)
                output += getNext().print(indentation);

            return output;
        }
    }

    public static class ConstantValue extends ASTNode {

        final String value;

        ConstantValue(ASTNode prev, String value) {
            super(prev);
            this.value = value;
        }

        @Override
        public String print(int indentation) {
            String output = ident(indentation) + "\"" + value + "\"\n";

            if (getNext() != null)
                output += getNext().print(indentation);

            return output;
        }
    }
}

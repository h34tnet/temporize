package net.h34t.temporize;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class ASTNode implements Iterator<ASTNode> {

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

    public static String toClassName(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
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

    public abstract String print(int identation);

    @Override
    public boolean hasNext() {
        return this.next != null;
    }

    @Override
    public ASTNode next() {
        return this.next;
    }

    @Override
    public void forEachRemaining(Consumer<? super ASTNode> action) {
        action.accept(this);

        if (this.next != null)
            next.forEachRemaining(action);
    }

    public static class NoOp extends ASTNode {
        public NoOp(ASTNode prev) {
            super(prev);
        }

        @Override
        public String print(int identation) {
            return (getNext() != null) ? getNext().print(identation) : "";
        }
    }

    public static class Conditional extends ASTNode {

        public final String name;
        public ASTNode consequent;
        public ASTNode alternative;

        public Conditional(ASTNode prev, String name) {
            super(prev);
            this.name = name;
        }

        public void setConsequent(ASTNode consequent) {
            this.consequent = consequent;
        }

        public void setAlternative(ASTNode alternative) {
            this.alternative = alternative;
        }

        @Override
        public String print(int identation) {
            StringBuilder output = new StringBuilder();
            output.append(ident(identation) + "if " + name + "\n");
            output.append(consequent.print(identation + 1));

            if (alternative != null) {
                output.append(ident(identation) + "else\n");
                output.append(alternative.print(identation + 1));
            }

            if (getNext() != null)
                output.append(getNext().print(identation));

            return output.toString();
        }
    }

    public static class Variable extends ASTNode {

        public final String name;
        public final List<String> modifiers;

        public Variable(ASTNode prev, String name, List<String> modifiers) {
            super(prev);
            this.name = name;
            this.modifiers = new ArrayList<>(modifiers);
        }

        @Override
        public String print(int identation) {
            String output = ident(identation) + "$" + name + ":" + modifiers.stream().collect(Collectors.joining("|")) + "\n";

            if (getNext() != null)
                output += getNext().print(identation);

            return output;
        }
    }

    public static class Block extends ASTNode {

        public final String blockName;
        public final String blockClassName;

        public ASTNode branch;

        public Block(ASTNode prev, String blockName) {
            super(prev);
            this.blockName = blockName;
            this.blockClassName = ASTNode.toClassName(blockName);
        }

        public void setBranch(ASTNode branch) {
            this.branch = branch;
        }

        @Override
        public String print(int identation) {
            StringBuilder output = new StringBuilder();
            output.append(ident(identation) + "for " + blockName + ":\n");
            output.append(branch.print(identation + 1));
            if (getNext() != null)
                output.append(getNext().print(identation));

            return output.toString();
        }
    }

    public static class Include extends ASTNode {

        public final String filename;
        public final String instance;

        public Include(ASTNode prev, String filename, String instance) {
            super(prev);
            this.filename = filename;
            this.instance = instance;
        }

        @Override
        public String print(int identation) {
            String output = ident(identation) + "include(" + filename + " as " + instance + ")\n";

            if (getNext() != null)
                output += getNext().print(identation);

            return output;
        }
    }

    public static class ConstantValue extends ASTNode {

        public final String value;

        public ConstantValue(ASTNode prev, String value) {
            super(prev);
            this.value = value;
        }

        @Override
        public String print(int identation) {
            String output = ident(identation) + "\"" + value + "\"\n";

            if (getNext() != null)
                output += getNext().print(identation);

            return output;
        }
    }
}

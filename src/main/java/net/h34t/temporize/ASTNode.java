package net.h34t.temporize;

import java.util.ArrayList;
import java.util.List;

public abstract class ASTNode {

    public ASTNode() {
    }


    public static class BooleanValue {

        public final String variableName;

        public BooleanValue(String variableName) {
            this.variableName = variableName;
        }
    }

    public static class Conditional extends ASTNode {

        public final BooleanValue condition;
        public ASTNode consequent;
        public ASTNode alternative;

        public Conditional(BooleanValue condition) {
            this.condition = condition;
        }

        public void setConsequent(ASTNode consequent) {
            this.consequent = consequent;
        }

        public void setAlternative(ASTNode alternative) {
            this.alternative = alternative;
        }

    }

    public static class Sequence extends ASTNode {

        public final List<ASTNode> nodes;

        public Sequence() {
            this.nodes = new ArrayList<>();
        }

        public Sequence add(ASTNode node) {
            this.nodes.add(node);
            return this;
        }
    }

    public static class Variable extends ASTNode {

        public final String name;
        public final List<String> modifiers;

        public Variable(String name, List<String> modifiers) {
            this.name = name;
            this.modifiers = new ArrayList<>(modifiers);
        }
    }

    public static class Block extends ASTNode {

        public final String blockName;
        public ASTNode node;

        public Block(String blockName, ASTNode node) {
            this.blockName = blockName;
            this.node = node;
        }
    }

    public static class Include extends ASTNode {

        public final String filename;

        public Include(String filename) {
            this.filename = filename;
        }
    }

    public static class ConstantValue extends ASTNode {

        public final String value;

        public ConstantValue(String value) {
            this.value = value;
        }
    }
}

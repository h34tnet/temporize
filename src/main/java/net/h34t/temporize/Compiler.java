package net.h34t.temporize;

import java.util.ArrayList;
import java.util.List;

public class Compiler {

    public Compiler() {

    }

    public static List<ASTNode.Variable> getVariables(ASTNode node) {
        if (node == null) {
            return new ArrayList<>();

        } else if (node instanceof ASTNode.Variable) {
            List<ASTNode.Variable> variables = new ArrayList<>();
            variables.add((ASTNode.Variable) node);
            variables.addAll(getVariables(node.getNext()));
            return variables;

        } else if (node instanceof ASTNode.Conditional) {
            List<ASTNode.Variable> variables = new ArrayList<>();
            variables.addAll(getVariables(((ASTNode.Conditional) node).consequent));

            if (((ASTNode.Conditional) node).alternative != null)
                variables.addAll(getVariables(((ASTNode.Conditional) node).alternative));

            variables.addAll(getVariables(node.next()));

            return variables;

        } else {
            return getVariables(node.getNext());

        }
    }

    public String compile(String packageName, String className, ASTNode root) {
        // for
        List<ASTNode.Variable> variables = getVariables(root);

        StringBuilder sb = new StringBuilder();

        sb.append("public class ").append(className).append("{\n\n");
        for (ASTNode.Variable var : variables)
            sb.append("    private final String ").append(var.name).append(";\n");

        sb.append("}");

        return sb.toString();
    }

}

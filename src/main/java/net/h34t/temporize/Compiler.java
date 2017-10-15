package net.h34t.temporize;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        List<ASTNode.Variable> variables = getVariables(root).stream()
                .distinct()
                .collect(Collectors.toList());

        List<String> variableNames = variables.stream().map(v -> v.name).distinct().collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        if (packageName != null)
            sb.append("package ").append(packageName).append(";\n\n");
        sb.append("public class ").append(className).append(" {\n\n");

        for (String var : variableNames)
            sb.append("    private String ").append(var).append(";\n");

        sb.append("\n");

        sb.append("    public " + className + "() {\n");
        sb.append("    }\n\n");


        sb.append("    public " + className + "(");
        sb.append(variableNames.stream().map(v -> "String " + v).collect(Collectors.joining(", ")));
        sb.append(") {\n");
        variableNames.forEach(v -> sb.append("        this.").append(v).append(" = ").append(v).append(";\n"));
        sb.append("    }\n\n");


        sb.append("}");


        return sb.toString();
    }

}

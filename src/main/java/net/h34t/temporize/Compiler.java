package net.h34t.temporize;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.h34t.temporize.ASTNode.toClassName;

public class Compiler {

    public Compiler() {
    }

    static List<ASTNode.Variable> getVariables(ASTNode node) {
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

    static List<ASTNode.Block> getBlocks(ASTNode node) {
        if (node == null) {
            return new ArrayList<>();

        } else if (node instanceof ASTNode.Block) {
            List<ASTNode.Block> blocks = new ArrayList<>();
            blocks.add((ASTNode.Block) node);
            blocks.addAll(getBlocks(node.next()));
            return blocks;

        } else if (node instanceof ASTNode.Conditional) {
            List<ASTNode.Block> blocks = new ArrayList<>();
            blocks.addAll(getBlocks(((ASTNode.Conditional) node).consequent));

            if (((ASTNode.Conditional) node).alternative != null)
                blocks.addAll(getBlocks(((ASTNode.Conditional) node).alternative));

            blocks.addAll(getBlocks(node.next()));

            return blocks;

        } else {
            return getBlocks(node.getNext());
        }
    }

    static String createVariableSetter(String className, String name, int ident) {
        return Ident.of(ident) + "    public " + className + " set" + toClassName(name) + "(String " + name + ") {\n" +
                Ident.of(ident) + "        this." + name + " = " + name + ";\n" +
                Ident.of(ident) + "        return this;\n" +
                Ident.of(ident) + "    }";
    }

    static String createBlockSetter(String className, ASTNode.Block block, int ident) {
        return Ident.of(ident) + "    public " + className + " set" + block.blockClassName + "(List<" + block.blockClassName + "> " + block.blockName + ") {\n" +
                Ident.of(ident) + "        this." + block.blockName + " = " + block.blockName + ";\n" +
                Ident.of(ident) + "        return this;\n" +
                Ident.of(ident) + "    }";
    }

    static String createOutput(ASTNode node, int ident) {
        if (node == null) {
            return "";

        } else if (node instanceof ASTNode.NoOp) {
            return createOutput(node.next(), ident);
        }
        if (node instanceof ASTNode.ConstantValue) {
            return Ident.of(ident) + "sb.append(\"" + StringEscapeUtils.escapeJava(((ASTNode.ConstantValue) node).value) + "\");\n"
                    + createOutput(node.next(), ident);

        } else if (node instanceof ASTNode.Variable) {
            return Ident.of(ident) + "sb.append(this." + ((ASTNode.Variable) node).name + ");\n"
                    + createOutput(node.next(), ident);

        } else if (node instanceof ASTNode.Conditional) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n").append(Ident.of(ident)).append("if (this.").append(((ASTNode.Conditional) node).name).append(" != null && !").append(((ASTNode.Conditional) node).name).append(".isEmpty()) {\n");
            sb.append(createOutput(((ASTNode.Conditional) node).consequent, ident + 1));

            if (((ASTNode.Conditional) node).alternative != null) {
                sb.append("\n").append(Ident.of(ident)).append("} else {\n");
                sb.append(createOutput(((ASTNode.Conditional) node).alternative, ident + 1));
            }

            sb.append(Ident.of(ident)).append("}\n\n");
            return sb.toString() + createOutput(node.next(), ident);

        } else if (node instanceof ASTNode.Block) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n").append(Ident.of(ident) + "for (" + ((ASTNode.Block) node).blockClassName + " _block : " + ((ASTNode.Block) node).blockName + "List)\n");
            sb.append(Ident.of(ident + 1) + "sb.append(_block.toString());\n");
            return sb.toString();

        } else {
            throw new RuntimeException("Undefined ASTNode " + node.getClass().getName());

        }
    }

    public Template compile(String packageName, String className, ASTNode root) {
        return compile(packageName, className, root, 0);
    }


    Template compile(String packageName, String className, ASTNode root, int ident) {
        List<ASTNode.Variable> variables = getVariables(root).stream()
                .distinct()
                .collect(Collectors.toList());

        List<String> variableNames = variables.stream()
                .map(v -> v.name)
                .distinct()
                .collect(Collectors.toList());

        List<ASTNode.Block> blocks = getBlocks(root);

        List<String> constructorInitializers =
                variables.stream().map(v -> "String " + v.name)
                        .collect(Collectors.toList());

        constructorInitializers.addAll(blocks.stream()
                .map(b -> "List<" + b.blockClassName + "> " + b.blockName).collect(Collectors.toList()));


        StringBuilder sb = new StringBuilder();
        if (packageName != null) {
            sb.append("package ").append(packageName).append(";\n\n");
            sb.append("import java.util.List;\n\n");
        }

        sb.append(Ident.of(ident)).append("public " + (packageName != null ? "" : "static ") + "class ").append(className).append(" {\n\n");

        // variable property definitions
        for (String var : variableNames)
            sb.append(Ident.of(ident)).append("    private String ").append(var).append(";\n");

        // block property definitions
        for (ASTNode.Block block : blocks)
            sb.append(Ident.of(ident)).append("    private List<").append(block.blockClassName).append("> ").append(block.blockName).append(";\n");

        sb.append("\n");

        // empty constructor
        sb.append(Ident.of(ident)).append("    public ").append(className).append("() {\n");
        sb.append(Ident.of(ident)).append("    }\n\n");


        // full constructor
        sb.append(Ident.of(ident)).append("    public ").append(className).append("(");
        sb.append(constructorInitializers.stream()
                .collect(Collectors.joining(", ")))
                .append(") {\n");
        variableNames.forEach(v -> sb.append(Ident.of(ident + 2)).append("this.").append(v).append(" = ").append(v).append(";\n"));
        blocks.forEach(b -> sb.append(Ident.of(ident + 2)).append("this.").append(b.blockName).append(" = ").append(b.blockName).append(";\n"));
        sb.append(Ident.of(ident + 1)).append("}\n\n");

        // variable setters
        for (String var : variableNames)
            sb.append(createVariableSetter(className, var, ident)).append("\n\n");

        // block setters
        for (ASTNode.Block block : blocks)
            sb.append(Ident.of(ident)).append(createBlockSetter(className, block, ident)).append("\n\n");

        // output body
        sb.append(Ident.of(ident)).append("    @Override\n");
        sb.append(Ident.of(ident)).append("    public String toString() {\n");
        sb.append(Ident.of(ident)).append("        StringBuilder sb = new StringBuilder();\n");

        sb.append(createOutput(root, ident + 2));

        sb.append(Ident.of(ident)).append("\n");
        sb.append(Ident.of(ident)).append("        return sb.toString();\n");
        sb.append(Ident.of(ident)).append("    }\n");

        for (ASTNode.Block block : blocks) {

            sb.append("\n").append(compile(null, block.blockClassName, block.branch, ident + 1));
        }

        sb.append(Ident.of(ident)).append("}\n");

        return new Template(
                packageName,
                className,
                sb.toString());
    }

}

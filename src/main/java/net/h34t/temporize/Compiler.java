package net.h34t.temporize;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Compiler {

    public Compiler() {
    }

    static <A extends ASTNode> List<A> getNodesOf(ASTNode node, Class<A> aClass) {
        if (node == null) {
            return new ArrayList<>();
        }

        List<A> nodes = new ArrayList<>();
        if (node.getClass().isAssignableFrom(aClass)) {
            nodes.add((A) node);
        }

        if (node instanceof ASTNode.Conditional) {
            nodes.addAll(getNodesOf(((ASTNode.Conditional) node).consequent, aClass));

            if (((ASTNode.Conditional) node).alternative != null)
                nodes.addAll(getNodesOf(((ASTNode.Conditional) node).alternative, aClass));
        }

        nodes.addAll(getNodesOf(node.next(), aClass));

        return nodes;

    }

    static String createSetter(String className, String type, String instanceName, int ident) {
        return Ident.of(ident) + "    public " + className + " set" + ASTNode.toClassName(instanceName) + "(" + type + " " + instanceName + ") {\n" +
                Ident.of(ident) + "        this." + instanceName + " = " + instanceName + ";\n" +
                Ident.of(ident) + "        return this;\n" +
                Ident.of(ident) + "    }";
    }

    /**
     * This generates .toString() calls for every statement in the body.
     * <p>
     * It doesn't traverse Block bodies, which are compiled as a subclass.
     *
     * @param node   the node to traverse
     * @param indent level of indentation
     * @return the toString() body as a string of java code
     */
    static String createOutput(ASTNode node, int indent) {
        if (node == null) {
            return "";

        } else if (node instanceof ASTNode.NoOp) {
            return createOutput(node.next(), indent);
        }
        if (node instanceof ASTNode.ConstantValue) {
            return Ident.of(indent) + "sb.append(\"" + StringEscapeUtils.escapeJava(((ASTNode.ConstantValue) node).value) + "\");\n"
                    + createOutput(node.next(), indent);

        } else if (node instanceof ASTNode.Variable) {
            StringBuilder variable = new StringBuilder("this." + ((ASTNode.Variable) node).name);
            for (String modifier : ((ASTNode.Variable) node).modifiers)
                variable = new StringBuilder(modifier + "(" + variable + ")");

            return Ident.of(indent) + "sb.append(" + variable.toString() + ");\n"
                    + createOutput(node.next(), indent);

        } else if (node instanceof ASTNode.Conditional) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n").append(Ident.of(indent)).append("if (this.").append(((ASTNode.Conditional) node).name).append(" != null && !").append(((ASTNode.Conditional) node).name).append(".isEmpty()) {\n");
            sb.append(createOutput(((ASTNode.Conditional) node).consequent, indent + 1));

            if (((ASTNode.Conditional) node).alternative != null) {
                sb.append("\n").append(Ident.of(indent)).append("} else {\n");
                sb.append(createOutput(((ASTNode.Conditional) node).alternative, indent + 1));
            }

            sb.append(Ident.of(indent)).append("}\n\n");
            sb.append(createOutput(node.next(), indent));
            return sb.toString();

        } else if (node instanceof ASTNode.Block) {
            return "\n" + Ident.of(indent) + "for (" + ((ASTNode.Block) node).blockClassName + " _block : " + ((ASTNode.Block) node).blockName + ")\n" +
                    Ident.of(indent + 1) + "sb.append(_block.toString());\n"
                    + createOutput(node.next(), indent);

        } else if (node instanceof ASTNode.Include) {
            return Ident.of(indent) + "sb.append(" + ((ASTNode.Include) node).instance + ".toString());\n" +
                    createOutput(node.next(), indent);

        } else {
            throw new RuntimeException("Undefined ASTNode " + node.getClass().getName());

        }
    }

    public Template compile(String packageName, String className, String modifier, ASTNode root, Consumer<String> includeHandler) {
        return compile(packageName, className, modifier, root, 0, includeHandler);
    }


    Template compile(String packageName, String className, String modifier, ASTNode root, int ident, Consumer<String> includeHandler) {
        List<ASTNode.Variable> variables = getNodesOf(root, ASTNode.Variable.class);
        List<ASTNode.Block> blocks = getNodesOf(root, ASTNode.Block.class);
        List<ASTNode.Include> includes = getNodesOf(root, ASTNode.Include.class);

        // process compilation of includes from the outside
        // note: this may generate infinite loops
        for (ASTNode.Include include : includes)
            includeHandler.accept(include.filename);

        // extract the variable names
        List<String> variableNames = variables.stream()
                .map(v -> v.name)
                .distinct()
                .collect(Collectors.toList());

        List<String> constructorInitializers =
                variables.stream().map(v -> "String " + v.name)
                        .distinct()
                        .collect(Collectors.toList());

        constructorInitializers.addAll(blocks.stream()
                .map(b -> "List<" + b.blockClassName + "> " + b.blockName).collect(Collectors.toList()));

        constructorInitializers.addAll(includes.stream()
                .map(i -> i.filename + " " + i.instance).collect(Collectors.toList()));

        StringBuilder sb = new StringBuilder();
        if (packageName != null) {
            sb.append("package ").append(packageName).append(";\n\n");
            sb.append("import java.util.List;\n");
            if (modifier != null)
                sb.append("import static ").append(modifier).append(";\n");
            sb.append("\n");
        }

        sb.append(Ident.of(ident)).append("public ").append(packageName != null ? "" : "static ").append("class ").append(className).append(" {\n\n");

        // variable property definitions
        for (String var : variableNames)
            sb.append(Ident.of(ident)).append("    private String ").append(var).append(" = \"\";\n");

        // block property definitions
        for (ASTNode.Block block : blocks)
            sb.append(Ident.of(ident)).append("    private List<").append(block.blockClassName).append("> ").append(block.blockName).append(" = new ArrayList<>();\n");

        // block property definitions
        for (ASTNode.Include inc : includes)
            sb.append(Ident.of(ident)).append("    private ").append(inc.filename).append(" ").append(inc.instance).append(";\n");


        sb.append("\n");

        // empty constructor
        sb.append(Ident.of(ident)).append("    public ").append(className).append("() {\n");
        sb.append(Ident.of(ident)).append("    }\n\n");


        // full constructor
        if (constructorInitializers.size() > 0) {
            sb.append(Ident.of(ident)).append("    public ").append(className).append("(");
            sb.append(constructorInitializers.stream()
                    .collect(Collectors.joining(", ")))
                    .append(") {\n");
            variableNames.forEach(v -> sb.append(Ident.of(ident + 2)).append("this.").append(v).append(" = ").append(v).append(";\n"));
            blocks.forEach(b -> sb.append(Ident.of(ident + 2)).append("this.").append(b.blockName).append(" = ").append(b.blockName).append(";\n"));
            includes.forEach(b -> sb.append(Ident.of(ident + 2)).append("this.").append(b.instance).append(" = ").append(b.instance).append(";\n"));
            sb.append(Ident.of(ident + 1)).append("}\n\n");
        }

        // variable setters
        for (String var : variableNames)
            sb.append(createSetter(className, "String", var, ident)).append("\n\n");

        // block setters
        for (ASTNode.Block block : blocks)
            sb.append(Ident.of(ident)).append(createSetter(className, "List<" + block.blockClassName + ">", block.blockName, ident)).append("\n\n");

        // include setters
        for (ASTNode.Include elem : includes)
            sb.append(Ident.of(ident)).append(createSetter(className, elem.filename, elem.instance, ident)).append("\n\n");

        // output body
        sb.append(Ident.of(ident)).append("    @Override\n");
        sb.append(Ident.of(ident)).append("    public String toString() {\n");
        sb.append(Ident.of(ident)).append("        StringBuilder sb = new StringBuilder();\n");

        sb.append(createOutput(root, ident + 2));

        sb.append(Ident.of(ident)).append("\n");
        sb.append(Ident.of(ident)).append("        return sb.toString();\n");
        sb.append(Ident.of(ident)).append("    }\n");

        for (ASTNode.Block block : blocks) {
            sb.append("\n").append(compile(null, block.blockClassName, modifier, block.branch, ident + 1, includeHandler));
        }

        sb.append(Ident.of(ident)).append("}\n");

        return new Template(
                packageName,
                className,
                sb.toString());
    }

}

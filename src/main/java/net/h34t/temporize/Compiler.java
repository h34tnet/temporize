package net.h34t.temporize;

import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.EntityArrays;
import org.apache.commons.text.translate.LookupTranslator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Compiler {

    public static final CharSequenceTranslator ESCAPE_JAVA =
            new LookupTranslator(
                    new HashMap<CharSequence, CharSequence>() {{
                        put("\"", "\\\"");
                        put("\\", "\\\\");
                    }}
            ).with(new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE));


    // .with(
    // new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE()))


    public Compiler() {
    }

    /**
     * @param node   the root node
     * @param aClass the class of the nodes to find
     * @param <A>    the type of the nodes to find
     * @return all nodes of type A in the current context
     */
    static <A extends ASTNode> List<A> getNodesOf(ASTNode node, Class<A> aClass) {
        if (node == null) {
            return new ArrayList<>();
        }

        List<A> nodes = new ArrayList<>();
        if (node.getClass().isAssignableFrom(aClass)) {
            //noinspection unchecked
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
        return Ident.of(ident) + "    public " + className + " set" + Utils.toClassName(instanceName) + "(" + type + " " + instanceName + ") {\n" +
                Ident.of(ident) + "        this." + instanceName + " = " + instanceName + ";\n" +
                Ident.of(ident) + "        return this;\n" +
                Ident.of(ident) + "    }";

    }

    static String createStringValueSetter(String className, String type, String instanceName, int ident) {
        return Ident.of(ident) + "    public " + className + " set" + Utils.toClassName(instanceName) + "(" + type + " " + instanceName + ") {\n" +
                Ident.of(ident) + "        this." + instanceName + " = String.valueOf(" + instanceName + ");\n" +
                Ident.of(ident) + "        return this;\n" +
                Ident.of(ident) + "    }";
    }

    static String createTemplateSetter(String className, String instanceName, int ident) {
        return Ident.of(ident) + "    public " + className + " set" + Utils.toClassName(instanceName) + "(temporize.TemporizeTemplate " + instanceName + ") {\n" +
                Ident.of(ident) + "        this." + instanceName + " = " + instanceName + ".toString();\n" +
                Ident.of(ident) + "        return this;\n" +
                Ident.of(ident) + "    }";
    }

    static List<ASTNode> getNodesInContext(ASTNode current) {
        List<ASTNode> nodes = new ArrayList<>();

        while (current != null) {
            if (current instanceof ASTNode.Conditional) {
                nodes.addAll(getNodesInContext(((ASTNode.Conditional) current).consequent));
                if (((ASTNode.Conditional) current).alternative != null)
                    nodes.addAll(getNodesInContext(((ASTNode.Conditional) current).alternative));
            } else
                nodes.add(current);

            current = current.next();
        }

        return nodes;
    }

    static ConditionalType getType(String name, List<ASTNode> nodesInContext) {
        for (ASTNode node : nodesInContext)
            if (node instanceof ASTNode.Variable && ((ASTNode.Variable) node).name.equals(name))
                return ConditionalType.STRING;
            else if (node instanceof ASTNode.Block && ((ASTNode.Block) node).blockName.equals(name))
                return ConditionalType.BLOCK;
            else if (node instanceof ASTNode.Include && ((ASTNode.Include) node).instance.equals(name))
                return ConditionalType.INCLUDE;

        return ConditionalType.BOOLEAN;
    }

    /**
     * This generates .toString() calls for every statement in the body.
     * <p>
     * It doesn't traverse Block bodies, which are compiled as a subclass.
     *
     * @param node     the node to traverse
     * @param fnOutput callback that takes the expression to write and creates a writing command
     * @param indent   level of indentation
     * @return the toString() body as a string of java code
     */
    static String createStringOutput(ASTNode node, Function<String, String> fnOutput, Function<String, String> fnInclude, int indent) {
        if (node == null) {
            return "";
        }

        List<ASTNode> nodesInContext = getNodesInContext(node);

        if (node instanceof ASTNode.NoOp) {
            return createStringOutput(node.next(), fnOutput, fnInclude, indent);

        } else if (node instanceof ASTNode.ConstantValue) {
            return Ident.of(indent)
                    + fnOutput.apply("\"" + ESCAPE_JAVA.translate(((ASTNode.ConstantValue) node).value) + "\"") + ";\n"
                    + createStringOutput(node.next(), fnOutput, fnInclude, indent);

        } else if (node instanceof ASTNode.Variable) {
            StringBuilder variable = new StringBuilder("this." + ((ASTNode.Variable) node).name);
            for (String modifier : ((ASTNode.Variable) node).modifiers)
                variable = new StringBuilder("TemporizeTemplate." + modifier + "(" + variable + ")");

            return Ident.of(indent) + fnOutput.apply(variable.toString()) + ";\n"
                    + createStringOutput(node.next(), fnOutput, fnInclude, indent);

        } else if (node instanceof ASTNode.Conditional) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n").append(Ident.of(indent));
            ConditionalType type = getType(((ASTNode.Conditional) node).name, nodesInContext);

            switch (type) {
                case STRING:
                    sb.append("if (this.").append(((ASTNode.Conditional) node).name).append(" != null && !").append(((ASTNode.Conditional) node).name).append(".isEmpty()) {\n");
                    break;
                case BLOCK:
                    sb.append("if (this.").append(((ASTNode.Conditional) node).name).append(" != null && !").append(((ASTNode.Conditional) node).name).append(".isEmpty()) {\n");
                    break;
                case INCLUDE:
                    sb.append("if (this.").append(((ASTNode.Conditional) node).name).append(" != null) {\n");
                    break;
                case BOOLEAN:
                    sb.append("if (this.").append(((ASTNode.Conditional) node).name).append(") {\n");
                    break;
            }

            sb.append(createStringOutput(((ASTNode.Conditional) node).consequent, fnOutput, fnInclude, indent + 1));

            if (((ASTNode.Conditional) node).alternative != null) {
                sb.append("\n").append(Ident.of(indent)).append("} else {\n");
                sb.append(createStringOutput(((ASTNode.Conditional) node).alternative, fnOutput, fnInclude, indent + 1));
            }

            sb.append(Ident.of(indent)).append("}\n\n");
            sb.append(createStringOutput(node.next(), fnOutput, fnInclude, indent));
            return sb.toString();

        } else if (node instanceof ASTNode.Block) {
            return "\n" + Ident.of(indent) + "for (" + ((ASTNode.Block) node).blockClassName + " _block : " + ((ASTNode.Block) node).blockName + ")\n" +
                    Ident.of(indent + 1) + fnInclude.apply("_block") + ";\n"
                    + createStringOutput(node.next(), fnOutput, fnInclude, indent);

        } else if (node instanceof ASTNode.Include) {
            return Ident.of(indent) + fnInclude.apply(((ASTNode.Include) node).instance) + ";\n" +
                    createStringOutput(node.next(), fnOutput, fnInclude, indent);

        } else {
            throw new RuntimeException("Undefined ASTNode " + node.getClass().getName());

        }
    }

    public Template compile(String packageName, String className, ASTNode root, Consumer<String> includeHandler) {
        return compile(packageName, className, root, 0, includeHandler);
    }


    Template compile(String packageName, String className, ASTNode root, int ident, Consumer<String> includeHandler) {
        // variables, blocks and includes are all defining values
        List<ASTNode.Variable> variables = getNodesOf(root, ASTNode.Variable.class);
        List<ASTNode.Block> blocks = getNodesOf(root, ASTNode.Block.class);
        List<ASTNode.Include> includes = getNodesOf(root, ASTNode.Include.class);
        List<ASTNode.Conditional> conditionals = getNodesOf(root, ASTNode.Conditional.class);

        // process compilation of includes from the outside
        // note: this may generate infinite loops
        for (ASTNode.Include include : includes)
            includeHandler.accept(include.classname);

        // extract the variable names
        Set<String> variableNames = variables.stream().map(e -> Utils.normalizeVarName(e.name)).distinct().collect(Collectors.toSet());
        List<String> blockNames = blocks.stream().map(e -> Utils.normalizeVarName(e.blockName)).collect(Collectors.toList());
        List<String> includeNames = includes.stream().map(e -> Utils.normalizeVarName(e.instance)).collect(Collectors.toList());

        List<String> conditionalValues = conditionals.stream()
                .filter(c -> !variableNames.contains(c.name) && !blockNames.contains(c.name) && !includeNames.contains(c.name))
                .map(c -> c.name)
                .collect(Collectors.toList());

        if (Utils.containsDuplicates(blockNames))
            throw new RuntimeException("Block variables must be unique:" + blockNames.stream().collect(Collectors.joining(", ")));

        if (Utils.containsDuplicates(includeNames))
            throw new RuntimeException("Include variables must be unique: " + includeNames.stream().collect(Collectors.joining(", ")));

        List<String> blockVarCollissions = blockNames.stream().filter(variableNames::contains).collect(Collectors.toList());
        if (blockVarCollissions.size() > 0)
            throw new RuntimeException("Blocks " + blockVarCollissions.stream().collect(Collectors.joining(", ")) + " are already defined as variables.");

        List<String> includeVarCollissions = includeNames.stream().filter(variableNames::contains).collect(Collectors.toList());
        if (includeVarCollissions.size() > 0)
            throw new RuntimeException("Includes " + includeVarCollissions.stream().collect(Collectors.joining(", ")) + " are already defined as variables.");

        List<String> blockIncludeCollisions = includeNames.stream().filter(blockNames::contains).collect(Collectors.toList());
        if (blockIncludeCollisions.size() > 0)
            throw new RuntimeException("Includes " + blockIncludeCollisions.stream().collect(Collectors.joining(", ")) + " are already defined as blocks.");

        // constructor parameters for variables
        List<String> constructorInitializers =
                variables.stream().map(v -> "String " + v.name)
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());

        // constructor parameters for blocks
        constructorInitializers.addAll(blocks.stream()
                .map(b -> "List<" + b.blockClassName + "> " + b.blockName).collect(Collectors.toList()));

        // constructor parameters for includes
        constructorInitializers.addAll(includes.stream()
                .map(i -> i.classname + " " + i.instance).collect(Collectors.toList()));

        // constructor parameters for includes
        constructorInitializers.addAll(conditionalValues.stream()
                .map(c -> "boolean " + c).collect(Collectors.toList()));

        StringBuilder sb = new StringBuilder();
        if (packageName != null) {
            sb.append("package ").append(packageName).append(";\n\n");
            if (!blocks.isEmpty()) {
                sb.append("import java.util.List;\n");
                sb.append("import java.util.ArrayList;\n\n");
            }
        }

        sb.append(Ident.of(ident)).append("public ").append(packageName != null ? "" : "static ").append("class ").append(className).append(" implements temporize.TemporizeTemplate {\n\n");

        // variable property definitions
        for (String var : variableNames)
            sb.append(Ident.of(ident)).append("    private String ").append(var).append(" = \"\";\n");

        // block property definitions
        for (ASTNode.Block block : blocks)
            sb.append(Ident.of(ident)).append("    private List<").append(block.blockClassName).append("> ").append(block.blockName).append(" = new ArrayList<>();\n");

        // include property definitions
        for (ASTNode.Include inc : includes)
            sb.append(Ident.of(ident)).append("    private ").append(inc.classname).append(" ").append(inc.instance).append(";\n");

        // conditional property definitions
        for (String cond : conditionalValues)
            sb.append(Ident.of(ident)).append("    private boolean ").append(cond).append(";\n");


        sb.append("\n");

        // empty constructor
        sb.append(Ident.of(ident)).append("    public ").append(className).append("() {\n");
        sb.append(Ident.of(ident)).append("    }\n\n");


        // full constructor
        if (!constructorInitializers.isEmpty()) {
            sb.append(Ident.of(ident)).append("    public ").append(className).append("(");
            sb.append(constructorInitializers.stream()
                    .collect(Collectors.joining(", ")))
                    .append(") {\n");
            variableNames.forEach(v -> sb.append(Ident.of(ident + 2)).append("this.").append(v).append(" = ").append(v).append(";\n"));
            blocks.forEach(b -> sb.append(Ident.of(ident + 2)).append("this.").append(b.blockName).append(" = ").append(b.blockName).append(";\n"));
            includes.forEach(b -> sb.append(Ident.of(ident + 2)).append("this.").append(b.instance).append(" = ").append(b.instance).append(";\n"));
            conditionalValues.forEach(c -> sb.append(Ident.of(ident + 2)).append("this.").append(c).append(" = ").append(c).append(";\n"));
            sb.append(Ident.of(ident + 1)).append("}\n\n");
        }

        // variable setters
        for (String var : variableNames) {
            sb.append(createSetter(className, "String", var, ident)).append("\n\n");
            sb.append(createStringValueSetter(className, "long", var, ident)).append("\n\n");
            sb.append(createTemplateSetter(className, var, ident)).append("\n\n");
        }

        // block setters
        for (ASTNode.Block block : blocks)
            sb.append(Ident.of(ident)).append(createSetter(className, "List<" + block.blockClassName + ">", block.blockName, ident)).append("\n\n");

        // include setters
        for (ASTNode.Include elem : includes)
            sb.append(Ident.of(ident)).append(createSetter(className, elem.classname, elem.instance, ident)).append("\n\n");

        // conditional setters
        for (String elem : conditionalValues)
            sb.append(Ident.of(ident)).append(createSetter(className, "boolean", elem, ident)).append("\n\n");

        // output body
        sb.append(Ident.of(ident)).append("    @Override\n");
        sb.append(Ident.of(ident)).append("    public String toString() {\n");
        sb.append(Ident.of(ident)).append("        try {\n");
        sb.append(Ident.of(ident)).append("            java.io.Writer w = new java.io.StringWriter();\n");
        sb.append(Ident.of(ident)).append("            write(w);\n");
        sb.append(Ident.of(ident)).append("            return w.toString();\n");
        sb.append(Ident.of(ident)).append("        } catch (java.io.IOException e) {\n");
        sb.append(Ident.of(ident)).append("            throw new RuntimeException(e);\n");
        sb.append(Ident.of(ident)).append("        }\n");
        sb.append(Ident.of(ident)).append("    }\n\n");

        // output body
        sb.append(Ident.of(ident)).append("    @Override\n");
        sb.append(Ident.of(ident)).append("    public void write(java.io.Writer w) throws java.io.IOException {\n");
        sb.append(createStringOutput(root,
                s -> "w.write(" + s + ")",
                s -> s + ".write(w)",
                ident + 2));
        sb.append(Ident.of(ident)).append("    }\n");


        for (ASTNode.Block block : blocks) {
            sb.append("\n").append(compile(null, block.blockClassName, block.branch, ident + 1, includeHandler));
        }

        sb.append(Ident.of(ident)).append("}\n");

        return new Template(
                packageName,
                className,
                sb.toString());
    }

}

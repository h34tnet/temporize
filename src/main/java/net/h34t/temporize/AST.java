package net.h34t.temporize;

import java.util.Arrays;
import java.util.List;

public class AST {

    private ASTNode.Sequence sequence;

    public AST() {
        sequence = new ASTNode.Sequence();
    }


    public static AST generate(List<Token> tokens) {
        AST root = new AST();
        AST ast = root;

        for (Token token : tokens) {
            if (token instanceof Token.Literal) {
                ast.sequence.add(new ASTNode.ConstantValue(token.contents));

            } else if (token instanceof Token.Variable) {
                ast.sequence.add(new ASTNode.Variable(((Token.Variable) token).variableName, Arrays.asList(((Token.Variable) token).modifiers)));

            } else if (token instanceof Token.Block) {
                ast.sequence.add(new ASTNode.Block(((Token.Block) token).blockName, null));

            }

        }
        return ast;
    }
}
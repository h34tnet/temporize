package net.h34t.temporize;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class ASTBuilder {

    public ASTBuilder() {
    }

    public ASTNode build(List<Token> tokens) {
        ASTNode previous = new ASTNode.NoOp(null);
        final ASTNode root = previous;

        // the stack stores entry points of branching structures (i.e. blocks and conditionals)
        Stack<ASTNode> stack = new Stack<>();

        ASTNode current;

        for (Token token : tokens) {
            if (token instanceof Token.Variable) {
                current = new ASTNode.Variable(previous, ((Token.Variable) token).variableName, Arrays.asList(((Token.Variable) token).modifiers));
                previous.setNext(current);

            } else if (token instanceof Token.Literal) {
                current = new ASTNode.ConstantValue(previous, token.contents);
                previous.setNext(current);

            } else if (token instanceof Token.Include) {
                current = new ASTNode.Include(previous, ((Token.Include) token).includeName, ((Token.Include) token).instanceName);
                previous.setNext(current);

            } else if (token instanceof Token.Block) {
                // create a block and a NOP that can be used as a new current
                ASTNode.Block block = new ASTNode.Block(previous, ((Token.Block) token).blockName);
                previous.setNext(block);
                ASTNode branch = new ASTNode.NoOp(block);
                block.setBranch(branch);
                current = branch;

                // add the block to the stack
                stack.push(block);

            } else if (token instanceof Token.BlockEnd) {
                // and end block doesn't generate its own AST node

                if (stack.empty() || !(stack.peek() instanceof ASTNode.Block))
                    throw new UnmatchedBlockException("Block End without corresponding Block Opener at " + token.line + ":" + token.offs);

                current = stack.pop();

            } else if (token instanceof Token.Conditional) {
                ASTNode.Conditional conditional = new ASTNode.Conditional(previous, ((Token.Conditional) token).conditionalVariable);
                previous.setNext(conditional);

                ASTNode consequence = new ASTNode.NoOp(conditional);
                conditional.consequent = consequence;
                current = consequence;

                stack.push(conditional);

            } else if (token instanceof Token.ConditionalElse) {
                if (stack.empty() || !(stack.peek() instanceof ASTNode.Conditional))
                    throw new MismatchedBranchException("Else without corresponding Conditional-If at " + token.line + ":" + token.offs);

                current = stack.peek();

                if (((ASTNode.Conditional) current).alternative != null) {
                    throw new MismatchedBranchException("Double Else branching at " + token.line + ":" + token.offs);
                }

                ASTNode.NoOp elseNode = new ASTNode.NoOp(current);
                ((ASTNode.Conditional) current).alternative = elseNode;
                current = elseNode;

            } else if (token instanceof Token.ConditionalEnd) {
                if (stack.empty() || !(stack.peek() instanceof ASTNode.Conditional))
                    throw new MismatchedBranchException("Conditional-End without corresponding Conditional-If at " + token.line + ":" + token.offs);

                current = stack.pop();

            } else {
                throw new RuntimeException("Unhandled token type: " + token.getClass().getName());
            }

            previous = current;
        }

        if (!stack.empty() && stack.peek() instanceof ASTNode.Block) {
            throw new UnmatchedBlockException("Leftover unclosed block.");
        }

        if (!stack.empty() && stack.peek() instanceof ASTNode.Conditional) {
            throw new MismatchedBranchException("Leftover unclosed conditional");
        }

        return root;
    }

    public static class MismatchedBranchException extends RuntimeException {
        MismatchedBranchException(String message) {
            super(message);
        }
    }

    public static class UnmatchedBlockException extends RuntimeException {
        UnmatchedBlockException(String message) {
            super(message);
        }
    }
}

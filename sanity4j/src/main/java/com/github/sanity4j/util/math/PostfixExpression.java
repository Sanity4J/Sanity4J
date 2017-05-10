package com.github.sanity4j.util.math;

import java.util.Map;

/**
 * PostFixExpression - evaluates postfix expressions like [1, 2, +]. Variables can be used, prefix them with a '#'.
 * 
 * @version 1.0 02/01/2008
 * @author Yiannis Paschalidis
 */
public class PostfixExpression
{
    /** The string array containing operators &amp; operands as strings, in postfix evaluation order. */
    private final String[] terms;

    /**
     * Evaluates a postfix expression.
     * 
     * @param terms - a String array containing operators &amp; operands as strings, in postfix evaluation order.
     * @return the result of the expression.
     * 
     * @throws SyntaxException Thrown if a problem occurs.
     */
    public static double evaluatePostfix(final String[] terms) throws SyntaxException
    {
        // an instance of the Stack class is an overkill, we can just get away using an array
        double[] stack = new double[terms.length]; // worst-case
        int stackIndex = 0;

        double result = 0.0;
        double operand1, operand2;

        try
        {
            for (int i = 0; i < terms.length; i++)
            {
                String temp = terms[i];

                if (temp.length() == 1)
                {
                    switch (temp.charAt(0))
                    {
                        case '+': // Addition
                            operand1 = stack[--stackIndex];
                            operand2 = stack[--stackIndex];
                            stack[stackIndex++] = operand1 + operand2;
                            break;

                        case '-': // Subtraction\
                            operand1 = stack[--stackIndex];
                            operand2 = stack[--stackIndex];
                            stack[stackIndex++] = operand2 - operand1;
                            break;

                        case '*': // Multiplication
                            operand1 = stack[--stackIndex];
                            operand2 = stack[--stackIndex];
                            stack[stackIndex++] = operand1 * operand2;
                            break;

                        case '/': // Division
                            operand1 = stack[--stackIndex];
                            operand2 = stack[--stackIndex];
                            stack[stackIndex++] = operand2 / operand1;
                            break;

                        case '^': // Power operator
                            operand1 = stack[--stackIndex];
                            operand2 = stack[--stackIndex];
                            stack[stackIndex++] = Math.pow(operand2, operand1);
                            break;

                        case '~': // Unary minus
                            stack[stackIndex - 1] = -stack[stackIndex - 1];
                            break;

                        default: // is 1 digit number
                            stack[stackIndex++] = new Double(temp).doubleValue();
                    }
                }
                else
                {
                    // is multiple-digit number
                    stack[stackIndex++] = new Double(temp).doubleValue();
                }
            }

            // The result should now be the only element on the stack.
            result = stack[--stackIndex];
        }
        catch (ArrayIndexOutOfBoundsException ex)
        {
            throw new SyntaxException("Stack underflow", ex);
        }
        catch (NumberFormatException ex)
        {
            throw new SyntaxException("Number format exception / Illegal operator", ex);
        }
        catch (Exception ex)
        {
            throw new SyntaxException("Unknown error during evaluation: " + ex.getMessage(), ex);
        }

        if (stackIndex != 0)
        {
            throw new SyntaxException("Expression incomplete");
        }

        return result;
    }

    /**
     * Evaluates a postfix expression.
     * 
     * @param terms - a String array containing operators &amp; operands as strings, in postfix evaluation order. It can also
     *            contain variables (string keys prefixed by a hash sign "#")
     * @param values - values for the variables
     * @return the result of the expression
     * 
     * @throws SyntaxException Thrown if a problem occurs.
     */
    public static double evaluatePostfixWithVariables(final String[] terms, final Map<String, Object>values) throws SyntaxException
    {
        String[] newOp = new String[terms.length];

        // Create a new expression by replacing variables with their values

        for (int i = 0; i < terms.length; i++)
        {
            if (terms[i].charAt(0) == '#')
            {
                String key = terms[i].substring(1);
                Object value = values.get(key);
                
                if (value == null)
                {
                    throw new SyntaxException("Bad variable id: " + key);
                }
                
                newOp[i] = value.toString();
            }
            else
            {
                newOp[i] = terms[i];
            }
        }

        // Now evaluate the new expression
        return evaluatePostfix(newOp);
    }

    /**
     * Creates a postfix expression.
     * 
     * @param terms - a String array containing operators &amp; operands as strings, in postfix evaluation order
     */
    public PostfixExpression(final String[] terms)
    {
        this.terms = new String[terms.length];
        System.arraycopy(terms, 0, this.terms, 0, terms.length);
    }

    /**
     * Evaluates this postfix expression.
     * 
     * @return the result of the expression
     * 
     * @throws SyntaxException Thrown if a problem occurs.
     */
    public double evaluate() throws SyntaxException
    {
        return evaluatePostfix(terms);
    }

    /**
     * Evaluates this postfix expression.
     * 
     * @param values - values for the variables
     * @return the result of the expression
     * 
     * @throws SyntaxException Thrown if a problem occurs.
     */
    public double evaluate(final Map<String, Object> values) throws SyntaxException
    {
        return evaluatePostfixWithVariables(terms, values);
    }
}

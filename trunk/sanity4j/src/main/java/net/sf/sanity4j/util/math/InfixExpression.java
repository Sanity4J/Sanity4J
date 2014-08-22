package net.sf.sanity4j.util.math;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Infix expression evaluator - evaluates infix expressions like "(12.34-(2+3.3)*4/3)^2"
 * Variables can be used, prefix them with a '#'. The infix expression is parsed and 
 * processed in postfix form.
 *
 * @version 1.0 02/01/2008
 * @author Yiannis Paschalidis
 */
public final class InfixExpression
{
    /** The transformed postfix expression which is used in evaluation. */
    private final PostfixExpression postFix;

    /**
     * Creates an InfixExpression with the given expression.
     * 
     * @param expression the expression to parse.
     * @throws SyntaxException if the expression is syntactically invalid.
     */
    public InfixExpression(final String expression) throws SyntaxException
    {
        postFix = new PostfixExpression(parseInfixToPostfix(expression));
    }

    /**
     * Evaluates this infix expression.
     * 
     * @return the result of the expression
     * 
     * @throws SyntaxException Thrown if a problem occurs.
     */
    public double evaluate() throws SyntaxException
    {
        return postFix.evaluate();
    }

    /**
     * Evaluates this infix expression.
     * 
     * @param values - values for the variables
     * @return the result of the expression
     * 
     * @throws SyntaxException Thrown if a problem occurs.
     */
    public double evaluate(final Map<String, Object> values) throws SyntaxException
    {
        return postFix.evaluate(values);
    }
    
    /** 
     * Convenience method to see if an operator belongs to a certain set of operators.
     * 
     * @param operator the operator to search for
     * @param operators the operators to check
     * @return true if <code>operators</code> contains <code>operator</code>, false if not.
     */
    private static boolean charIn(final char operator, final String operators)
    {
        return (operators.indexOf(operator) != -1);
    }

    /**
     * Evaluate an infix expression by first parsing it to postfix form, then evaluating the postfix.
     * 
     * @param expression - the string to evaluate
     * @return the evaluated expression
     * 
     * @throws SyntaxException Thrown if a problem occurs.
     */
    public static double evaluateInfix(final String expression) throws SyntaxException
    {
        return PostfixExpression.evaluatePostfix(parseInfixToPostfix(expression));
    }

    /**
     * Parse an infix expression.
     * 
     * @param inval - the infix expression to be parsed
     * @return a String array containing numbers & operands as strings, in postfix evaluation order
     * 
     * @throws SyntaxException Thrown if a problem occurs.
     */
    public static String[] parseInfixToPostfix(final String inval) throws SyntaxException
    {
        StringBuffer opString = new StringBuffer();
        char ltr = ' ';
        List<String> postfixOp = new ArrayList<String>();
        boolean unary = true; // determines whether operand is unary - used for -ve

        try
        {
            for (int i = 0; i < inval.length(); i++)
            {
                ltr = inval.charAt(i);

                switch (ltr)
                {
                    case ' ':
                    case '\t':
                    case '\r':
                    case '\n':
                        continue;
                        
                    case '(':
                        opString.append('(');
                        break;
                        
                    case ')':
                        while ((opString.length() > 0) && (opString.charAt(opString.length() - 1) != '('))
                        {
                            postfixOp.add(opString.substring(opString.length() - 1));
                            opString.setLength(opString.length() - 1);
                        }
                        opString.setLength(opString.length() - 1);
                        break;

                    case '^':
                        while (opString.length() > 0 && charIn(opString.charAt(opString.length() - 1), "^~"))
                        // while (opString.length()>0 && opString.charAt(opString.length()-1)=='^')
                        {
                            postfixOp.add(opString.substring(opString.length() - 1));
                            opString.setLength(opString.length() - 1);
                        }
                        opString.append('^');
                        unary = true;
                        break;

                    case '*':
                    case '/':
                        while (opString.length() > 0 && charIn(opString.charAt(opString.length() - 1), "^*/~"))
                        {
                            postfixOp.add(opString.substring(opString.length() - 1));
                            opString.setLength(opString.length() - 1);
                        }
                        opString.append(ltr);
                        unary = true;
                        break;
                        
                    case '-':
                        if (unary)
                        {
                            opString.append('~'); // use tilda to show unary minus
                            break;
                        }

                    case '+':

                        while (opString.length() > 0 && charIn(opString.charAt(opString.length() - 1), "^*/+-~"))
                        {
                            postfixOp.add(opString.substring(opString.length() - 1));
                            opString.setLength(opString.length() - 1);
                        }

                        opString.append(ltr);
                        unary = true;
                        break;

                    default: // is number/text
                        
                        StringBuffer numString = new StringBuffer();
                        numString.append(ltr);

                        while (i + 1 < inval.length())
                        {
                            ltr = inval.charAt(i + 1);

                            if (charIn(ltr, "+-*/()^"))
                            {
                                break;
                            }
                            else
                            {
                                if (ltr != ' ' && ltr != ',')
                                {
                                    numString.append(ltr);
                                }

                                i++;
                            }
                        }

                        postfixOp.add(numString.toString());
                        unary = false;

                        break;
                }
            }

            while (opString.length() > 0)
            {
                postfixOp.add(opString.substring(opString.length() - 1));
                opString.setLength(opString.length() - 1);
            }

            String[] result = postfixOp.toArray(new String[postfixOp.size()]);

            return result;
        }
        catch (Exception e)
        {
            throw new SyntaxException("Error parsing expression: " + e.getMessage(), e);
        }
    }
}

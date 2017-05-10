package com.github.sanity4j.util.math;

/**
 * Thrown by {@link PostfixExpression} and {@link InfixExpression} when a 
 * malformed expression produces an error when executed.
 * 
 * @version 1.0 02/01/2008
 * @author Yiannis Paschalidis
 */ 
public final class SyntaxException extends Exception
{
    /**
     * Creates a SyntaxException with the specified message.
     * @param message the error message.
     */
	public SyntaxException(final String message)
	{ 
	    super(message); 
	}
	
    /**
     * Creates a SyntaxException with the specified message.
     * 
     * @param message the error message.
     * @param cause the cause of the exception.
     */
    public SyntaxException(final String message, final Throwable cause)
    { 
        super(message, cause); 
    }
}

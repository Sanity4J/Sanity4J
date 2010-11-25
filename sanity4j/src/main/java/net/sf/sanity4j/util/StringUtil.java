package net.sf.sanity4j.util; 

import java.util.List;

/**
 * StringUtil - String related utility methods.  
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public final class StringUtil
{
    /** No instance methods here. */
    private StringUtil()
    {
    }

    /**
     * Concatenates the elements of a list into a single String, 
     * separating entries with the supplied separator.
     * 
     * @param list the list to concatenate
     * @param separator the separator to use
     * @return the concatenated string
     */
    public static String concatList(final List<?> list, final String separator)
    {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < list.size(); i++)
        {
            if (i > 0)
            {
                buf.append(separator);
            }

            buf.append(list.get(i));
        }

        return buf.toString();
    }
    
    /**
     * Determines whether the given string is null or "empty".
     * 
     * @param aString the string to check.
     * @return true if the given String is null or contains only whitespace.
     */
    public static boolean empty(final String aString)
    {
        if (aString != null)
        {
            final int len = aString.length();
            
            for (int i = 0; i < len; i++)
            {
                // This mirrors String.trim(), which removes ASCII
                // control characters as well as whitespace.
                if (aString.charAt(i) > ' ')
                {
                    return false;
                }
            }
        }
        
        return true;
    }    
}

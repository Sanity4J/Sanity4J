package net.sf.sanity4j.util; 

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.sanity4j.util.StringUtil;

import junit.framework.TestCase;

/**
 * StringUtil_Test - unit tests for {@link StringUtil}.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class StringUtil_Test extends TestCase
{
    public void testConcatList()
    {
        assertEquals("Incorrect result for empty list", 
                     "", StringUtil.concatList(Collections.EMPTY_LIST, ","));
        
        List<String> list = new ArrayList<String>();
        list.add("testConcatList.1");
        
        assertEquals("Incorrect result for list with 1 element", 
                     list.get(0), StringUtil.concatList(list, ","));

        list.add("testConcatList.2");
        
        assertEquals("Incorrect result for list with 2 elements", 
                     list.get(0) + "," + list.get(1), 
                     StringUtil.concatList(list, ","));
        
        list.add("");
        
        assertEquals("Incorrect result for list with empty string", 
                     list.get(0) + "!@#" + list.get(1) + "!@#" + list.get(2), 
                     StringUtil.concatList(list, "!@#"));
    }
    
    /** String with non-whitespace should not be empty. */
    private static final String NON_WHITE_SPAGE_MSG = "String with non-whitespace should not be empty";
    
    /** String with whitespace should be empty. */
    private static final String WHITE_SPAGE_MSG = "String with whitespace should be empty";
    
    public void testEmpty()
    {
        assertTrue("Null string should be empty", StringUtil.empty(null));
        assertTrue("Empty string should be empty", StringUtil.empty(""));
        assertTrue(WHITE_SPAGE_MSG, StringUtil.empty(" "));
        assertTrue(WHITE_SPAGE_MSG, StringUtil.empty(" \n\r\t"));
        assertFalse(NON_WHITE_SPAGE_MSG, StringUtil.empty("x"));
        assertFalse(NON_WHITE_SPAGE_MSG, StringUtil.empty(" x"));
        assertFalse(NON_WHITE_SPAGE_MSG, StringUtil.empty("x "));
        assertFalse(NON_WHITE_SPAGE_MSG, StringUtil.empty("\tx\n"));
    }
}

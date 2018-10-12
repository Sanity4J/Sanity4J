package com.github.sanity4j.util; 

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.sanity4j.util.StringUtil;

/**
 * StringUtil_Test - unit tests for {@link StringUtil}.
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class StringUtil_Test
{
   /** String with non-whitespace should not be empty. */
   private static final String NON_WHITE_SPAGE_MSG = "String with non-whitespace should not be empty";
   
   /** String with whitespace should be empty. */
   private static final String WHITE_SPAGE_MSG = "String with whitespace should be empty";
   
    @Test
    public void testConcatList()
    {
        Assert.assertEquals("Incorrect result for empty list", 
                     "", StringUtil.concatList(Collections.EMPTY_LIST, ","));
        
        List<String> list = new ArrayList<String>();
        list.add("testConcatList.1");
        
        Assert.assertEquals("Incorrect result for list with 1 element", 
                     list.get(0), StringUtil.concatList(list, ","));

        list.add("testConcatList.2");
        
        Assert.assertEquals("Incorrect result for list with 2 elements", 
                     list.get(0) + "," + list.get(1), 
                     StringUtil.concatList(list, ","));
        
        list.add("");
        
        Assert.assertEquals("Incorrect result for list with empty string", 
                     list.get(0) + "!@#" + list.get(1) + "!@#" + list.get(2), 
                     StringUtil.concatList(list, "!@#"));
    }
    
    @Test
    public void testEmpty()
    {
        Assert.assertTrue("Null string should be empty", StringUtil.empty(null));
        Assert.assertTrue("Empty string should be empty", StringUtil.empty(""));
        Assert.assertTrue(WHITE_SPAGE_MSG, StringUtil.empty(" "));
        Assert.assertTrue(WHITE_SPAGE_MSG, StringUtil.empty(" \n\r\t"));
        Assert.assertFalse(NON_WHITE_SPAGE_MSG, StringUtil.empty("x"));
        Assert.assertFalse(NON_WHITE_SPAGE_MSG, StringUtil.empty(" x"));
        Assert.assertFalse(NON_WHITE_SPAGE_MSG, StringUtil.empty("x "));
        Assert.assertFalse(NON_WHITE_SPAGE_MSG, StringUtil.empty("\tx\n"));
    }
}

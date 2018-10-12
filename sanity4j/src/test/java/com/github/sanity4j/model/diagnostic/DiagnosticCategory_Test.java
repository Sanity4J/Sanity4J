package com.github.sanity4j.model.diagnostic; 

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

/**
 * DiagnosticCategory_Test - unit tests for {@link DiagnosticCategory}. 
 * 
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class DiagnosticCategory_Test
{
    @Test
    public void testConstructors()
    {
        DiagnosticCategory root = new DiagnosticCategory();
        Assert.assertNotNull("Name should not be null", root.getName());
        Assert.assertNull("Parent should be null", root.getParent());
    }
    
    @Test
    public void testAddDiagnostic()
    {
        DiagnosticCategory root = new DiagnosticCategory();
        Assert.assertEquals("getLevel incorrect for root", 0, root.getLevel());
        
        addDiagnostic(root, "category/subcategory");
        Assert.assertTrue("root should not have diagnostics", root.getDiagnostics().isEmpty());
        
        DiagnosticCategory child = root.getSubCategories().get(0);
        Assert.assertEquals("Incorrect child name", "category", child.getName());
        Assert.assertTrue("Child should not have diagnostics", child.getDiagnostics().isEmpty());
        
        DiagnosticCategory grandChild = child.getSubCategories().get(0);
        Assert.assertEquals("Incorrect grandchild name", "subcategory", grandChild.getName());        
        Assert.assertFalse("Grandchild should have a diagnostic", grandChild.getDiagnostics().isEmpty());
    }
    
    @Test
    public void testIsRoot()
    {
        DiagnosticCategory root = new DiagnosticCategory();
        Assert.assertTrue("isRoot incorrect for root", root.isRoot());
        
        addDiagnostic(root, "dummy");
        DiagnosticCategory child = root.getSubCategories().get(0);
        Assert.assertFalse("isRoot incorrect for child", child.isRoot());
    }

    @Test
    public void testGetLevel()
    {
        DiagnosticCategory root = new DiagnosticCategory();
        Assert.assertEquals("getLevel incorrect for root", 0, root.getLevel());
        
        addDiagnostic(root, "category/subcategory");
        DiagnosticCategory child = root.getSubCategories().get(0);
        Assert.assertEquals("getLevel incorrect for child", 1, child.getLevel());
        
        DiagnosticCategory grandChild = child.getSubCategories().get(0);
        Assert.assertEquals("getLevel incorrect for child", 2, grandChild.getLevel());        
    }
    
    @Test
    public void testGetParent()
    {
        DiagnosticCategory root = new DiagnosticCategory();
        Assert.assertNull("getParent incorrect for root", root.getParent());
        
        addDiagnostic(root, "dummy");
        DiagnosticCategory child = root.getSubCategories().get(0);
        Assert.assertEquals("getParent incorrect for child", root, child.getParent());
    }
    
    @Test
    public void testGetDiagnosticCount()
    {
        DiagnosticCategory root = new DiagnosticCategory();
        addDiagnostic(root, "category/subcategory", Diagnostic.SEVERITY_MODERATE);
        addDiagnostic(root, "category", Diagnostic.SEVERITY_HIGH);

        DiagnosticCategory child = root.getSubCategories().get(0);
        DiagnosticCategory grandChild = child.getSubCategories().get(0);
        
        Assert.assertEquals("getDiagnosticCount incorrect for root", 
                     2, root.getDiagnosticCount());
        Assert.assertEquals("getDiagnosticCount incorrect for child", 
                     2, child.getDiagnosticCount());
        Assert.assertEquals("getDiagnosticCount incorrect for grandchild", 
                     1, grandChild.getDiagnosticCount());
        
        Assert.assertEquals("getDiagnosticCount sev=all incorrect for root", 
                     2, root.getDiagnosticCount(Diagnostic.SEVERITY_ALL));
        Assert.assertEquals("getDiagnosticCount sev=all incorrect for child", 
                     2, child.getDiagnosticCount(Diagnostic.SEVERITY_ALL));
        Assert.assertEquals("getDiagnosticCount sev=all incorrect for grandchild", 
                     1, grandChild.getDiagnosticCount(Diagnostic.SEVERITY_ALL));
        
        Assert.assertEquals("getDiagnosticCount sev=moderate incorrect for root", 
                     1, root.getDiagnosticCount(Diagnostic.SEVERITY_MODERATE));
        Assert.assertEquals("getDiagnosticCount sev=moderate incorrect for child", 
                     1, child.getDiagnosticCount(Diagnostic.SEVERITY_MODERATE));
        Assert.assertEquals("getDiagnosticCount sev=moderate incorrect for grandchild", 
                     1, grandChild.getDiagnosticCount(Diagnostic.SEVERITY_MODERATE));        
        
        Assert.assertEquals("getDiagnosticCount sev=high incorrect for root", 
                     1, root.getDiagnosticCount(Diagnostic.SEVERITY_HIGH));
        Assert.assertEquals("getDiagnosticCount sev=high incorrect for child", 
                     1, child.getDiagnosticCount(Diagnostic.SEVERITY_HIGH));
        Assert.assertEquals("getDiagnosticCount sev=high incorrect for grandchild", 
                     0, grandChild.getDiagnosticCount(Diagnostic.SEVERITY_HIGH));        
    }
    
    @Test
    public void testSubCategoriesIterator()
    {
        DiagnosticCategory root = new DiagnosticCategory();
        Assert.assertEquals("getLevel incorrect for root", 0, root.getLevel());
        
        addDiagnostic(root, "category/subcategory1");
        addDiagnostic(root, "category/subcategory2");
        
        // Root should have one subcategory
        Iterator<DiagnosticCategory> iterator = root.subCategoriesIterator();
        Assert.assertTrue("Iterator should have next", iterator.hasNext());
        
        Object next = iterator.next();
        Assert.assertTrue("Iterator next should be a DiagnosticCategory", 
                   next instanceof DiagnosticCategory);
        
        DiagnosticCategory child = (DiagnosticCategory) next; 
        Assert.assertSame("Child's parent should be the root node", root, child.getParent());
        
        Assert.assertFalse("Iterator should not have next", iterator.hasNext());
        
        // Child should have two subcategories
        iterator = child.subCategoriesIterator();
        Assert.assertTrue("Iterator should have next", iterator.hasNext());        
        next = iterator.next();
        Assert.assertTrue("Iterator should have next", iterator.hasNext());        
        next = iterator.next();
        Assert.assertFalse("Iterator should not have next", iterator.hasNext());
    }
    
    private void addDiagnostic(final DiagnosticCategory parent, final String category)
    {
        addDiagnostic(parent, category, Diagnostic.SEVERITY_MODERATE);
    }
    
    private void addDiagnostic(final DiagnosticCategory parent, 
                               final String category, final int severity)
    {
        Diagnostic diag = new Diagnostic()
        {
            @Override
            public String[] getCategories()
            {
                return new String[]{category};
            }
        };
        
        diag.setSeverity(severity);
        
        parent.addDiagnostic(diag);
    }
}

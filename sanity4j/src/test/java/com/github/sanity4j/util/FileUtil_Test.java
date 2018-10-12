package com.github.sanity4j.util; 

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * FileUtil_Test - unit tests for {@link FileUtil}. 
 * 
 * @author Yiannis Paschalidis 
 * @since Sanity4J 1.0
 */
public class FileUtil_Test
{
    @Test
    public void testCreateDir() throws IOException
    {
        File tempDir = getTempDir();
        
        try
        {
            FileUtil.createDir(tempDir.getPath());
            Assert.assertTrue("Failed to create directory", tempDir.isDirectory());
        }
        finally
        {
            tempDir.delete();
            Assert.assertFalse("Failed to delete temp dir " + tempDir.getPath(), tempDir.exists());
        }
    }
    
    @Test
    public void testFindJars() throws IOException
    {
        List<String> jars = new ArrayList<String>();
        File tempDir = getTempDir();
        
        try
        {
            tempDir.mkdirs();
            Assert.assertTrue("Failed to create directory", tempDir.isDirectory());
            
            File jar1 = new File(tempDir, "file1.jar");
            jar1.createNewFile();
            
            File nonJar = new File(tempDir, "file.txt");
            nonJar.createNewFile();
            
            File subDir1 = new File(tempDir, "subdir1");
            subDir1.mkdirs();
            File jar2 = new File(subDir1, "file2.jar");
            jar2.createNewFile();
            
            File subDir2 = new File(subDir1, "subdir2");
            subDir2.mkdirs();
            File jar3 = new File(subDir2, "file3.jar");
            jar3.createNewFile();
            
            FileUtil.findJars(tempDir, jars);
            Assert.assertEquals("Incorrect number of jars found", 3, jars.size());
            Assert.assertTrue("Missing jar at top level", jars.contains(jar1.getPath()));
            Assert.assertTrue("Missing jar at subdirectory level", jars.contains(jar2.getPath()));
            Assert.assertTrue("Missing jar at nested subdirectory level", jars.contains(jar3.getPath()));
        }
        finally
        {
            // test deletion
            if (tempDir.exists())
            {
                FileUtil.delete(tempDir);
                Assert.assertFalse("FileUtil.delete failed to delete " + tempDir, tempDir.exists());
            }
        }
    }
    
    private File getTempDir() throws IOException
    {
        // Alternatively, could read system property java.io.tmpdir ...
        File file = File.createTempFile("FileUtil_Test", "tmp");
        file.delete();
        Assert.assertFalse("Failed to delete temp file " + file.getPath(), file.exists());
        
        return file;
    }
}

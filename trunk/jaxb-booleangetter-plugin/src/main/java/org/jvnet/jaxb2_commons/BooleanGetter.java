package org.jvnet.jaxb2_commons;

import java.util.Collection;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.sun.codemodel.JMethod;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.outline.ClassOutline;

/**
 * XJC plugin to generate getXX functions instead of isXX functions for boolean values.
 * The motivation is to make using XJC generated classes easier to use with Spring, since 
 * Spring expects accessors to be called getXX.
 * 
 * @author Adam Burnett
 * @version 1.0
 
 * @author Yiannis Paschalidis
 * @version 1.1
 */
public class BooleanGetter extends Plugin
{
    protected final String OPTION_NAME = "Xboolean-getter";
    
    @Override
    public String getOptionName() 
	{
        return OPTION_NAME;
    }

    @Override
    public String getUsage() 
	{
        return "-" + OPTION_NAME + "    :   Generate getXX instead of isXX functions for boolean values\n";
    }
    
    @Override
    public boolean run(Outline model, Options opts, ErrorHandler errors) throws SAXException 
	{
        for( ClassOutline co : model.getClasses() ) 
		{
            Collection<JMethod> methods = co.implClass.methods();
            
			// Look at all our generated methods and rename isXX to getXX
            for(JMethod m : methods)
			{
                if(m.name().startsWith("is") && !m.type().isPrimitive()) // YP: 18/03/10 - Added check for !primitive
				{
                    m.name("get" + m.name().substring(2));
                }
            }
        }
        return true;
    }
}
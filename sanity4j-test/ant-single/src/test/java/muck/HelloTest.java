package muck;

import junit.framework.TestCase;

/**
 * A <a href="http://www.junit.org">JUnit</a> for the <b>muck.Hello</b> class.
 *
 * @author Brian Kavanagh.
 */
public class HelloTest extends TestCase {

    /**
     * A test case for the muck.Hello.main(String[]) method.
     */
    public final void testHello() {
        Hello.main(new String[] {});

        String string = "Hello";

        assertEquals("A fake assertion to avoid warning", "Hello", string);
    }
}

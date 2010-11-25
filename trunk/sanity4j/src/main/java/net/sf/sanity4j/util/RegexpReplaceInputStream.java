package net.sf.sanity4j.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>An {@link InputStream} implementation that replaced a regexp within the original stream
 * with a replacement regexp string "on the fly".</p>
 * 
 * <p>This class will use a fixed size "lookahead" buffer, so it is possible that a regexp longer than the "lookahead",
 * will not be matched. If expecting particularly long regexp matches (&gt; 4096 characters) use the
 * {@link #RegexpReplaceInputStream(InputStream, String, String, int)} constructor, and specify a lookahead long enough
 * to ensure that all regexps matched will fit within the lookahead.</p>
 * 
 * @author Brian Kavanagh
 * @author Yiannis Paschalidis
 * @since Sanity4J 1.0
 */
public class RegexpReplaceInputStream extends InputStream
{
    /** The default size of the lookahead buffer. */
    private static final int DEFAULT_LOOKAHEAD = 4096;

    /** The lookahead buffer used for pattern matching. (2 * {@link #lookaheadLength}) */
    private final byte[] lookahead;

    /**
     * Half the size of the {@link #lookahead} buffer, (we copy in half a buffer at a time to avoid missing matches on
     * the boundaries.
     */
    private final int lookaheadLength;

    /** The backing input stream. */
    private final InputStream backing;
    
    /**
     * The number of bytes remaining in the {@link #lookahead}. (Normally this will = 2 * {@link #lookaheadLength} but
     * when we reach the end of the underlying {@link InputStream} we need to work-out how many bytes are "valid" within
     * the {@link #lookahead} buffer.
     */
    private int lookaheadRemaining;

    /** The current read offset within the {@link #lookahead}. */
    private int lookaheadOffset;

    /**
     * The offset within the {@link #lookahead} of the begining of the {@link #pattern} string. -1 if there is no match
     * in the current {@link #lookahead}.
     */
    private int matchOffset;

    /**
     * The offset within the {@link #lookahead} of the end of the {@link #pattern} string. -1 if there is no match in
     * the current {@link #lookahead}.
     */
    private int matchFinishOffset;

    /** The regexp to be matched. */
    private final Pattern pattern;

    /** The replacement regular expression string. */
    private final String replaceRegexp;

    /** The replacement string. */
    private String replace;
    
    /** The current offset within the {@link #replace} string. */
    private int replaceOffset;

    /**
     * A flag indicating whether or not to replace all occurances of the {@link #pattern} within the underlying
     * {@link InputStream}.
     */
    private final boolean replaceAll;

    /**
     * This constructor creates a <b>RegexpReplaceInputStream</b> which replaces a single <em>regexp</em>, backed by
     * a specified <em>InputStream</em> with a default buffer size.
     * 
     * @param backing
     *            The <b>InputStream</b> backing this <b>RegexpReplaceInputStream</b>.
     * @param regexp
     *            The regexp to be replaced.
     * @param replaceRegexp
     *            The replacement replaceRegexp string.
     *            
     * @throws IOException if there is an error reading from the backing stream.
     */
    public RegexpReplaceInputStream(final InputStream backing, final String regexp, final String replaceRegexp) throws IOException
    {
        this(backing, regexp, replaceRegexp, DEFAULT_LOOKAHEAD, false);
    }

    /**
     * This constructor creates a <b>RegexpReplaceInputStream</b> which replaces a single regexp, backed by a specified
     * InputStream with a explicitly specified "lookahead" buffer size.
     * 
     * @param backing
     *            The <b>InputStream</b> backing this <b>RegexpReplaceInputStream</b>.
     * @param regexp
     *            The regexp to be replaced.
     * @param replaceRegexp
     *            The replacement regexp string.
     * @param lookaheadLength
     *            The length of the "lookahead" buffer used for pattern matching.
     *            
     * @throws IOException if there is an error reading from the backing stream.
     */
    public RegexpReplaceInputStream(final InputStream backing, final String regexp, final String replaceRegexp, final int lookaheadLength)
        throws IOException
    {
        this(backing, regexp, replaceRegexp, lookaheadLength, false);
    }

    /**
     * This constructor creates a <b>RegexpReplaceInputStream</b> backed by a specified <em>InputStream</em> with a
     * explicitly specified "lookahead" buffer size.
     * 
     * @param backing
     *            The <b>InputStream</b> backing this <b>RegexpReplaceInputStream</b>.
     * @param regexp
     *            The regexp to be replaced.
     * @param replaceRegexp
     *            The replacement regexp string.
     * @param lookaheadLength
     *            The size of the lookahead buffer.
     * @param replaceAll
     *            A flag which indicates whether (true) or not (false) we should replace all occurances of the regexp in
     *            the stream.
     *            
     * @throws IOException if there is an error reading from the backing stream.
     */
    public RegexpReplaceInputStream(final InputStream backing, final String regexp, final String replaceRegexp, final int lookaheadLength,
                                    final boolean replaceAll) throws IOException
    {
        this.backing = backing;
        this.lookaheadLength = lookaheadLength;
        this.lookahead = new byte[lookaheadLength * 2];
        this.lookaheadOffset = 0;
        this.lookaheadRemaining = backing.read(this.lookahead, 0, this.lookahead.length);

        this.pattern = Pattern.compile(regexp);
        this.matchOffset = -1;
        this.matchFinishOffset = -1;

        this.replaceOffset = 0;
        this.replaceRegexp = replaceRegexp;
        this.replaceAll = replaceAll;

        updateMatchOffset();
    }

    /**
     * This method is used to update the matchOffset member variables 
     * after a new read from the underlying inputStream.
     */
    private void updateMatchOffset()
    {
        try
        {
            if ((lookaheadRemaining > 0) && (matchOffset == -1))
            {
                String buffer = new String(lookahead, lookaheadOffset, lookaheadRemaining - lookaheadOffset, "UTF-8");

                Matcher matcher = pattern.matcher(buffer);

                if (matcher.find() && matcher.start() < lookaheadLength)
                {
                    matchOffset = lookaheadOffset + matcher.start();
                    matchFinishOffset = lookaheadOffset + matcher.end();
                    
                    replace = buffer.substring(matcher.start(), matcher.end()).replaceFirst(pattern.pattern(), replaceRegexp);
                }
            }
        }
        catch (UnsupportedEncodingException uue)
        {
            // Should never happen, java has to support UTF-8.
        }
    }

    /**
     * This method is used to read in another "chunk" from the underlying <b>InputStream</b>. It shuffles the last half
     * of the {@link #lookahead} buffer into the first half and then read in a new 2nd "half" form the underlying
     * <b>InputStream</b>.
     * 
     * @throws IOException if there is an error reading from the backing stream.
     */
    private void getLookahead() throws IOException
    {
        // Copy the 2nd half to the 1st half.
        if (lookaheadRemaining > lookaheadLength)
        {
            System.arraycopy(lookahead, lookaheadLength, lookahead, 0, lookaheadLength);
            lookaheadRemaining -= lookaheadLength;
            lookaheadOffset -= lookaheadLength;

            if (matchOffset != -1)
            {
                if (matchOffset - lookaheadLength >= 0)
                {
                    matchOffset -= lookaheadLength;
                }

                if (matchFinishOffset - lookaheadLength >= 0)
                {
                    matchFinishOffset -= lookaheadLength;
                }
            }
        }

        // Read in a new 2nd half.
        if (lookaheadRemaining == lookaheadLength)
        {
            int read = backing.read(lookahead, lookaheadLength, lookaheadLength);

            if (read != -1)
            {
                lookaheadRemaining += read;
            }
        }
    }

    /**
     * This method is used to perform a read after a match has been found in the underlying InputStream. The processing
     * is slightly different, because we now read perform the replacement, and then just defer to the underlying stream.
     * 
     * @return The character read from the underlying stream or -1 if we have reached the end of the stream.
     * @throws IOException if there is an error reading from the backing stream.
     */
    private int readMatch() throws IOException
    {
        if (lookaheadOffset >= lookaheadRemaining)
        {
            return backing.read();
        }
        else if (lookaheadOffset < matchOffset)
        {
            return lookahead[lookaheadOffset++];
        }
        else if (lookaheadOffset < matchFinishOffset)
        {
            int c = replace.charAt(replaceOffset++);

            if (replaceOffset >= replace.length())
            {
                lookaheadOffset = matchFinishOffset;
                matchFinishOffset = -1;

                if (replaceAll)
                {
                    matchOffset = -1;
                    replaceOffset = 0;
                    updateMatchOffset();
                }
            }

            return c;
        }
        else
        {
            return lookahead[lookaheadOffset++];
        }
    }

    /**
     * This method reads a character from the underlying {@link InputStream}, 
     * replacing a match to the given regexp with the replacement text along the way.
     * 
     * @return The character read from the underlying stream, or -1 if we have reached the end of the stream.
     * @throws IOException if there is an error reading from the backing stream.
     */
    public int read() throws IOException
    {
        if (lookaheadOffset >= lookaheadLength)
        {
            getLookahead();
            updateMatchOffset();
        }

        if (matchOffset != -1)
        {
            return readMatch();
        }

        if (lookaheadOffset > lookaheadRemaining - 1)
        {
            return -1;
        }

        return lookahead[lookaheadOffset++];
    }
}

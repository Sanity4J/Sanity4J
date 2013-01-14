<?xml version="1.0"?>
<xsl:stylesheet
    version   ="1.0"
    xmlns:xsl ="http://www.w3.org/1999/XSL/Transform">

    <xsl:output
        method         ="html"
        doctype-system ="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
        doctype-public ="-//W3C//DTD XHTML 1.0 Strict//EN"
        indent         ="yes"/>

    <xsl:include href="merge-attributes.xsl"/>

    <xsl:template match="classDetails">
        <html
            xmlns      ="http://www.w3.org/1999/xhtml"
            xmlns:html ="http://www.w3.org/1999/xhtml">
            <head>
                <title>
                    <xsl:value-of select="@className"/>
                </title>
                <link
                    rel   ="stylesheet"
                    type  ="text/css"
                    title ="Style">
                    <xsl:attribute name="href">
                        <xsl:value-of select="concat(@pathToRoot, 'css/qareport.css')"/>
                    </xsl:attribute>
                </link>
            </head>
            <body>
                <a target="_top">
                    <xsl:attribute name="href">
                        <xsl:value-of select="concat(@pathToRoot, 'index.html')"/>
                    </xsl:attribute>
                    Top
                </a>
                &#160;
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="concat(@pathToRoot, 'overview-summary.xml')"/>
                    </xsl:attribute>
                    Overview
                </a>
                &#160;
                <a href="package-summary.xml">Package</a>
                &#160;
                <b>Class</b>
                &#160;
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="concat(@pathToRoot, 'rule-catalogue.xml')"/>
                    </xsl:attribute>
                    Rule catalogue
                </a>
                &#160;
                <hr />
                <xsl:apply-templates select="./summary"/>
               	<xsl:if test="./diags">
	                <xsl:if test="./diags[@first='true']">
                    	<hr />
                    	<xsl:apply-templates select="./diags"/>
                	</xsl:if>
                </xsl:if>
                <hr />
                <font style="font-family: monospace">
                    <xsl:apply-templates select="./source"/>
                </font>
                <hr />
               	<xsl:if test="./diags">
	                <xsl:if test="./diags[@first!='true']">
                    	<hr />
                    	<xsl:apply-templates select="./diags"/>
                	</xsl:if>
                </xsl:if>
                <font size="-1">Generated at <xsl:value-of select="@runDate"/></font>
                <xsl:call-template name="emit-javascript"/>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="summary">
        <table
            class       ="diagSummaryHeader"
            cellpadding ="3"
            cellspacing ="0">
            <tr>
                <td class="sev4"><b>High: <xsl:value-of select="@high"/></b></td>
                <td class="sev3"><b>Significant: <xsl:value-of select="@significant"/></b></td>
                <td class="sev2"><b>Moderate:<xsl:value-of select="@moderate"/></b></td>
                <td class="sev1"><b>Low:<xsl:value-of select="@low"/></b></td>
                <td class="sev0"><b>Info:<xsl:value-of select="@info"/></b></td>
            </tr>
        </table>
        <font size="-1">
            <xsl:if test="@lineCoverage">Code coverage:<xsl:value-of select="@lineCoverage"/>
                %&#160;&#160;&#160;
            </xsl:if>
            <xsl:if test="@branchCoverage">
                Branch coverage:
                <xsl:value-of select="@branchCoverage"/>
                %&#160;&#160;&#160;
            </xsl:if>
            Code quality: <xsl:value-of select="@quality"/>%
        </font>
    </xsl:template>

    <xsl:template match="source">
        <table
            id          ="sourcecode"
            class       ="sourcecode"
            border      ="0"
            cellpadding ="0"
            cellspacing ="0"
            style       ="width: 100%">
            <tbody>
                <xsl:apply-templates select="./line"/>
            </tbody>
        </table>
    </xsl:template>

    <xsl:template match="source/line">
        <tr>
            <xsl:attribute name="class">
                <xsl:choose>
                    <xsl:when test="(position() mod 2 = 1)">
                        <xsl:text>odd</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>even</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <td>
                <xsl:attribute name="class">
                    <xsl:choose>
                        <xsl:when test="(@covered = 'no')"><xsl:text>cov0</xsl:text></xsl:when>
                        <xsl:when test="(@covered = 'partial')"><xsl:text>covP</xsl:text></xsl:when>
                        <xsl:when test="(@covered = 'yes')"><xsl:text>cov1</xsl:text></xsl:when>
                        <xsl:otherwise><xsl:text>covNA</xsl:text></xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:value-of select="position()"/>
            </td>
            <td>
                <xsl:attribute name="class">
                    <xsl:choose>
                        <xsl:when test="@sev">sev<xsl:value-of select="@sev"/></xsl:when>
                        <xsl:otherwise><xsl:text>sev-1</xsl:text></xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:apply-templates select="./diag"/>
            </td>
            <td>
                <xsl:attribute name="class">
                    <xsl:choose>
                        <xsl:when test="@sev">sev<xsl:value-of select="@sev"/></xsl:when>
                        <xsl:otherwise><xsl:text>sev-1</xsl:text></xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:value-of select="text()"/>
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="line/diag">
        <xsl:variable name="id">
            <xsl:value-of select="@id"/>
        </xsl:variable>
        <a>
            <xsl:attribute name="name">
                <xsl:value-of select="concat('diag', @id)"/>
            </xsl:attribute>
        </a>
        <xsl:if test="position()!=1">,</xsl:if>
        <a>
            <xsl:attribute name="title">
                <xsl:variable name="sev">
                    <xsl:value-of select="/classDetails/diags/diag[@id=$id]/@sev"/>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="($sev = '0')">Info:</xsl:when>
                    <xsl:when test="($sev = '1')">Low:</xsl:when>
                    <xsl:when test="($sev = '2')">Moderate:</xsl:when>
                    <xsl:when test="($sev = '3')">Significant:</xsl:when>
                    <xsl:when test="($sev = '4')">High:</xsl:when>
                </xsl:choose>
                <xsl:value-of select="/classDetails/diags/diag[@id=$id]/text()"/>
            </xsl:attribute>
            <xsl:attribute name="href">
                <xsl:value-of select="concat('#diagList', @id)"/>
            </xsl:attribute>
            <xsl:value-of select="count(//diags/diag[@id=$id]/preceding-sibling::*)+1"/>
        </a>
    </xsl:template>

    <xsl:template match="diags">
        <table caption="Issue list">
            <thead>
                <th style="text-align: left; width: 3em">#</th>
                <th style="text-align: left; width: 4em">line</th>
                <th style="text-align: left; width: 6em">Severity</th>
                <th style="text-align: left">Description</th>
            </thead>
            <tbody>
                <xsl:apply-templates select="./diag"/>
            </tbody>
        </table>
    </xsl:template>

    <xsl:template match="diags/diag">
        <xsl:variable name="id">
            <xsl:value-of select="@id"/>
        </xsl:variable>
        <tr>
            <td>
                <a>
                    <xsl:attribute name="name">
                        <xsl:value-of select="concat('diagList', @id)"/>
                    </xsl:attribute>
                </a>
                <b>
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="concat('#diag', @id)"/>
                        </xsl:attribute>
                        <xsl:value-of select="position()"/>
                    </a>
                </b>
            </td>
            <td>
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="concat('#diag', @id)"/>
                    </xsl:attribute>
                    <xsl:value-of select="count(//line/diag[@id=$id]/../preceding-sibling::line)+1"/>
                </a>
            </td>
            <td>
                <xsl:choose>
                    <xsl:when test="(@sev = '0')">Info</xsl:when>
                    <xsl:when test="(@sev = '1')">Low</xsl:when>
                    <xsl:when test="(@sev = '2')">Moderate</xsl:when>
                    <xsl:when test="(@sev = '3')">Significant</xsl:when>
                    <xsl:when test="(@sev = '4')">High</xsl:when>
                </xsl:choose>
            </td>
            <td>
                <a>
                    <xsl:variable name="lowercase" select="'abcdefghijklmnopqrstuvwxyz'" />
                    <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />
                
                    <xsl:attribute name="href">javascript:popupwindow('<xsl:value-of select="concat(//classDetails/@pathToRoot, 'rules/', translate(@tool, $uppercase, $lowercase), '/', @rule)"/>.html');</xsl:attribute>
                    <xsl:value-of select="text()"/>
                </a>
                <br />
            </td>
        </tr>
    </xsl:template>

    <!-- Emits the javascript used by the transformed HTML. -->
    <xsl:template name="emit-javascript">
        <script
            type  ="text/javascript"
            defer ="defer">
            <xsl:text disable-output-escaping="yes">
                <![CDATA[
// Keywords from http://java.sun.com/docs/books/tutorial/java/nutsandbolts/_keywords.html
var keywords = 
    {
        "abstract":true,"continue":true,"for":true,"new":true,"switch":true,
        "assert":true,"default":true,"goto":true,"package":true,"synchronized":true,
        "boolean":true,"do":true,"if":true,"private":true,"this":true,
        "break":true,"double":true,"implements":true,"protected":true,"throw":true,
        "byte":true,"else":true,"import":true,"public":true,"throws":true,
        "case":true,"enum":true,"instanceof":true,"return":true,"transient":true,
        "catch":true,"extends":true,"int":true,"short":true,"try":true,
        "char":true,"final":true,"interface":true,"static":true,"void":true,
        "class":true,"finally":true,"long":true,"strictfp":true,"volatile":true,
        "const":true,"float":true,"native":true,"super":true,"while":true
    };

// Pops up a new browser window, ensuring that only one popup is active at a time
// url: The url to open the new window
var newwindow;

function popupwindow(url)
{
    newwindow=window.open(url,'name','height=800,width=600,resizable=yes,scrollbars=yes');

    if (window.focus) 
    {
        newwindow.focus()
    }
}

function doHighlighting()
{	
    var table = document.getElementById("sourcecode");
    var rows = table.rows;

    // These are the only things that can span multiple rows.	
    var inComment = false;
    var inJavadoc = false;

    for (var i=0 ; i < rows.length ; i++)
    {
        var inString = false;
        var codeCell = rows[i].cells[2];

        if (codeCell.firstChild == null)
        {
            // No text
            continue;
        }

        var text = codeCell.firstChild.nodeValue;
        codeCell.removeChild(codeCell.firstChild);

        var textPos = 0;
        var lastChar=' ';

        for (var j=0 ; j <text.length ; j++)
        {
            switch (text.charAt(j))
            {
                case '"':
                {
                    if (!inComment && !inJavadoc)
                    {
                        if (inString == false)
                        {
                            inString = true;
                        }
                        else if (inString && lastChar != '\'')
                        {
                            inString = false;
                        }
                    }
                    break;
                }

                case '*':
                {
                    if (lastChar=='/' && !inComment && !inJavadoc && !inString)
                    {
                        parseRow(text.substring(textPos, j-1), codeCell);
                        textPos = j-1;

                        // Start of a multi-line comment peek at next char for javadoc comment
                        if (j+1 < text.length && text.charAt(j+1) == '*')
                        {
                            inJavadoc = true;
                        }
                        else
                        {
                            inComment = true;
                        }
                    }
                    break;
                }

                case '/':
                {
                    if (lastChar=='*' && !inString && (inComment || inJavadoc))
                    {
                        if (inJavadoc)
                        {
                            inJavadoc = false;
                            appendJavadocComment(text.substring(textPos, j+1), codeCell);
                        }
                        else // inComment
                        {
                            inComment = false;
                            appendComment(text.substring(textPos, j+1), codeCell);
                        }

                        textPos = j+1;
                    }
                    else if (lastChar=='/' && !inComment && !inJavadoc && !inString)
                    {
                        // Rest of the line is a comment
                        parseRow(text.substring(textPos, j-1), codeCell);

                        if (inJavadoc)
                        {
                            appendJavadocComment(text.substring(j-1), codeCell);
                        }
                        else
                        {
                            appendComment(text.substring(j-1), codeCell);
                        }

                        j = text.length;
                        textPos = j;
                    }

                    break;
                }
            }

            lastChar = text.charAt(j);
        }

        // At EOL		
        if (inJavadoc)
        {
            appendJavadocComment(text.substring(textPos), codeCell);
        }
        else if (inComment)
        {
            appendComment(text.substring(textPos), codeCell);
        }
        else
        {
            parseRow(text.substring(textPos), codeCell);
        }
    }
}

function parseRow(text, node)
{
    var inString = false;
    var textPos = 0;
    var lastChar=' ';
    var lastToken='';

    for (var j=0 ; j < text.length ; j++)
    {
        if (inString)
        {
            if (text.charAt(j) == '"' && lastChar!='\\')
            {
                appendTextString(text.substring(textPos, j+1), node);
                inString = false;
                textPos = j+1;
            }
        }
        else
        {
            switch (text.charAt(j))
            {
                case '"':
                {					
                    appendText(text.substring(textPos, j), node);
                    inString = true;
                    textPos = j;
                    break;
                }				

                case '}':
                case '{':
                case ')':
                case '(':
                case ';':
                case ':':
                case ' ':
                case '\t':
                {
                    if (isJavaKeyword(lastToken))				
                    {
                        appendText(text.substring(textPos, j-lastToken.length), node);
                        appendKeyword(lastToken, node);
                        textPos = j;
                    }
                    lastToken = '';
                    break;
                }

                default:
                lastToken += text.charAt(j);
            }
        }		

        lastChar = text.charAt(j);
    }	

    lastToken = text.substring(textPos); 

    if (isJavaKeyword(trim(lastToken)))	
    {
        appendKeyword(lastToken, node);
    }
    else
    {
        appendText(lastToken, node);
    }
}

//
// Appends a text node to the parent node that contains the given keyword text
//
function appendKeyword(text, parentNode)
{
    var spanNode = document.createElement("span");
    spanNode.style.color="blue";
    spanNode.style.fontWeight="bold";
    spanNode.appendChild(document.createTextNode(text));
    parentNode.appendChild(spanNode);
}

//
// Appends a text node to the parent node that contains the given commented text
//
function appendComment(text, parentNode)
{
    var spanNode = document.createElement("span");
    spanNode.style.color="green";
    spanNode.appendChild(document.createTextNode(text));
    parentNode.appendChild(spanNode);
}

//
// Appends a text node to the parent node that contains the given javadoc commented text
//
function appendJavadocComment(text, parentNode)
{
    var spanNode = document.createElement("span");
    spanNode.style.color="gray";
    spanNode.appendChild(document.createTextNode(text));
    parentNode.appendChild(spanNode);
}

//
// Appends a text node to the parent node that contains the given text String
//
function appendTextString(text, parentNode)
{
    var spanNode = document.createElement("span");
    spanNode.style.color="red";
    spanNode.appendChild(document.createTextNode(text));
    parentNode.appendChild(spanNode);
}

//
// Appends a text node to the parent node that contains the given text
//
function appendText(text, parentNode)
{
    var spanNode = document.createElement("span");
    spanNode.appendChild(document.createTextNode(text));
    parentNode.appendChild(spanNode);
}

//
// Is the given token a java keyword?
//
// token: The token to check
// return: true if the supplied token is a java keyword, false otherwise
//
function isJavaKeyword(token)
{
    return (token in keywords) && keywords.hasOwnProperty(token);
}

//
// Trims whitespace from the given text
//
// text: The token to trim
//
function trim(text)
{
    return text.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
}

doHighlighting();
//]]>
            </xsl:text>
        </script>
    </xsl:template>
</xsl:stylesheet>

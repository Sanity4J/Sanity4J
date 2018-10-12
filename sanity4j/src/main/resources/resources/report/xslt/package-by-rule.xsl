<?xml version="1.0"?>

<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     version="1.0">

	<xsl:output method="html" 
	    doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
	    doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" indent="yes"/>
	
	<!-- include common templates -->
	<xsl:include href="merge-attributes.xsl"/>

	<xsl:template match="packageByRule">
	
		<html xmlns="http://www.w3.org/1999/xhtml" xmlns:html="http://www.w3.org/1999/xhtml">
		<head>
			<title><xsl:value-of select="@packageName"/></title>
			<link rel="stylesheet" type="text/css" title="Style">
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
		</a>&#160;
		<a>
			<xsl:attribute name="href">
				<xsl:value-of select="concat(@pathToRoot, 'overview-summary.xml')"/>
			</xsl:attribute>		
			Overview
		</a>&#160;
		<a href="package-summary.xml">Package</a>&#160;
		Class&#160;
		<a>
			<xsl:attribute name="href">
				<xsl:value-of select="concat(@pathToRoot, 'rule-catalogue.xml')"/>
			</xsl:attribute>				
			Rule catalogue
		</a>&#160;
		
		<hr/>
		
		<table border="0" width="100%" summary="" cellpadding="2" cellspacing="0">
			<tr>
				<td class="sev-1" style="text-align: center; width:15%">
					<a href="javascript:showSev(-1);">All</a> (<xsl:value-of select="count(./rule/class/diag)"/>)
				</td>
				<td class="sev0" style="text-align: center; width:15%">
					<a href="javascript:showSev(0);">Info</a> (<xsl:value-of select="count(./rule[@severity='0']/class/diag)"/>)
				</td>
				<td class="sev1" style="text-align: center; width:15%">
					<a href="javascript:showSev(1);">Low</a> (<xsl:value-of select="count(./rule[@severity='1']/class/diag)"/>)
				</td>
				<td class="sev2" style="text-align: center; width:15%">
					<a href="javascript:showSev(2);">Moderate</a> (<xsl:value-of select="count(./rule[@severity='2']/class/diag)"/>)
				</td>
				<td class="sev3" style="text-align: center; width:15%">
					<a href="javascript:showSev(3);">Significant</a> (<xsl:value-of select="count(./rule[@severity='3']/class/diag)"/>)
				</td>
				<td class="sev4" style="text-align: center; width:15%">
					<a href="javascript:showSev(4);">High</a> (<xsl:value-of select="count(./rule[@severity='4']/class/diag)"/>)
				</td>
				<td style="text-align: right; width:10%">
					<a href="javascript:toggleDetails();">Toggle details</a>
				</td>
			</tr>
		</table>
			
		<hr/>
		
		<xsl:variable name="packageName">
			<xsl:choose>
				<xsl:when test="(@packageName = '')">All packages</xsl:when>
				<xsl:otherwise><xsl:value-of select="/packageByRule/@packageName"/></xsl:otherwise>
			</xsl:choose>			
		</xsl:variable>

		<h2 id="diagHeading">All rule violations for <xsl:value-of select="$packageName"/></h2>
			
		<table style="width: 100%; margin-left: 2em" cellspacing="0" cellpadding="0">
			
			<xsl:apply-templates select="./rule"/>
			
		</table>

		<hr/>
		
		<font size="-1">Generated at <xsl:value-of select="@runDate"/></font>

		</body>

		<script language='javascript'>
			<xsl:text disable-output-escaping="yes"><![CDATA[

			function toggleDetails()
			{
				for (var i=1 ; ; i++)
				{
					var element = document.getElementById('rule' + i + '-details');

					if (element != null)
					{
						element.style.display = (element.style.display != 'none' ? 'none' : '' );
					}
					else
					{
						break;
					}
				}
			}
			
			function showSev(severity)
			{
				if (severity == -1)
				{
					showRules();
				}
				else
				{
					showRules(0, 'ruleSeverity' + severity);
				}
			}
			
			function showRules(index, match, remove)
			{
				var countRemaining = 0;
				
				for (var i=1 ; ; i++)
				{
					var element = document.getElementById('rule' + i);
					
					if (element != null)
					{
						if (typeof index != 'undefined')
						{
							var isTarget = element.className.split(' ')[index] == match;
							
							if (remove)
							{ 
								if (!isTarget)
								{
									element.parentNode.removeChild(element);
								}
								else
								{
									element.id = 'rule' + (++countRemaining);
									document.getElementById('rule' + i + '-details').id = element.id + '-details';
								}
							}
							else
							{
								element.style.display = (isTarget ? '' : 'none');
							}
						}
						else
						{
							element.style.display = '';	
						}
					}
					else
					{
						break;
					}
				}
			}

			function popupwindow(url)
			{
				var newwindow=window.open(url,'name','height=800,width=600,resizable=yes,scrollbars=yes');
				
				if (window.focus) 
				{
					newwindow.focus()
				}
			}

			/* parse the query */
			function ptq(q)
			{
				var x = q.replace(/;/g, '&').split('&'), i, name, t;
				
				/* q changes from string version of query to object */				
				for (q={}, i=0; i<x.length; i++)
				{
					t = x[i].split('=', 2);
					name = unescape(t[0]);
					
					if (!q[name])
						q[name] = [];
						
					if (t.length > 1)
					{
						q[name][q[name].length] = unescape(t[1]);
					}
					/* next two lines are nonstandard */
					else
						q[name][q[name].length] = true;
				}
				
				return q;
			}

			var q = ptq(location.search.substring(1).replace(/\+/g, ' '));
			var tool = q['tool'];
			var sev = q['sev'];
			
			if (tool)
			{
				showRules(1, 'ruleTool' + tool, true);
				
				var heading = document.getElementById('diagHeading');
				
				heading.innerHTML = tool + heading.innerHTML.substring('All rule'.length);
			}
	
			if (sev)
			{
				showSev(sev);
			}
	
			//]]></xsl:text>
		</script>

		</html>
		
	</xsl:template>
	
	<xsl:template match="rule">
		<tr>
			<td>
				<xsl:attribute name="id">
					<xsl:value-of select="concat('rule', position())"/>
				</xsl:attribute>

				<xsl:attribute name="class">
					<xsl:value-of select="concat('ruleSeverity', @severity, ' ruleTool', @tool)"/>
				</xsl:attribute>

				<xsl:choose>
				  <xsl:when test="(@tool = 'PMD' or @tool = 'SpotBugs')">
					<a>
						<xsl:attribute name="href">
							<xsl:text>javascript:popupwindow('</xsl:text><xsl:value-of select="concat(/packageByRule/@pathToRoot, 'rules/', translate(@tool, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '/', @name)"/><xsl:text>.html');</xsl:text>
						</xsl:attribute>
						<xsl:value-of select="@name"/>
					</a> 			
					(<xsl:value-of select="count(./class/diag)"/> in <xsl:value-of select="count(./class)"/> class<xsl:if test="count(./class) > 1">es</xsl:if>)
				  </xsl:when>
				  <xsl:otherwise>
					<xsl:value-of select="concat(@name, ' (', count(./class/diag), ' in ', count(./class))"/> class<xsl:if test="count(./class) > 1">es</xsl:if>)
				  </xsl:otherwise>
				</xsl:choose>

				<div class="details" style='margin-left: 1em; margin-bottom: 1em'>
					<xsl:attribute name="id">
						<xsl:value-of select="concat('rule', position(), '-details')"/>
					</xsl:attribute>
					<xsl:apply-templates select="./class"/>
				</div>			
			</td>
		</tr>
	</xsl:template>

	<xsl:template match="class">
		<xsl:value-of select="@name"/> 
		
		<xsl:if test="(@name != 'unknown')">
			(<xsl:apply-templates select="./diag"/>)
		</xsl:if>
		
		<br/>
	</xsl:template>

	<xsl:template match="diag">

		<xsl:variable name="packageName"><xsl:value-of select="/packageByRule/@packageName"/></xsl:variable>
		<xsl:variable name="outerClassName">
			<xsl:choose>
			  <xsl:when test="contains(../@name, '$')">
				<xsl:value-of select="substring-before(../@name, '$')"/>
			  </xsl:when>
			  <xsl:otherwise>
				<xsl:value-of select="../@name"/>
			  </xsl:otherwise>
		  </xsl:choose>
		</xsl:variable>

		<xsl:if test="position() != 1">, </xsl:if>
		<a>
			<xsl:attribute name="href">
				<xsl:choose>
					<xsl:when test="($packageName = '')">
						<xsl:call-template name="replace-string">
							<xsl:with-param name='string' select="$outerClassName"/>
							<xsl:with-param name='search' select="'.'"/>
							<xsl:with-param name='replace' select="'/'"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="replace-string">
							<xsl:with-param name='string' select="substring($outerClassName, string-length($packageName) + 2)"/>
							<xsl:with-param name='search' select="'.'"/>
							<xsl:with-param name='replace' select="'/'"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:value-of select="concat('.xml#diag', @id)"/>
			</xsl:attribute>
			<xsl:value-of select="position()" />
		</a>
	
	</xsl:template>
	
	<!-- Macros -->

	<xsl:template name="replace-string">
		<xsl:param name="string"/>
		<xsl:param name="search"/>
		<xsl:param name="replace"/>
		<xsl:choose>
		  <xsl:when test="contains($string,$search)">
			<xsl:value-of select="substring-before($string,$search)"/>
			<xsl:value-of select="$replace"/>
			<xsl:call-template name="replace-string">
			  <xsl:with-param name="string" select="substring-after($string,$search)"/>
			  <xsl:with-param name="search" select="$search"/>
			  <xsl:with-param name="replace" select="$replace"/>
			</xsl:call-template>
		  </xsl:when>
		  <xsl:otherwise>
			<xsl:value-of select="$string"/>
		  </xsl:otherwise>
		</xsl:choose>
	  </xsl:template>
	  
	<xsl:template name="strip-package">
		<xsl:param name="string"/>
		<xsl:choose>
		  <xsl:when test="contains($string,'.')">
			<xsl:call-template name="strip-package">
			  <xsl:with-param name="string" select="substring-after($string, '.')"/>
			</xsl:call-template>
		  </xsl:when>
		  <xsl:otherwise>
			<xsl:value-of select="$string"/>
		  </xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	  
</xsl:stylesheet>
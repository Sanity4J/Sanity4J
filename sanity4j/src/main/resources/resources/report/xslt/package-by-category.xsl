<?xml version="1.0"?>

<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     version="1.0">

	<xsl:output method="html" 
	    doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
	    doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" indent="yes"/>
	
	<!-- include common templates -->
	<xsl:include href="merge-attributes.xsl"/>

	<xsl:template match="packageByCategory">
	
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
					<a href="javascript:showSev(-1);">All</a> (<xsl:value-of select="count(.//diag)"/>)
				</td>
				<td class="sev0" style="text-align: center; width:15%">
					<a href="javascript:showSev(0);">Info</a> (<xsl:value-of select="count(.//rule[@severity='0']//diag)"/>)
				</td>
				<td class="sev1" style="text-align: center; width:15%">
					<a href="javascript:showSev(1);">Low</a> (<xsl:value-of select="count(.//rule[@severity='1']//diag)"/>)
				</td>
				<td class="sev2" style="text-align: center; width:15%">
					<a href="javascript:showSev(2);">Moderate</a> (<xsl:value-of select="count(.//rule[@severity='2']//diag)"/>)
				</td>
				<td class="sev3" style="text-align: center; width:15%">
					<a href="javascript:showSev(3);">Significant</a> (<xsl:value-of select="count(.//rule[@severity='3']//diag)"/>)
				</td>
				<td class="sev4" style="text-align: center; width:15%">
					<a href="javascript:showSev(4);">High</a> (<xsl:value-of select="count(.//rule[@severity='4']//diag)"/>)
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
				<xsl:otherwise><xsl:value-of select="/packageByCategory/@packageName"/></xsl:otherwise>
			</xsl:choose>			
		</xsl:variable>

		<h2 id="diagHeading">Categories for <xsl:value-of select="$packageName"/></h2>
		
		<!-- only the 1st level categories are used. -->
		<xsl:apply-templates select="./category/category"/>

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
					showItems('rule');
				}
				else
				{
					showItems('rule', 0, 'ruleSeverity' + severity, false);
				}
			}
			
			function showRules(match, remove)
			{
				showItems('rule', 1, 'rule' + match, remove);
			}

			function showCategory(category, remove)
			{
				showItems('category', 0, 'category' + category, remove);
			}

			function showItems(prefix, index, match, remove)
			{
				var countRemaining = 0;
				
				for (var i=1 ; ; i++)
				{
					var element = document.getElementById(prefix + i);
					
					if (element != null)
					{
						if (typeof index != 'undefined')
						{
							var isTarget = element.className.split(' ')[index] == match;
							
							if (remove && !isTarget)
							{
								if (!isTarget)
								{
									element.parentNode.removeChild(element);
								}
								else
								{
									element.id = 'rule' + (++countRemaining);
									getElementById('rule' + i + '-details').id = element.id + '-details';
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
			var category = q['category'];
			var sev = q['sev'];
			
			if (category)
			{
				showCategory((category + '').replace(' ', '_'), true);
				
				var heading = document.getElementById('diagHeading');
				
				heading.innerHTML = category + heading.innerHTML.substring('Categories'.length);
			}
	
			if (sev)
			{
				showSev(sev);
			}
	
			//]]></xsl:text>
		</script>

		</html>
		
	</xsl:template>
	
	<xsl:template match="category">

		<div>
			<xsl:attribute name="id">
				<xsl:value-of select="concat('category', position())"/>
			</xsl:attribute>

			<xsl:attribute name="class">
				<xsl:text>category</xsl:text>
				<xsl:call-template name="replace-string">
					<xsl:with-param name='string' select="@name"/>
					<xsl:with-param name='search' select="' '"/>
					<xsl:with-param name='replace' select="'_'"/>
				</xsl:call-template>
			</xsl:attribute>

			<h3>
				 <xsl:value-of select="@name"/> 
				 (<xsl:value-of select="count(.//diag)"/> in <xsl:value-of select="count(.//class)"/> class<xsl:if test="count(.//class) > 1">es</xsl:if>)
			</h3>
				
			<table style="width: 100%; margin-left: 2em" cellspacing="0" cellpadding="0">
				<xsl:apply-templates select="./*"/>
			</table>
		</div>
	
	</xsl:template>
	
	<xsl:template match="rule">
		<tr>
			<td>
				<xsl:attribute name="id">
					<xsl:value-of select="concat('rule', position())"/>
				</xsl:attribute>

				<xsl:attribute name="class">
					<xsl:value-of select="concat('ruleSeverity', @severity)"/>
				</xsl:attribute>

				<xsl:choose>
				  <xsl:when test="(@tool = 'PMD' or @tool = 'SpotBugs')">
					<a>
						<xsl:attribute name="href">
							<xsl:text>javascript:popupwindow('</xsl:text><xsl:value-of select="concat(/packageByCategory/@pathToRoot, 'rules/', translate(@tool, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '/', @name)"/><xsl:text>.html');</xsl:text>
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

		<xsl:variable name="packageName"><xsl:value-of select="/packageByCategory/@packageName"/></xsl:variable>
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
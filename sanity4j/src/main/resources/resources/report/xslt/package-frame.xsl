<?xml version="1.0"?>

<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     version="1.0">

	<xsl:output method="html" 
	    doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
	    doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" indent="yes"/>
	
	<!-- include common templates -->
	<xsl:include href="merge-attributes.xsl"/>

	<xsl:template match="packageClasses">
	
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

		<xsl:call-template name="output-classes"><xsl:with-param name='sev' select="'-1'"/></xsl:call-template>
		<xsl:call-template name="output-classes"><xsl:with-param name='sev' select="'0'"/></xsl:call-template>
		<xsl:call-template name="output-classes"><xsl:with-param name='sev' select="'1'"/></xsl:call-template>
		<xsl:call-template name="output-classes"><xsl:with-param name='sev' select="'2'"/></xsl:call-template>
		<xsl:call-template name="output-classes"><xsl:with-param name='sev' select="'3'"/></xsl:call-template>
		<xsl:call-template name="output-classes"><xsl:with-param name='sev' select="'4'"/></xsl:call-template>

		<hr/>
		
		<font size="-1">Generated at <xsl:value-of select="@runDate"/></font>

		</body>

		<script language='javascript'>
			<xsl:text disable-output-escaping="yes"><![CDATA[

			function showSev(severity)
			{
				for (var i=-1 ; i<=4 ; i++)
					document.getElementById('sev' + i).style.display = (i == severity ? '' : 'none');
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
			var sev = q['sev'];
			
			if (sev && sev >=-1 && sev<=4)
			{
				showSev(sev);
			}
			else
			{
				showSev(-1);
			}
	
			//]]></xsl:text>
		</script>

		</html>
		
	</xsl:template>
	
	<!-- Macros -->

	<xsl:template name="output-classes">

		<xsl:param name="sev"/>

		<div>

			<xsl:attribute name="style">
				<xsl:if test="($sev != -1)">
					<xsl:text>display: none</xsl:text>
				</xsl:if>
			</xsl:attribute>

			<xsl:attribute name="id">
				<xsl:text>sev</xsl:text>
				<xsl:value-of select="$sev"/>
			</xsl:attribute>

			<table border="0" width="100%" summary="" cellpadding="2" cellspacing="0">
				<tr>
					<td class="sev-1" style="text-align: center; width:16%">
						<xsl:choose>
							<xsl:when test="($sev != -1)"><a href="javascript:showSev(-1);">All</a></xsl:when>
							<xsl:otherwise><b>All</b></xsl:otherwise>
						</xsl:choose>
					</td>
					<td class="sev0" style="text-align: center; width:17%">
						<xsl:choose>
							<xsl:when test="($sev != 0)"><a href="javascript:showSev(0);">Inf</a></xsl:when>
							<xsl:otherwise><b>Inf</b></xsl:otherwise>
						</xsl:choose>
					</td>
					<td class="sev1" style="text-align: center; width:17%">
						<xsl:choose>
							<xsl:when test="($sev != 1)"><a href="javascript:showSev(1);">Low</a></xsl:when>
							<xsl:otherwise><b>Low</b></xsl:otherwise>
						</xsl:choose>
					</td>
					<td class="sev2" style="text-align: center; width:17%">
						<xsl:choose>
							<xsl:when test="($sev != 2)"><a href="javascript:showSev(2);">Mod</a></xsl:when>
							<xsl:otherwise><b>Mod</b></xsl:otherwise>
						</xsl:choose>
					</td>
					<td class="sev3" style="text-align: center; width:17%">
						<xsl:choose>
							<xsl:when test="($sev != 3)"><a href="javascript:showSev(3);">Sig</a></xsl:when>
							<xsl:otherwise><b>Sig</b></xsl:otherwise>
						</xsl:choose>
					</td>
					<td class="sev4" style="text-align: center; width:16%">
						<xsl:choose>
							<xsl:when test="($sev != 4)"><a href="javascript:showSev(4);">Hig</a></xsl:when>
							<xsl:otherwise><b>Hig</b></xsl:otherwise>
						</xsl:choose>
					</td>
				</tr>
			</table>
			
			<hr/>
			
			<xsl:choose>
				<xsl:when test="@packageName = ''">
					<font size="+1" class="FrameTitleFont"><b>All Classes</b></font>
				</xsl:when>
				<xsl:otherwise>
					<font size="+1" class="FrameTitleFont">
						<a>
							<xsl:attribute name="target">
								<xsl:text>classFrame</xsl:text>
							</xsl:attribute>
							<xsl:attribute name="href">
								<xsl:value-of select="@pathToRoot"/>
								<xsl:call-template name="replace-string">
									<xsl:with-param name='string' select="@packageName"/>
									<xsl:with-param name='search' select="'.'"/>
									<xsl:with-param name='replace' select="'/'"/>
								</xsl:call-template>
								<xsl:text>/package-summary.xml</xsl:text>
							</xsl:attribute>
							<xsl:value-of select="@packageName"/>
						</a>
					</font>
				</xsl:otherwise>
			</xsl:choose>
			
			<p/>
			
			<font class="FrameItemFont">
			
			<xsl:for-each select="/packageClasses/class">
				
				<xsl:variable name="packageName"><xsl:value-of select="/packageClasses/@packageName"/></xsl:variable>
				<xsl:variable name="href">
					<xsl:value-of select="/packageClasses/@pathToRoot"/>
					<xsl:call-template name="replace-string">
						<xsl:with-param name='string' select="@name"/>
						<xsl:with-param name='search' select="'.'"/>
						<xsl:with-param name='replace' select="'/'"/>
					</xsl:call-template>
					<xsl:text>.xml</xsl:text>
				</xsl:variable>

				<xsl:if test="($sev=-1 or ($sev=0 and @info>0) or ($sev=1 and @low>0) or ($sev=2 and @moderate>0) or ($sev=3 and @significant>0) or ($sev=4 and @high>0))">

					<a>
				        <xsl:attribute name="href">
				          <xsl:value-of select="$href" />
				        </xsl:attribute>
				        <xsl:attribute name="target">
				          <xsl:text>classFrame</xsl:text>
				        </xsl:attribute>
						<xsl:call-template name="strip-package">
							<xsl:with-param name='string' select="@name"/>
						</xsl:call-template>
					</a>				
					
					<xsl:choose>
						<xsl:when test="($sev = '-1' and (@info + @low + @moderate + @significant + @high > 0))">
							<xsl:value-of select="concat(' (', @info + @low + @moderate + @significant + @high, ')')" />
						</xsl:when>
						<xsl:when test="($sev = '0' and @info > 0)">
							<xsl:value-of select="concat(' (', @info, ')')" />
						</xsl:when>
						<xsl:when test="($sev = '1' and @low > 0)">
							<xsl:value-of select="concat(' (', @low, ')')" />
						</xsl:when>
						<xsl:when test="($sev = '2' and @moderate > 0)">
							<xsl:value-of select="concat(' (', @moderate, ')')" />
						</xsl:when>
						<xsl:when test="($sev = '3' and @significant > 0)">
							<xsl:value-of select="concat(' (', @significant, ')')" />
						</xsl:when>
						<xsl:when test="($sev = '4' and @high > 0)">
							<xsl:value-of select="concat(' (', @high, ')')" />
						</xsl:when>
					</xsl:choose>
					
					<br/>
					
				</xsl:if>						
			
			</xsl:for-each>
			
			</font>			
			
		</div>		
	</xsl:template>
	
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
			  <xsl:with-param name="string" select="substring-after($string,'.')"/>
			</xsl:call-template>
		  </xsl:when>
		  <xsl:otherwise>
			<xsl:value-of select="$string"/>
		  </xsl:otherwise>
		</xsl:choose>
	  </xsl:template>	
	  
</xsl:stylesheet>
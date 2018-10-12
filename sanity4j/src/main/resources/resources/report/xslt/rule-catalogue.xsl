<?xml version="1.0"?>

<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     version="1.0">

	<xsl:output method="html" 
	    doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
	    doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" indent="yes"/>
	
	<!-- include common templates -->
	<xsl:include href="merge-attributes.xsl"/>

	<xsl:template match="rules">
	
		<html xmlns="http://www.w3.org/1999/xhtml" xmlns:html="http://www.w3.org/1999/xhtml">
		<head>
			<title>QA rule catalogue</title>
			<link rel="stylesheet" type="text/css" title="Style" href="css/qareport.css"/>
		</head>
		<body>
			<a target="_top" href="index.html">Top</a>&#160;
			<a href="overview-summary.xml">Overview</a>&#160;
			Package&#160;
			Class&#160;
			<b>Rule catalogue</b>&#160;
			
			<hr/>

			<h2>Code Quality Assurance rule catalogue</h2>

			<table cellspacing='0'>
			  <thead>
				<tr style='background-color: #cccccc'>
				  <th style='text-align: left'>Severity</th>
				  <th style='text-align: left'>Rule name</th>
				  <th style='text-align: left'>Source</th>
				</tr>

			  </thead>
			  <tbody>

				<xsl:apply-templates select="./rule"/>
				
			  </tbody>
			</table>

			<hr/>
			
			<font size="-1">Generated at <xsl:value-of select="@runDate"/></font>

			<xsl:call-template name="emit-javascript"/>			
		
		</body>
		</html>
		
	</xsl:template>

	<xsl:template match="rule">
		<tr>
			<xsl:attribute name="class">
				<xsl:choose>
					<xsl:when test="(position() mod 2 = 1)"><xsl:text>odd</xsl:text></xsl:when>
					<xsl:otherwise><xsl:text>even</xsl:text></xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
			<td style='padding-right: 1em'>
				<xsl:choose>
					<xsl:when test="(@severity = '0')">Info</xsl:when>
					<xsl:when test="(@severity = '1')">Low</xsl:when>
					<xsl:when test="(@severity = '2')">Moderate</xsl:when>
					<xsl:when test="(@severity = '3')">Significant</xsl:when>
					<xsl:when test="(@severity = '4')">High</xsl:when>
				</xsl:choose>			
			</td>
			<td style='padding-right: 1em'>
				<xsl:choose>
				  <xsl:when test="(@tool = 'PMD' or @tool = 'SpotBugs')">
					<a>
						<xsl:attribute name="href">
							<xsl:text>javascript:popupwindow('</xsl:text><xsl:value-of select="concat('rules/', translate(@tool, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '/', @name)"/><xsl:text>.html');</xsl:text>
						</xsl:attribute>
						<xsl:value-of select="@name"/>
					</a> 			
				  </xsl:when>
				  <xsl:otherwise>
					<xsl:value-of select="@name"/>
				  </xsl:otherwise>
				</xsl:choose>
			</td>
			<td><xsl:value-of select="@tool"/></td>
		</tr>
	</xsl:template>
	
	<!--
	 Emits the javascript used by the transformed HTML. 
	 -->
	<xsl:template name="emit-javascript">
		<script type="text/javascript" defer="defer">
		<xsl:text disable-output-escaping="yes"><![CDATA[
		
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
		//]]></xsl:text>
		</script>
	</xsl:template>
	
</xsl:stylesheet>
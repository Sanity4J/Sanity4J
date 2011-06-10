<?xml version="1.0"?>

<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     version="1.0">

	<xsl:output method="html" 
	    doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
	    doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" indent="yes"/>
	
	<!-- include common templates -->
	<xsl:include href="merge-attributes.xsl"/>

	<xsl:template match="packageSummary">
	
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
		<xsl:choose>
			<xsl:when test="(@packageName = '')"><b><xsl:text>Overview</xsl:text></b></xsl:when>
			<xsl:otherwise>
				<a>
					<xsl:attribute name="href">
						<xsl:value-of select="concat(@pathToRoot, 'overview-summary.xml')"/>
					</xsl:attribute>
					Overview
				</a>
			</xsl:otherwise>		
		</xsl:choose>
		&#160;
		<b>Package</b>&#160;
		Class&#160;
		<a>
			<xsl:attribute name="href">
				<xsl:value-of select="concat(@pathToRoot, 'rule-catalogue.xml')"/>
			</xsl:attribute>				
			Rule catalogue
		</a>&#160;
		
		<hr/>
		
		<h2>Code Quality Assurance Summary</h2>
		
		<h3>Issue summary</h3>		
		<xsl:apply-templates select="./issueSummary"/>
		
		<h3>Quality summary</h3>
		<xsl:apply-templates select="./graphs"/>
		<xsl:apply-templates select="./qualitySummary"/>

		<hr/>
		
		<font size="-1">Generated at <xsl:value-of select="@runDate"/></font>

		<xsl:call-template name="emit-javascript"/>
		
		</body>
		</html>
		
	</xsl:template>
	
	<xsl:template match="issueSummary">
		<table class='diagSummaryHeader' cellpadding='3' cellspacing='0'>
		<thead>
		<tr>
			<td class='row-heading'>All issues</td>
			<td class='sev4 heading'><a href="package-by-rule.xml?sev=4" style="text-decoration: none; color: black">High</a></td>
			<td class='sev3 heading'><a href="package-by-rule.xml?sev=3" style="text-decoration: none; color: black">Significant</a></td>
			<td class='sev2 heading'><a href="package-by-rule.xml?sev=2" style="text-decoration: none; color: black">Moderate</a></td>
			<td class='sev1 heading'><a href="package-by-rule.xml?sev=1" style="text-decoration: none; color: black">Low</a></td>
			<td class='sev0 heading'><a href="package-by-rule.xml?sev=0" style="text-decoration: none; color: black">Info</a></td>
		</tr>
		</thead>
		<tbody>
			<tr>
				<td class="row-sub-heading"><a href="package-by-rule.xml">All issues</a></td>
				<td class="sev4">
					<xsl:choose>
						<xsl:when test="(@high > 0)"><a href="package-by-rule.xml?sev=4" style="text-decoration: none; color: black; font-weight: bold;"><xsl:value-of select="@high"/></a></xsl:when>
						<xsl:otherwise><xsl:value-of select="@high"/></xsl:otherwise>		
					</xsl:choose>
				</td>
				<td class="sev3">
					<xsl:choose>
						<xsl:when test="(@significant > 0)"><a href="package-by-rule.xml?sev=3" style="text-decoration: none; color: black; font-weight: bold;"><xsl:value-of select="@significant"/></a></xsl:when>
						<xsl:otherwise><xsl:value-of select="@significant"/></xsl:otherwise>		
					</xsl:choose>
				</td>
				<td class="sev2">
					<xsl:choose>
						<xsl:when test="(@moderate > 0)"><a href="package-by-rule.xml?sev=2" style="text-decoration: none; color: black; font-weight: bold;"><xsl:value-of select="@moderate"/></a></xsl:when>
						<xsl:otherwise><xsl:value-of select="@moderate"/></xsl:otherwise>		
					</xsl:choose>
				</td>
				<td class="sev1">
					<xsl:choose>
						<xsl:when test="(@low > 0)"><a href="package-by-rule.xml?sev=1" style="text-decoration: none; color: black; font-weight: bold;"><xsl:value-of select="@low"/></a></xsl:when>
						<xsl:otherwise><xsl:value-of select="@low"/></xsl:otherwise>		
					</xsl:choose>
				</td>
				<td class="sev0">
					<xsl:choose>
						<xsl:when test="(@info > 0)"><a href="package-by-rule.xml?sev=0" style="text-decoration: none; color: black; font-weight: bold;"><xsl:value-of select="@info"/></a></xsl:when>
						<xsl:otherwise><xsl:value-of select="@info"/></xsl:otherwise>		
					</xsl:choose>
				</td>
			</tr>

			<!-- Only first-level categories are shown -->
			<xsl:if test="./category/category">
				<tr><td colspan="6" class="row-heading">Issues by category</td></tr>
				<xsl:apply-templates select="./category/category"/>
			</xsl:if>
			
			<xsl:if test="./tool">
				<tr><td colspan="6" class="row-heading">Issues by tool</td></tr>
				<xsl:apply-templates select="./tool"/>
			</xsl:if>
			
		</tbody>
		</table>
	</xsl:template>

	<xsl:template match="issueSummary/category/category">
		<tr>
			<td class="row-sub-heading">
				<a>
					<xsl:attribute name="href">
						<xsl:call-template name="replace-string">
							<xsl:with-param name='string' select="concat('package-by-category.xml?category=', @name)"/>
							<xsl:with-param name='search' select="' '"/>
							<xsl:with-param name='replace' select="'+'"/>
						</xsl:call-template>
					</xsl:attribute>
					<xsl:value-of select="@name"/>
				</a>
			</td>
			<td class="sev4">
				<xsl:choose>
					<xsl:when test="(@high > 0)">
						<a>
							<xsl:attribute name="href">
								<xsl:call-template name="replace-string">
									<xsl:with-param name='string' select="concat('package-by-category.xml?category=', @name, '&amp;sev=4')"/>
									<xsl:with-param name='search' select="' '"/>
									<xsl:with-param name='replace' select="'+'"/>
								</xsl:call-template>
							</xsl:attribute>
							<xsl:attribute name="style"><xsl:text>text-decoration: none; color: black; font-weight: bold;</xsl:text></xsl:attribute>
							<xsl:value-of select="@high"/>
						</a>
					</xsl:when>
					<xsl:otherwise><xsl:value-of select="@high"/></xsl:otherwise>		
				</xsl:choose>
			</td>
			<td class="sev3">
				<xsl:choose>
					<xsl:when test="(@significant > 0)">
						<a>
							<xsl:attribute name="href">
								<xsl:call-template name="replace-string">
									<xsl:with-param name='string' select="concat('package-by-category.xml?category=', @name, '&amp;sev=3')"/>
									<xsl:with-param name='search' select="' '"/>
									<xsl:with-param name='replace' select="'+'"/>
								</xsl:call-template>
							</xsl:attribute>
							<xsl:attribute name="style"><xsl:text>text-decoration: none; color: black; font-weight: bold;</xsl:text></xsl:attribute>
							<xsl:value-of select="@significant"/>
						</a>
					</xsl:when>
					<xsl:otherwise><xsl:value-of select="@significant"/></xsl:otherwise>		
				</xsl:choose>
			</td>
			<td class="sev2">
				<xsl:choose>
					<xsl:when test="(@moderate > 0)">
						<a>
							<xsl:attribute name="href">
								<xsl:call-template name="replace-string">
									<xsl:with-param name='string' select="concat('package-by-category.xml?category=', @name, '&amp;sev=2')"/>
									<xsl:with-param name='search' select="' '"/>
									<xsl:with-param name='replace' select="'+'"/>
								</xsl:call-template>
							</xsl:attribute>
							<xsl:attribute name="style"><xsl:text>text-decoration: none; color: black; font-weight: bold;</xsl:text></xsl:attribute>
							<xsl:value-of select="@moderate"/>
						</a>
					</xsl:when>
					<xsl:otherwise><xsl:value-of select="@moderate"/></xsl:otherwise>		
				</xsl:choose>
			</td>
			<td class="sev1">
				<xsl:choose>
					<xsl:when test="(@low > 0)">
						<a>
							<xsl:attribute name="href">
								<xsl:call-template name="replace-string">
									<xsl:with-param name='string' select="concat('package-by-category.xml?category=', @name, '&amp;sev=1')"/>
									<xsl:with-param name='search' select="' '"/>
									<xsl:with-param name='replace' select="'+'"/>
								</xsl:call-template>
							</xsl:attribute>
							<xsl:attribute name="style"><xsl:text>text-decoration: none; color: black; font-weight: bold;</xsl:text></xsl:attribute>
							<xsl:value-of select="@low"/>
						</a>
					</xsl:when>
					<xsl:otherwise><xsl:value-of select="@low"/></xsl:otherwise>		
				</xsl:choose>
			</td>
			<td class="sev0">
				<xsl:choose>
					<xsl:when test="(@info > 0)">
						<a>
							<xsl:attribute name="href">
								<xsl:call-template name="replace-string">
									<xsl:with-param name='string' select="concat('package-by-category.xml?category=', @name, '&amp;sev=0')"/>
									<xsl:with-param name='search' select="' '"/>
									<xsl:with-param name='replace' select="'+'"/>
								</xsl:call-template>
							</xsl:attribute>
							<xsl:attribute name="style"><xsl:text>text-decoration: none; color: black; font-weight: bold;</xsl:text></xsl:attribute>
							<xsl:value-of select="@info"/>
						</a>
					</xsl:when>
					<xsl:otherwise><xsl:value-of select="@info"/></xsl:otherwise>		
				</xsl:choose>
			</td>
		</tr>
	</xsl:template>

	<xsl:template match="issueSummary/tool">
		<tr>
			<td class="row-sub-heading">
				<a>
					<xsl:attribute name="href">
						<xsl:call-template name="replace-string">
							<xsl:with-param name='string' select="concat('package-by-rule.xml?tool=', @name)"/>
							<xsl:with-param name='search' select="' '"/>
							<xsl:with-param name='replace' select="'+'"/>
						</xsl:call-template>
					</xsl:attribute>
					<xsl:value-of select="@name"/>
				</a>
			</td>
			<td class="sev4">
				<xsl:choose>
					<xsl:when test="(@high > 0)">
						<a>
							<xsl:attribute name="href">
								<xsl:call-template name="replace-string">
									<xsl:with-param name='string' select="concat('package-by-rule.xml?tool=', @name, '&amp;sev=4')"/>
									<xsl:with-param name='search' select="' '"/>
									<xsl:with-param name='replace' select="'+'"/>
								</xsl:call-template>
							</xsl:attribute>
							<xsl:attribute name="style"><xsl:text>text-decoration: none; color: black; font-weight: bold;</xsl:text></xsl:attribute>
							<xsl:value-of select="@high"/>
						</a>
					</xsl:when>
					<xsl:otherwise><xsl:value-of select="@high"/></xsl:otherwise>		
				</xsl:choose>
			</td>
			<td class="sev3">
				<xsl:choose>
					<xsl:when test="(@significant > 0)">
						<a>
							<xsl:attribute name="href">
								<xsl:call-template name="replace-string">
									<xsl:with-param name='string' select="concat('package-by-rule.xml?tool=', @name, '&amp;sev=3')"/>
									<xsl:with-param name='search' select="' '"/>
									<xsl:with-param name='replace' select="'+'"/>
								</xsl:call-template>
							</xsl:attribute>
							<xsl:attribute name="style"><xsl:text>text-decoration: none; color: black; font-weight: bold;</xsl:text></xsl:attribute>
							<xsl:value-of select="@significant"/>
						</a>
					</xsl:when>
					<xsl:otherwise><xsl:value-of select="@significant"/></xsl:otherwise>		
				</xsl:choose>
			</td>
			<td class="sev2">
				<xsl:choose>
					<xsl:when test="(@moderate > 0)">
						<a>
							<xsl:attribute name="href">
								<xsl:call-template name="replace-string">
									<xsl:with-param name='string' select="concat('package-by-rule.xml?tool=', @name, '&amp;sev=2')"/>
									<xsl:with-param name='search' select="' '"/>
									<xsl:with-param name='replace' select="'+'"/>
								</xsl:call-template>
							</xsl:attribute>
							<xsl:attribute name="style"><xsl:text>text-decoration: none; color: black; font-weight: bold;</xsl:text></xsl:attribute>
							<xsl:value-of select="@moderate"/>
						</a>
					</xsl:when>
					<xsl:otherwise><xsl:value-of select="@moderate"/></xsl:otherwise>		
				</xsl:choose>
			</td>
			<td class="sev1">
				<xsl:choose>
					<xsl:when test="(@low > 0)">
						<a>
							<xsl:attribute name="href">
								<xsl:call-template name="replace-string">
									<xsl:with-param name='string' select="concat('package-by-rule.xml?tool=', @name, '&amp;sev=1')"/>
									<xsl:with-param name='search' select="' '"/>
									<xsl:with-param name='replace' select="'+'"/>
								</xsl:call-template>
							</xsl:attribute>
							<xsl:attribute name="style"><xsl:text>text-decoration: none; color: black; font-weight: bold;</xsl:text></xsl:attribute>
							<xsl:value-of select="@low"/>
						</a>
					</xsl:when>
					<xsl:otherwise><xsl:value-of select="@low"/></xsl:otherwise>		
				</xsl:choose>
			</td>
			<td class="sev0">
				<xsl:choose>
					<xsl:when test="(@info > 0)">
						<a>
							<xsl:attribute name="href">
								<xsl:call-template name="replace-string">
									<xsl:with-param name='string' select="concat('package-by-rule.xml?tool=', @name, '&amp;sev=0')"/>
									<xsl:with-param name='search' select="' '"/>
									<xsl:with-param name='replace' select="'+'"/>
								</xsl:call-template>
							</xsl:attribute>
							<xsl:attribute name="style"><xsl:text>text-decoration: none; color: black; font-weight: bold;</xsl:text></xsl:attribute>
							<xsl:value-of select="@info"/>
						</a>
					</xsl:when>
					<xsl:otherwise><xsl:value-of select="@info"/></xsl:otherwise>		
				</xsl:choose>
			</td>
		</tr>
	</xsl:template>

	<xsl:template match="graphs">
		<div style="text-align:center; margin-bottom: 1em;">
			<xsl:apply-templates select="./graph"/>
		</div>
	</xsl:template>
	
	<xsl:template match="graphs/graph">
		<div style="text-align:center; margin-bottom: 1em;">
			<img>
				<xsl:attribute name="src"><xsl:value-of select="@path"/></xsl:attribute>
				<xsl:attribute name="alt"><xsl:value-of select="@alt"/></xsl:attribute>
			</img>
		</div>
	</xsl:template>
	
	<xsl:template match="qualitySummary">
	
		<table class="qualitySummary" id="packageResults" summary="">
			<thead>
			<tr>
				<td class="heading">Package name</td>
				<td class="heading"># Classes</td>
				<td class="heading">Unit test &#160;&#160;&#160; <br/> Line coverage</td>
				<td class="heading">Unit test &#160;&#160;&#160; <br/> Branch coverage</td>
				<td class="heading">Code quality &#160;&#160;&#160; <br/> (H / S / M / L)</td>
			</tr>
			</thead>
			<tbody>
				<xsl:apply-templates select="./package"/>
			</tbody>
		</table>
		
		<p/>
		
		<table class="qualitySummary" id="classResults" summary="">
			<thead>
				<tr>
					<td class="heading">Classes in this Package</td>
					<td class="heading">Unit test &#160;&#160;&#160; <br/> Line coverage</td>
					<td class="heading">Unit test &#160;&#160;&#160; <br/> Branch coverage</td>
					<td class="heading">Code quality &#160;&#160;&#160; <br/> (H / S / M / L)</td>
				</tr>
			</thead>
			<tbody>
				<xsl:choose>
				  <xsl:when test="./class">
				      <xsl:apply-templates select="./class"/>
				  </xsl:when>
				  <xsl:otherwise>
					  <tr>
						<td>None</td>
						<td>
							<xsl:call-template name="percent-bar">
							  <xsl:with-param name="numerator" select="'0'"/>
							  <xsl:with-param name="denominator" select="'0'"/>
							  <xsl:with-param name="percent" select="'0'"/>
							</xsl:call-template>
						</td>
						<td>
							<xsl:call-template name="percent-bar">
							  <xsl:with-param name="numerator" select="'0'"/>
							  <xsl:with-param name="denominator" select="'0'"/>
							  <xsl:with-param name="percent" select="'0'"/>
							</xsl:call-template>
						</td>
						<td>
							<xsl:call-template name="percent-bar">
							  <xsl:with-param name="numerator" select="'0'"/>
							  <xsl:with-param name="denominator" select="'0'"/>
							  <xsl:with-param name="percent" select="'0'"/>
							</xsl:call-template>
						</td>
					  </tr>
				  </xsl:otherwise>
				</xsl:choose>
			</tbody>
		</table>
	
	</xsl:template>
	
	<xsl:template match="qualitySummary/package">
	
		<xsl:variable name="packageName"><xsl:value-of select="/packageSummary/@packageName"/></xsl:variable>
		
		<tr>
			<td>
				<xsl:choose>
				  <xsl:when test="(@name = '')">
						<b>All packages</b>
				  </xsl:when>
				  <xsl:when test="(@name = $packageName)">
						<xsl:value-of select="@name"/>
				  </xsl:when>
				  <xsl:otherwise>
						<a>
							<xsl:attribute name="href">
								<xsl:choose>
								  <xsl:when test="($packageName = '')">
									<xsl:call-template name="replace-string">
										<xsl:with-param name='string' select="@name"/>
										<xsl:with-param name='search' select="'.'"/>
										<xsl:with-param name='replace' select="'/'"/>
									</xsl:call-template>							
								  </xsl:when>
								  <xsl:otherwise>
									<xsl:call-template name="replace-string">
										<xsl:with-param name='string' select="substring(@name, string-length($packageName) + 2)"/>
										<xsl:with-param name='search' select="'.'"/>
										<xsl:with-param name='replace' select="'/'"/>
									</xsl:call-template>							
								  </xsl:otherwise>
								</xsl:choose>
								<xsl:text>/package-summary.xml</xsl:text>
							</xsl:attribute>
							<xsl:value-of select="@name"/>
						</a>
				  </xsl:otherwise>
				</xsl:choose>
			</td>
			<td>
				<xsl:value-of select="@classes"/>
			</td>
			<td>
				<xsl:choose>
				  <xsl:when test="(./testCoverage)">
					<xsl:call-template name="percent-bar">
					  <xsl:with-param name="numerator" select="./testCoverage/@coveredLineCount"/>
					  <xsl:with-param name="denominator" select="./testCoverage/@lineCount"/>
					  <xsl:with-param name="percent" select="./testCoverage/@lineCoveragePct"/>
					</xsl:call-template>
				  </xsl:when>
				  <xsl:otherwise>
					<xsl:call-template name="percent-bar">
					  <xsl:with-param name="numerator" select="'0'"/>
					  <xsl:with-param name="denominator" select="'0'"/>
					  <xsl:with-param name="percent" select="'0'"/>
					</xsl:call-template>
				  </xsl:otherwise>
				</xsl:choose>
			</td>
			<td>
				<xsl:choose>
				  <xsl:when test="(./testCoverage)">
					<xsl:call-template name="percent-bar">
					  <xsl:with-param name="numerator" select="./testCoverage/@coveredBranchCount"/>
					  <xsl:with-param name="denominator" select="./testCoverage/@branchCount"/>
					  <xsl:with-param name="percent" select="./testCoverage/@branchCoveragePct"/>
					</xsl:call-template>
				  </xsl:when>
				  <xsl:otherwise>
					<xsl:call-template name="percent-bar">
					  <xsl:with-param name="numerator" select="'0'"/>
					  <xsl:with-param name="denominator" select="'0'"/>
					  <xsl:with-param name="percent" select="'0'"/>
					</xsl:call-template>
				  </xsl:otherwise>
				</xsl:choose>
			</td>
			<td>
				<table cellpadding="0px" cellspacing="0px" class="percentgraph">
					<tr class="percentgraph">
						<td align="right" class="percentgraph" width="40"><xsl:value-of select="@quality"/>%</td>
						<td class="percentgraph">
							<div class="percentgraph">
								<div class="greenbar">
									<xsl:attribute name="style">
										<xsl:value-of select="concat('width:', @quality, 'px')"/>
									</xsl:attribute>
									<span class="text"><xsl:value-of select="concat(@high, ' / ', @significant, ' / ', @moderate, ' / ', @low)"/></span>
								</div>
							</div>
						</td>
					</tr>
				</table>
			</td>
		</tr>
		
	</xsl:template>
	
	<xsl:template match="qualitySummary/class">

		<tr>
			<td>
				<a>
					<xsl:attribute name="href">
						<xsl:value-of select="concat(@name, '.xml')"/>
					</xsl:attribute>
					<xsl:value-of select="@name"/>
				</a>
			</td>
			<td>
				<xsl:choose>
				  <xsl:when test="(./testCoverage)">
					<xsl:call-template name="percent-bar">
					  <xsl:with-param name="numerator" select="./testCoverage/@coveredLineCount"/>
					  <xsl:with-param name="denominator" select="./testCoverage/@lineCount"/>
					  <xsl:with-param name="percent" select="./testCoverage/@lineCoveragePct"/>
					</xsl:call-template>
				  </xsl:when>
				  <xsl:otherwise>
					<xsl:call-template name="percent-bar">
					  <xsl:with-param name="numerator" select="'0'"/>
					  <xsl:with-param name="denominator" select="'0'"/>
					  <xsl:with-param name="percent" select="'0'"/>
					</xsl:call-template>
				  </xsl:otherwise>
				</xsl:choose>
			</td>
			<td>
				<xsl:choose>
				  <xsl:when test="(./testCoverage)">
					<xsl:call-template name="percent-bar">
					  <xsl:with-param name="numerator" select="./testCoverage/@coveredBranchCount"/>
					  <xsl:with-param name="denominator" select="./testCoverage/@branchCount"/>
					  <xsl:with-param name="percent" select="./testCoverage/@branchCoveragePct"/>
					</xsl:call-template>
				  </xsl:when>
				  <xsl:otherwise>
					<xsl:call-template name="percent-bar">
					  <xsl:with-param name="numerator" select="'0'"/>
					  <xsl:with-param name="denominator" select="'0'"/>
					  <xsl:with-param name="percent" select="'0'"/>
					</xsl:call-template>
				  </xsl:otherwise>
				</xsl:choose>
			</td>
			<td>
				<table cellpadding="0px" cellspacing="0px" class="percentgraph">
					<tr class="percentgraph">
						<td align="right" class="percentgraph" width="40"><xsl:value-of select="@quality"/>%</td>
						<td class="percentgraph">
							<div class="percentgraph">
								<div class="greenbar">
									<xsl:attribute name="style">
										<xsl:value-of select="concat('width:', @quality, 'px')"/>
									</xsl:attribute>
									<span class="text"><xsl:value-of select="concat(@high, ' / ', @significant, ' / ', @moderate, ' / ', @low)"/></span>
								</div>
							</div>
						</td>
					</tr>
				</table>
			</td>
		</tr>

	</xsl:template>

	<!-- Macros -->

	<xsl:template name="percent-bar">
		<xsl:param name="numerator"/>
		<xsl:param name="denominator"/>
		<xsl:param name="percent"/>
		
		<xsl:choose>
		  <xsl:when test="($denominator = 0)">
				<table cellpadding="0px" cellspacing="0px" class="percentgraph"><tr class="percentgraph"><td align="right" class="percentgraph" width="40">N/A</td>
				<td class="percentgraph"><div class="percentgraph"><div class="na" style="width:100px"><span class="text">N/A</span></div></div></td></tr></table>
		  </xsl:when>
		  <xsl:otherwise>
				<table cellpadding="0px" cellspacing="0px" class="percentgraph">
					<tr class="percentgraph">
						<td align="right" class="percentgraph" width="40"><xsl:value-of select="$percent"/>%</td>
						<td class="percentgraph">
							<div class="percentgraph">
								<div class="greenbar">
									<xsl:attribute name="style">
										<xsl:value-of select="concat('width:', $percent, 'px')"/>
									</xsl:attribute>
									<span class="text"><xsl:value-of select="$numerator"/> / <xsl:value-of select="$denominator"/></span>
								</div>
							</div>
						</td>
					</tr>
				</table>
		  </xsl:otherwise>
		</xsl:choose>
		
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
	
	<!--
	 Emits the javascript used by the transformed HTML. 
	 -->
	<xsl:template name="emit-javascript">
		<script type="text/javascript" defer="defer">
		<xsl:text disable-output-escaping="yes"><![CDATA[

		/*----------------------------------------------------------------------------\
		|                            Sortable Table 1.12                              |
		|-----------------------------------------------------------------------------|
		|                         Created by Erik Arvidsson                           |
		|                  (http://webfx.eae.net/contact.html#erik)                   |
		|                      For WebFX (http://webfx.eae.net/)                      |
		|-----------------------------------------------------------------------------|
		| A DOM 1 based script that allows an ordinary HTML table to be sortable.     |
		|-----------------------------------------------------------------------------|
		|                  Copyright (c) 1998 - 2004 Erik Arvidsson                   |
		|-----------------------------------------------------------------------------|
		| This software is provided "as is", without warranty of any kind, express or |
		| implied, including  but not limited  to the warranties of  merchantability, |
		| fitness for a particular purpose and noninfringement. In no event shall the |
		| authors or  copyright  holders be  liable for any claim,  damages or  other |
		| liability, whether  in an  action of  contract, tort  or otherwise, arising |
		| from,  out of  or in  connection with  the software or  the  use  or  other |
		| dealings in the software.                                                   |
		| - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - |
		| This  software is  available under the  three different licenses  mentioned |
		| below.  To use this software you must chose, and qualify, for one of those. |
		| - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - |
		| The WebFX Non-Commercial License          http://webfx.eae.net/license.html |
		| Permits  anyone the right to use the  software in a  non-commercial context |
		| free of charge.                                                             |
		| - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - |
		| The WebFX Commercial license           http://webfx.eae.net/commercial.html |
		| Permits the  license holder the right to use  the software in a  commercial |
		| context. Such license must be specifically obtained, however it's valid for |
		| any number of  implementations of the licensed software.                    |
		| - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - |
		| GPL - The GNU General Public License    http://www.gnu.org/licenses/gpl.txt |
		| Permits anyone the right to use and modify the software without limitations |
		| as long as proper  credits are given  and the original  and modified source |
		| code are included. Requires  that the final product, software derivate from |
		| the original  source or any  software  utilizing a GPL  component, such  as |
		| this, is also licensed under the GPL license.                               |
		|-----------------------------------------------------------------------------|
		| 2003-01-10 | First version                                                  |
		| 2003-01-19 | Minor changes to the date parsing                              |
		| 2003-01-28 | JScript 5.0 fixes (no support for 'in' operator)               |
		| 2003-02-01 | Sloppy typo like error fixed in getInnerText                   |
		| 2003-07-04 | Added workaround for IE cellIndex bug.                         |
		| 2003-11-09 | The bDescending argument to sort was not correctly working     |
		|            | Using onclick DOM0 event if no support for addEventListener    |
		|            | or attachEvent                                                 |
		| 2004-01-13 | Adding addSortType and removeSortType which makes it a lot     |
		|            | easier to add new, custom sort types.                          |
		| 2004-01-27 | Switch to use descending = false as the default sort order.    |
		|            | Change defaultDescending to suit your needs.                   |
		| 2004-03-14 | Improved sort type None look and feel a bit                    |
		| 2004-08-26 | Made the handling of tBody and tHead more flexible. Now you    |
		|            | can use another tHead or no tHead, and you can chose some      |
		|            | other tBody.                                                   |
		|-----------------------------------------------------------------------------|
		| Created 2003-01-10 | All changes are in the log above. | Updated 2004-08-26 |
		\----------------------------------------------------------------------------*/
		
		
		function SortableTable(oTable, oSortTypes) {
		
			this.sortTypes = oSortTypes || [];
		
			this.sortColumn = null;
			this.descending = null;
		
			var oThis = this;
			this._headerOnclick = function (e) {
				oThis.headerOnclick(e);
			};
		
			if (oTable) {
				this.setTable( oTable );
				this.document = oTable.ownerDocument || oTable.document;
			}
			else {
				this.document = document;
			}
		
		
			// only IE needs this
			var win = this.document.defaultView || this.document.parentWindow;
			this._onunload = function () {
				oThis.destroy();
			};
			if (win && typeof win.attachEvent != "undefined") {
				win.attachEvent("onunload", this._onunload);
			}
		}
		
		SortableTable.gecko = navigator.product == "Gecko";
		SortableTable.msie = /msie/i.test(navigator.userAgent);
		// Mozilla is faster when doing the DOM manipulations on
		// an orphaned element. MSIE is not
		SortableTable.removeBeforeSort = SortableTable.gecko;
		
		SortableTable.prototype.onsort = function () {};
		
		// default sort order. true -> descending, false -> ascending
		SortableTable.prototype.defaultDescending = false;
		
		// shared between all instances. This is intentional to allow external files
		// to modify the prototype
		SortableTable.prototype._sortTypeInfo = {};
		
		SortableTable.prototype.setTable = function (oTable) {
			if ( this.tHead )
				this.uninitHeader();
			this.element = oTable;
			this.setTHead( oTable.tHead );
			this.setTBody( oTable.tBodies[0] );
		};
		
		SortableTable.prototype.setTHead = function (oTHead) {
			if (this.tHead && this.tHead != oTHead )
				this.uninitHeader();
			this.tHead = oTHead;
			this.initHeader( this.sortTypes );
		};
		
		SortableTable.prototype.setTBody = function (oTBody) {
			this.tBody = oTBody;
		};
		
		SortableTable.prototype.setSortTypes = function ( oSortTypes ) {
			if ( this.tHead )
				this.uninitHeader();
			this.sortTypes = oSortTypes || [];
			if ( this.tHead )
				this.initHeader( this.sortTypes );
		};
		
		// adds arrow containers and events
		// also binds sort type to the header cells so that reordering columns does
		// not break the sort types
		SortableTable.prototype.initHeader = function (oSortTypes) {
			if (!this.tHead) return;
			var cells = this.tHead.rows[0].cells;
			var doc = this.tHead.ownerDocument || this.tHead.document;
			this.sortTypes = oSortTypes || [];
			var l = cells.length;
			var img, c;
			for (var i = 0; i < l; i++) {
				c = cells[i];
				if (this.sortTypes[i] != null && this.sortTypes[i] != "None") {
					img = doc.createElement("IMG");
					img.src = img.src = "]]></xsl:text><xsl:value-of select="concat(@pathToRoot, 'images/blank.png')"/><xsl:text disable-output-escaping="yes"><![CDATA[";
					c.appendChild(img);
					if (this.sortTypes[i] != null)
						c._sortType = this.sortTypes[i];
					if (typeof c.addEventListener != "undefined")
						c.addEventListener("click", this._headerOnclick, false);
					else if (typeof c.attachEvent != "undefined")
						c.attachEvent("onclick", this._headerOnclick);
					else
						c.onclick = this._headerOnclick;
				}
				else
				{
					c.setAttribute( "_sortType", oSortTypes[i] );
					c._sortType = "None";
				}
			}
			this.updateHeaderArrows();
		};
		
		// remove arrows and events
		SortableTable.prototype.uninitHeader = function () {
			if (!this.tHead) return;
			var cells = this.tHead.rows[0].cells;
			var l = cells.length;
			var c;
			for (var i = 0; i < l; i++) {
				c = cells[i];
				if (c._sortType != null && c._sortType != "None") {
					c.removeChild(c.lastChild);
					if (typeof c.removeEventListener != "undefined")
						c.removeEventListener("click", this._headerOnclick, false);
					else if (typeof c.detachEvent != "undefined")
						c.detachEvent("onclick", this._headerOnclick);
					c._sortType = null;
					c.removeAttribute( "_sortType" );
				}
			}
		};
		
		SortableTable.prototype.updateHeaderArrows = function () {
			if (!this.tHead) return;
			var cells = this.tHead.rows[0].cells;
			var l = cells.length;
			var img;
			for (var i = 0; i < l; i++) {
				if (cells[i]._sortType != null && cells[i]._sortType != "None") {
					img = cells[i].lastChild;
					if (i == this.sortColumn)
						img.className = "sort-arrow " + (this.descending ? "descending" : "ascending");
					else
						img.className = "sort-arrow";
				}
			}
		};
		
		SortableTable.prototype.headerOnclick = function (e) {
			// find TD element
			var el = e.target || e.srcElement;
			while (el.tagName != "TD")
				el = el.parentNode;
		
			this.sort(SortableTable.msie ? SortableTable.getCellIndex(el) : el.cellIndex);
		};
		
		// IE returns wrong cellIndex when columns are hidden
		SortableTable.getCellIndex = function (oTd) {
			var cells = oTd.parentNode.childNodes
			var l = cells.length;
			var i;
			for (i = 0; cells[i] != oTd && i < l; i++)
				;
			return i;
		};
		
		SortableTable.prototype.getSortType = function (nColumn) {
			return this.sortTypes[nColumn] || "String";
		};
		
		// only nColumn is required
		// if bDescending is left out the old value is taken into account
		// if sSortType is left out the sort type is found from the sortTypes array
		
		SortableTable.prototype.sort = function (nColumn, bDescending, sSortType) {
			if (!this.tBody) return;
			if (sSortType == null)
				sSortType = this.getSortType(nColumn);
		
			// exit if None
			if (sSortType == "None")
				return;
		
			if (bDescending == null) {
				if (this.sortColumn != nColumn)
					this.descending = this.defaultDescending;
				else
					this.descending = !this.descending;
			}
			else
				this.descending = bDescending;
		
			this.sortColumn = nColumn;
		
			if (typeof this.onbeforesort == "function")
				this.onbeforesort();
		
			var f = this.getSortFunction(sSortType, nColumn);
			var a = this.getCache(sSortType, nColumn);
			var tBody = this.tBody;
		
			a.sort(f);
		
			if (this.descending)
				a.reverse();
		
			if (SortableTable.removeBeforeSort) {
				// remove from doc
				var nextSibling = tBody.nextSibling;
				var p = tBody.parentNode;
				p.removeChild(tBody);
			}
		
			// insert in the new order
			var l = a.length;
			for (var i = 0; i < l; i++)
				tBody.appendChild(a[i].element);
		
			if (SortableTable.removeBeforeSort) {
				// insert into doc
				p.insertBefore(tBody, nextSibling);
			}
		
			this.updateHeaderArrows();
		
			this.destroyCache(a);
		
			if (typeof this.onsort == "function")
				this.onsort();
		};
		
		SortableTable.prototype.asyncSort = function (nColumn, bDescending, sSortType) {
			var oThis = this;
			this._asyncsort = function () {
				oThis.sort(nColumn, bDescending, sSortType);
			};
			window.setTimeout(this._asyncsort, 1);
		};
		
		SortableTable.prototype.getCache = function (sType, nColumn) {
			if (!this.tBody) return [];
			var rows = this.tBody.rows;
			var l = rows.length;
			var a = new Array(l);
			var r;
			for (var i = 0; i < l; i++) {
				r = rows[i];
				a[i] = {
					value:		this.getRowValue(r, sType, nColumn),
					element:	r
				};
			};
			return a;
		};
		
		SortableTable.prototype.destroyCache = function (oArray) {
			var l = oArray.length;
			for (var i = 0; i < l; i++) {
				oArray[i].value = null;
				oArray[i].element = null;
				oArray[i] = null;
			}
		};
		
		SortableTable.prototype.getRowValue = function (oRow, sType, nColumn) {
			// if we have defined a custom getRowValue use that
			if (this._sortTypeInfo[sType] && this._sortTypeInfo[sType].getRowValue)
				return this._sortTypeInfo[sType].getRowValue(oRow, nColumn);
		
			var s;
			var c = oRow.cells[nColumn];
			if (typeof c.innerText != "undefined")
				s = c.innerText;
			else
				s = SortableTable.getInnerText(c);
			return this.getValueFromString(s, sType);
		};
		
		SortableTable.getInnerText = function (oNode) {
			var s = "";
			var cs = oNode.childNodes;
			var l = cs.length;
			for (var i = 0; i < l; i++) {
				switch (cs[i].nodeType) {
					case 1: //ELEMENT_NODE
						s += SortableTable.getInnerText(cs[i]);
						break;
					case 3:	//TEXT_NODE
						s += cs[i].nodeValue;
						break;
				}
			}
			return s;
		};
		
		SortableTable.prototype.getValueFromString = function (sText, sType) {
			if (this._sortTypeInfo[sType])
				return this._sortTypeInfo[sType].getValueFromString( sText );
			return sText;
			/*
			switch (sType) {
				case "Number":
					return Number(sText);
				case "CaseInsensitiveString":
					return sText.toUpperCase();
				case "Date":
					var parts = sText.split("-");
					var d = new Date(0);
					d.setFullYear(parts[0]);
					d.setDate(parts[2]);
					d.setMonth(parts[1] - 1);
					return d.valueOf();
			}
			return sText;
			*/
			};
		
		SortableTable.prototype.getSortFunction = function (sType, nColumn) {
			if (this._sortTypeInfo[sType])
				return this._sortTypeInfo[sType].compare;
			return SortableTable.basicCompare;
		};
		
		SortableTable.prototype.destroy = function () {
			this.uninitHeader();
			var win = this.document.parentWindow;
			if (win && typeof win.detachEvent != "undefined") {	// only IE needs this
				win.detachEvent("onunload", this._onunload);
			}
			this._onunload = null;
			this.element = null;
			this.tHead = null;
			this.tBody = null;
			this.document = null;
			this._headerOnclick = null;
			this.sortTypes = null;
			this._asyncsort = null;
			this.onsort = null;
		};
		
		// Adds a sort type to all instance of SortableTable
		// sType : String - the identifier of the sort type
		// fGetValueFromString : function ( s : string ) : T - A function that takes a
		//    string and casts it to a desired format. If left out the string is just
		//    returned
		// fCompareFunction : function ( n1 : T, n2 : T ) : Number - A normal JS sort
		//    compare function. Takes two values and compares them. If left out less than,
		//    <, compare is used
		// fGetRowValue : function( oRow : HTMLTRElement, nColumn : int ) : T - A function
		//    that takes the row and the column index and returns the value used to compare.
		//    If left out then the innerText is first taken for the cell and then the
		//    fGetValueFromString is used to convert that string the desired value and type
		
		SortableTable.prototype.addSortType = function (sType, fGetValueFromString, fCompareFunction, fGetRowValue) {
			this._sortTypeInfo[sType] = {
				type:				sType,
				getValueFromString:	fGetValueFromString || SortableTable.idFunction,
				compare:			fCompareFunction || SortableTable.basicCompare,
				getRowValue:		fGetRowValue
			};
		};
		
		// this removes the sort type from all instances of SortableTable
		SortableTable.prototype.removeSortType = function (sType) {
			delete this._sortTypeInfo[sType];
		};
		
		SortableTable.basicCompare = function compare(n1, n2) {
			if (n1.value < n2.value)
				return -1;
			if (n2.value < n1.value)
				return 1;
			return 0;
		};
		
		SortableTable.idFunction = function (x) {
			return x;
		};
		
		SortableTable.toUpperCase = function (s) {
			return s.toUpperCase();
		};
		
		SortableTable.toDate = function (s) {
			var parts = s.split("-");
			var d = new Date(0);
			d.setFullYear(parts[0]);
			d.setDate(parts[2]);
			d.setMonth(parts[1] - 1);
			return d.valueOf();
		};
		
		/*
		 * Cobertura - http://cobertura.sourceforge.net/
		 *
		 * Copyright (C) 2005 Mark Doliner
		 * Copyright (C) 2005 Olivier Parent
		 *
		 * Cobertura is free software; you can redistribute it and/or modify
		 * it under the terms of the GNU General Public License as published
		 * by the Free Software Foundation; either version 2 of the License,
		 * or (at your option) any later version.
		 *
		 * Cobertura is distributed in the hope that it will be useful, but
		 * WITHOUT ANY WARRANTY; without even the implied warranty of
		 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
		 * General Public License for more details.
		 *
		 * You should have received a copy of the GNU General Public License
		 * along with Cobertura; if not, write to the Free Software
		 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
		 * USA
		 */
		
		
		
		function percentageSortType( s )
		{
			var ret;
			var i = s.indexOf( "%" );
		
			if (i != -1) {
				s = s.substr( 0, i );
			}
			ret = parseFloat(s);
			if (isNaN(ret)) {
				ret = -1;
			}
		
			return ret;
		}
		
		SortableTable.prototype.addSortType( "Percentage", percentageSortType );
		
		
		
		// This is needed for correctly sorting numbers in different
		// locales.  The stock number converter only expects to sort
		// numbers which use a period as a separator instead of a
		// comma (like French).
		function formattedNumberSortType( s )
		{
			var ret;
			var i = s.indexOf(';');
		
			if (i != -1) {
				s = s.substring(0, i);
			}
			ret = parseFloat(s);
			if (isNaN(ret)) {
				return -1;
			}
		
			return ret;
		}
		
		SortableTable.prototype.addSortType( "FormattedNumber", formattedNumberSortType );

		// add sort types
		SortableTable.prototype.addSortType("Number", Number);
		SortableTable.prototype.addSortType("CaseInsensitiveString", SortableTable.toUpperCase);
		SortableTable.prototype.addSortType("Date", SortableTable.toDate);
		SortableTable.prototype.addSortType("String");
		// None is a special case
		
		// Register the sortable tables
		var packageTable = new SortableTable(document.getElementById("packageResults"), ["String", "Number", "Percentage", "Percentage", "Percentage"]);
		packageTable.sort(0);
		
		var classTable = new SortableTable(document.getElementById("classResults"), ["String", "Percentage", "Percentage", "Percentage"]);
		classTable.sort(0);
		
		//]]></xsl:text>
		</script>
	</xsl:template>	
</xsl:stylesheet>
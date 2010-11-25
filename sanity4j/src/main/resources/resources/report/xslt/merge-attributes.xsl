<?xml version="1.0"?>
<xsl:stylesheet
  version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:exslt="http://exslt.org/common"
  xmlns:msxsl="urn:schemas-microsoft-com:xslt"
  exclude-result-prefixes="exslt msxsl">
  

<msxsl:script language="JScript" implements-prefix="exslt">
 this['node-set'] =  function (x) {
  return x;
  }
</msxsl:script>


<!-- merges attribues on the nodes $a and $b -->
<xsl:template name="merge-attributes">

  <xsl:param name="a" />
  <xsl:param name="b" ><empty /></xsl:param>
  <xsl:param name="a-omit" ><empty /></xsl:param>
    
  <!-- loop the attributes on node a and merge with those on node b -->
  <xsl:for-each select="exslt:node-set($a)/*[1]/@*">
    <xsl:variable name="a-value" select="name()" />
    <xsl:variable name="b-value" select="exslt:node-set($b)/*[1]/@*[name() = $a-value]" />
    <xsl:if test="not(exslt:node-set($a-omit)/*[1]/@*[name() = $a-value])">
    <xsl:choose>
      <!-- merge -->
      <xsl:when test="$b-value != ''">
        <xsl:attribute name="class">
          <xsl:value-of select="$b-value" />
           <xsl:value-of select="' '" />
          <xsl:value-of select="." />
        </xsl:attribute>
      </xsl:when>
       <!-- copy -->
      <xsl:otherwise>
        <xsl:copy />
      </xsl:otherwise>
    </xsl:choose>
    </xsl:if>
  </xsl:for-each>
  
  <!-- loop the attributes on node b NOT on node a (those have been merged already) -->
  <xsl:for-each select="exslt:node-set($b)/*[1]/@*">
    <!-- copy -->
    <xsl:variable name="b-value" select="name()" />
    <xsl:if test="not(exslt:node-set($a)/*[1]/@*[name() = $b-value])">
      <xsl:copy />
    </xsl:if>
  </xsl:for-each>

</xsl:template>

</xsl:stylesheet>
<?xml version='1.0'?>
<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fo="http://www.w3.org/1999/XSL/Format" version="1.0">
	<xsl:import
		href="http://cdn.docbook.org/release/xsl/1.79.2/fo/docbook.xsl" />
	<xsl:param name="toc.section.depth" select="3" />
	<xsl:param name="paper.type" select="'A4'" />
	<xsl:param name="fop1.extensions" select="1" />
	<xsl:param name="ulink.footnotes" select="1" />
	<xsl:param name="header.column.widths">
		1 3 0
	</xsl:param>
	<xsl:template name="article.titlepage.before.recto">
		<fo:block text-align="right" space-after="1cm">
			<fo:external-graphic
				src="https://github.com/Dauphine-MIDO/M1-alternance/raw/master/DauphineBleu.png"
				width="35%" content-width="scale-to-fit" />
		</fo:block>
	</xsl:template>
</xsl:stylesheet>

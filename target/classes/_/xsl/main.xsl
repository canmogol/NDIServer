<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" encoding="utf-8" indent="yes"/>

    <xsl:template match="/response">
        <html>
            <head>
                <title>Main Page</title>
            </head>
            <body>
                <h3><xsl:value-of select="header/message"/></h3>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>
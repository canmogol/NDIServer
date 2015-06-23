<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:include href="importer.xsl"/>
    <xsl:output method="html" encoding="utf-8" indent="yes"/>

    <xsl:template match="/response">
        <html>
            <head>
                <xsl:call-template name="header"/>
                <link rel="stylesheet" type="text/css" href="../_/dev/css/login.css"/>
                <script src="../_/dev/js/login.js"/>
            </head>
            <body>
                <header class="ui menu">
                    <a class="active item" href="login.html">
                        <i class="cloud icon"/>
                        Dev Panel Login
                    </a>
                </header>
                <div class="ui grid">
                    <article class="sixteen wide column">
                        <section>
                            <form action="#" method="get">
                                <div class="ui form segment login">
                                    <div class="field">
                                        <div class="ui left labeled icon input">
                                            <input id="loginPageUsername" type="text" placeholder="Username" autofocus=""/>
                                            <i class="user icon"/>
                                        </div>
                                    </div>
                                    <div class="field">
                                        <div class="ui left labeled icon input">
                                            <input id="loginPagePassword" type="password" placeholder="password"/>
                                            <i class="lock icon"/>
                                        </div>
                                    </div>
                                    <input type="submit" class="ui blue submit button" value="Login" id="loginPageLoginButton"/>
                                </div>
                            </form>
                        </section>
                    </article>
                </div>
                <xsl:call-template name="footer"/>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
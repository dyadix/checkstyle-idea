package org.infernus.idea.checkstyle.importer;


import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.testFramework.LightPlatformTestCase;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.InputSource;

import java.io.StringReader;

public class CodeStyleImporterTest extends LightPlatformTestCase {
    private CodeStyleSettings codeStyleSettings;
    private CommonCodeStyleSettings javaSettings;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        codeStyleSettings = new CodeStyleSettings(false);
        javaSettings = codeStyleSettings.getCommonSettings(JavaLanguage.INSTANCE);
    }

    private final static String FILE_PREFIX = 
            "<?xml version=\"1.0\"?>\n" +
            "<!DOCTYPE module PUBLIC\n" +
            "          \"-//Puppy Crawl//DTD Check Configuration 1.3//EN\"\n" +
            "          \"http://www.puppycrawl.com/dtds/configuration_1_3.dtd\">\n" +
            "<module name = \"Checker\">\n";
    private final static String FILE_SUFFIX =
            "</module>";
    
    private void importConfiguration(@NotNull String configuration) throws CheckstyleException {
        configuration = FILE_PREFIX + configuration + FILE_SUFFIX;
        CheckStyleCodeStyleImporter.importConfiguration(loadConfiguration(configuration), codeStyleSettings);
    }
    
    private String inTreeWalker(@NotNull String configuration) {
        return "<module name=\"TreeWalker\">" + configuration + "</module>";
    }
    
    private Configuration loadConfiguration(@NotNull String configuration) throws CheckstyleException {
        InputSource inputSource = new InputSource(new StringReader(configuration));
        return ConfigurationLoader.loadConfiguration(inputSource, null, false);
    }
    
    public void testImportRightMargin() throws CheckstyleException {
        importConfiguration(
                inTreeWalker(
                        "<module name=\"LineLength\">\n" +
                        "    <property name=\"max\" value=\"100\"/>\n" +
                        "</module>"
                )
        );
        assertEquals(100, javaSettings.RIGHT_MARGIN);
    }
    
    public void testEmptyLineSeparator() throws CheckstyleException {
        javaSettings.BLANK_LINES_AROUND_FIELD = 0;
        javaSettings.BLANK_LINES_AROUND_METHOD = 0;
        importConfiguration(
                inTreeWalker(
                        "<module name=\"EmptyLineSeparator\">\n" +
                        "    <property name=\"tokens\" value=\"VARIABLE_DEF, METHOD_DEF\"/>\n" +
                        "</module>"
                )
        );
        assertEquals(1, javaSettings.BLANK_LINES_AROUND_FIELD);
        assertEquals(1, javaSettings.BLANK_LINES_AROUND_METHOD);
    }
}

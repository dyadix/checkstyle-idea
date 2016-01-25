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
    
    private final static String FILE_PREFIX = 
            "<?xml version=\"1.0\"?>\n" +
            "<!DOCTYPE module PUBLIC\n" +
            "          \"-//Puppy Crawl//DTD Check Configuration 1.3//EN\"\n" +
            "          \"http://www.puppycrawl.com/dtds/configuration_1_3.dtd\">\n" +
            "<module name = \"Checker\">\n";
    private final static String FILE_SUFFIX =
            "</module>";
    
    private CodeStyleSettings importConfiguration(@NotNull String configuration) throws CheckstyleException {
        CodeStyleSettings styleSettings = new CodeStyleSettings(false);
        configuration = FILE_PREFIX + configuration + FILE_SUFFIX;
        CheckStyleCodeStyleImporter.importConfiguration(loadConfiguration(configuration), styleSettings);
        return styleSettings;
    }
    
    private Configuration loadConfiguration(@NotNull String configuration) throws CheckstyleException {
        InputSource inputSource = new InputSource(new StringReader(configuration));
        return ConfigurationLoader.loadConfiguration(inputSource, null, false);
    }
    
    public void testImportRightMargin() throws CheckstyleException {
        CodeStyleSettings settings = importConfiguration(
                "<module name=\"LineLength\">\n" +
                "    <property name=\"max\" value=\"100\"/>\n" + 
                "</module>"
        );
        CommonCodeStyleSettings javaSettings = settings.getCommonSettings(JavaLanguage.INSTANCE);
        assertEquals(100, javaSettings.RIGHT_MARGIN);
    }
}

package org.infernus.idea.checkstyle.importer.modules;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.infernus.idea.checkstyle.importer.ModuleImporter;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class EmptyLineSeparatorImporter extends ModuleImporter {
    private boolean noEmptyLinesBetweenFields = false;
    private final static String NO_EMPTY_LINES_BETWEEN_FIELDS_PROP = "allowNoEmptyLineBetweenFields";

    @Override
    protected boolean handleAttribute(@NotNull final String attrName, @NotNull final String attrValue) {
        if (!super.handleAttribute(attrName, attrValue)) {
            if (NO_EMPTY_LINES_BETWEEN_FIELDS_PROP.equals(attrName)) {
                noEmptyLinesBetweenFields = Boolean.parseBoolean(attrValue);
            }
        }
        return false;
    }

    @Override
    public void importTo(@NotNull final CodeStyleSettings settings) {
        CommonCodeStyleSettings javaSettings = getJavaSettings(settings);
        if (noEmptyLinesBetweenFields) {
            javaSettings.BLANK_LINES_AROUND_FIELD = 0;
        }
        else if (appliesTo(TokenTypes.VARIABLE_DEF)) {
            javaSettings.BLANK_LINES_AROUND_FIELD = 1;
        }
        if (appliesTo(TokenTypes.PACKAGE_DEF)) {
            javaSettings.BLANK_LINES_AFTER_PACKAGE = 1;
        }
        if (appliesTo(TokenTypes.IMPORT)) {
            javaSettings.BLANK_LINES_AFTER_IMPORTS = 1;
        }
        if (appliesTo(TokenTypes.METHOD_DEF)) {
            javaSettings.BLANK_LINES_AROUND_METHOD = 1;
        }
    }

}

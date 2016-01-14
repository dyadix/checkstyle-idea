package org.infernus.idea.checkstyle.importer.modules;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import org.infernus.idea.checkstyle.importer.ModuleImporter;
import org.jetbrains.annotations.NotNull;

public class LineLengthImporter extends ModuleImporter {
    private static final int DEFAULT_MAX_COLUMNS = 80;
    private static final String MAX_PROP = "max";
    private int maxColumns = DEFAULT_MAX_COLUMNS;

    public LineLengthImporter(@NotNull final Configuration moduleConfig) {
        super(moduleConfig);
    }

    @Override
    protected boolean handleAttribute(@NotNull final String attrName, @NotNull final String attrValue) {
        if (MAX_PROP.equals(attrName)) {
            try {
                maxColumns = Integer.parseInt(attrName);
            }
            catch (NumberFormatException nfe) {
                // ignore
            }
        }
        return false;
    }

    @Override
    public void importTo(@NotNull CodeStyleSettings settings) {
        settings.setRightMargin(JavaLanguage.INSTANCE, maxColumns);
    }
}

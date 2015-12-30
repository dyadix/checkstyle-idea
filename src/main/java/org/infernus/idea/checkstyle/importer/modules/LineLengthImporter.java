package org.infernus.idea.checkstyle.importer.modules;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.infernus.idea.checkstyle.importer.ModuleImporter;
import org.jetbrains.annotations.NotNull;

public class LineLengthImporter extends ModuleImporter {
    private static final int DEFAULT_MAX_COLUMNS = 80;
    private static final String MAX_PROP = "max";
    private int maxColumns = DEFAULT_MAX_COLUMNS;

    public LineLengthImporter(@NotNull final CodeStyleSettings settings) {
        super(settings);
    }


    @Override
    protected void handleAttribute(@NotNull final String attrName, @NotNull final String attrValue) {
        if (MAX_PROP.equals(attrName)) {
            try {
                maxColumns = Integer.parseInt(attrName);
            }
            catch (NumberFormatException nfe) {
                // ignore
            }
        }
    }

    @Override
    protected void adjustCodeStyle() {
        settings.setRightMargin(JavaLanguage.INSTANCE, maxColumns);
    }
}

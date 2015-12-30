package org.infernus.idea.checkstyle.importer;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import org.jetbrains.annotations.NotNull;

public abstract class ModuleImporter {
    protected final @NotNull CodeStyleSettings settings;

    public ModuleImporter(@NotNull final CodeStyleSettings settings) {
        this.settings = settings;
    }

    public void importConfig(@NotNull Configuration moduleConfig) {
        for (String attrName : moduleConfig.getAttributeNames()) {
            try {
                handleAttribute(attrName, moduleConfig.getAttribute(attrName));
            } catch (CheckstyleException e) {
                // Ignore, shouldn't happen
            }
        }
        adjustCodeStyle();
    }
    
    @NotNull
    protected CommonCodeStyleSettings getJavaSettings() {
        return this.settings.getCommonSettings(JavaLanguage.INSTANCE);
    }
    
    protected abstract void handleAttribute(@NotNull String attrName, @NotNull String attrValue);
    
    protected abstract void adjustCodeStyle();
}

package org.infernus.idea.checkstyle.importer;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import org.jetbrains.annotations.NotNull;

public abstract class ModuleImporter {

    void importConfig(@NotNull Configuration moduleConfig, @NotNull CodeStyleSettings settings) {
        for (String attrName : moduleConfig.getAttributeNames()) {
            try {
                handleAttribute(attrName, moduleConfig.getAttribute(attrName));
            } catch (CheckstyleException e) {
                // Ignore, shouldn't happen
            }
        }
        adjustCodeStyle(settings);
    }
    
    protected abstract void handleAttribute(@NotNull String attrName, @NotNull String attrValue);
    
    protected abstract void adjustCodeStyle(@NotNull CodeStyleSettings settings);
    
    protected abstract void setDefaults(@NotNull CodeStyleSettings settings);
}

package org.infernus.idea.checkstyle.importer;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import org.jetbrains.annotations.NotNull;

public abstract class ModuleImporter {
    private final static String TOKENS_PROP = "tokens";
    private int[] tokens;

    @NotNull
    protected CommonCodeStyleSettings getJavaSettings(@NotNull CodeStyleSettings settings) {
        return settings.getCommonSettings(JavaLanguage.INSTANCE); 
    }
    
    public void setFrom(@NotNull Configuration moduleConfig) {
        for (String attrName : moduleConfig.getAttributeNames()) {
            try {
                handleAttribute(attrName, moduleConfig.getAttribute(attrName));
            } catch (CheckstyleException e) {
                // Ignore, shouldn't happen
            }
        }
    }

    protected boolean handleAttribute(@NotNull String attrName, @NotNull String attrValue) {
        if (TOKENS_PROP.equals(attrName)) {
            tokens = TokenSetUtil.getTokens(attrValue);
        }
        return false;
    }

    protected boolean appliesTo(int tokenId) {
        if (tokens != null) {
            for (int token : tokens) {
                if (token == tokenId) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
    
    public abstract void importTo(@NotNull CodeStyleSettings settings);
}

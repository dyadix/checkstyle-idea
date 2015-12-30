package org.infernus.idea.checkstyle.importer;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import org.infernus.idea.checkstyle.importer.modules.LineLengthImporter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModuleImporterFactory {
    private final static String LINE_LENGTH_MODULE = "LineLength";
    
    @Nullable
    public static ModuleImporter getModuleImporter(@NotNull Configuration configuration, 
                                                   @NotNull CodeStyleSettings settings) {
        String name = configuration.getName();
        if (LINE_LENGTH_MODULE.equals(name)) {
            return new LineLengthImporter(settings);
        }
        return null;
    }
}

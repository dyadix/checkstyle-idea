package org.infernus.idea.checkstyle.importer;

import com.puppycrawl.tools.checkstyle.api.Configuration;
import org.infernus.idea.checkstyle.importer.modules.EmptyLineSeparatorImporter;
import org.infernus.idea.checkstyle.importer.modules.LineLengthImporter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ModuleImporterFactory {
    
    private static final String LINE_LENGTH_MODULE = "LineLength";
    private static final String EMPTY_LINE_SEPARATOR_MODULE = "EmptyLineSeparator";

    @Nullable
    static ModuleImporter getModuleImporter(@NotNull Configuration configuration) {
        String name = configuration.getName();
        ModuleImporter moduleImporter = null;
        if (LINE_LENGTH_MODULE.equals(name)) {
            moduleImporter = new LineLengthImporter();
        }
        else if (EMPTY_LINE_SEPARATOR_MODULE.equals(name)) {
            moduleImporter = new EmptyLineSeparatorImporter();
        }
        if (moduleImporter != null) {
            moduleImporter.setFrom(configuration);
        }
        return moduleImporter;
    }
}

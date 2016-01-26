package org.infernus.idea.checkstyle.importer;

import com.puppycrawl.tools.checkstyle.api.Configuration;
import org.infernus.idea.checkstyle.importer.modules.EmptyLineSeparatorImporter;
import org.infernus.idea.checkstyle.importer.modules.FileTabCharacterImporter;
import org.infernus.idea.checkstyle.importer.modules.LineLengthImporter;
import org.infernus.idea.checkstyle.importer.modules.WhitespaceAfterImporter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ModuleImporterFactory {
    
    private static final String LINE_LENGTH_MODULE          = "LineLength";
    private static final String EMPTY_LINE_SEPARATOR_MODULE = "EmptyLineSeparator";
    private static final String FILE_TAB_CHAR_MODULE        = "FileTabCharacter";
    private static final String WHITESPACE_AFTER_MODULE     = "WhitespaceAfter";

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
        else if (FILE_TAB_CHAR_MODULE.equals(name)) {
            moduleImporter = new FileTabCharacterImporter();
        }
        else if (WHITESPACE_AFTER_MODULE.equals(name)) {
            moduleImporter = new WhitespaceAfterImporter();
        }
        if (moduleImporter != null) {
            moduleImporter.setFrom(configuration);
        }
        return moduleImporter;
    }
}

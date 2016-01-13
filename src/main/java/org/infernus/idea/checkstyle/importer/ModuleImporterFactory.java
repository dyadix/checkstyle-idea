package org.infernus.idea.checkstyle.importer;

import com.intellij.util.containers.HashMap;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import org.infernus.idea.checkstyle.importer.modules.LineLengthImporter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

class ModuleImporterFactory {
    
    private final static Map<String, ModuleImporter> IMPORTERS_MAP = new HashMap<>();
    static {
        IMPORTERS_MAP.put("LineLength", new LineLengthImporter());
    }
    
    @Nullable
    static ModuleImporter getModuleImporter(@NotNull Configuration configuration) {
        String name = configuration.getName();
        return IMPORTERS_MAP.get(name);
    }
    
    static Collection<ModuleImporter> getAllImporters() {
        return IMPORTERS_MAP.values();
    }
}

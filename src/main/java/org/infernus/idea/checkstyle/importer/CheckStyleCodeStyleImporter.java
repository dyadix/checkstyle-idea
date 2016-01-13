package org.infernus.idea.checkstyle.importer;

import com.intellij.openapi.options.SchemeFactory;
import com.intellij.openapi.options.SchemeImportException;
import com.intellij.openapi.options.SchemeImporter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.codeStyle.CodeStyleScheme;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.InputSource;

import java.io.InputStream;

/**
 * Imports code style settings from check style configuration file.
 */
public class CheckStyleCodeStyleImporter implements SchemeImporter<CodeStyleScheme> {
    @NotNull
    @Override
    public String[] getSourceExtensions() {
        return new String[]{"xml"};
    }

    @Nullable
    @Override
    public CodeStyleScheme importScheme(@NotNull final Project project,
                                        @NotNull final VirtualFile selectedFile,
                                        @NotNull final CodeStyleScheme currentScheme,
                                        @NotNull final SchemeFactory<CodeStyleScheme> schemeFactory) throws SchemeImportException {
        try {
            Configuration configuration = loadConfiguration(selectedFile);
            if (configuration != null) {
                setDefaults(currentScheme);
                importConfiguration(configuration, currentScheme);
                return currentScheme;
            }
        } catch (Exception e) {
            throw new SchemeImportException(e);
        }
        return null;
    }

    @Nullable
    @Override
    public String getAdditionalImportInfo(@NotNull final CodeStyleScheme scheme) {
        return null;
    }
    
    @Nullable
    private Configuration loadConfiguration(@NotNull VirtualFile selectedFile) throws Exception {
        InputStream inputStream = null;
        try {
            inputStream = selectedFile.getInputStream();
            InputSource inputSource = new InputSource(inputStream);
            return ConfigurationLoader.loadConfiguration(inputSource, null, false);
        }
        finally {
            if (inputStream != null) //noinspection ThrowFromFinallyBlock
                inputStream.close();
        }
    }
    
    
    private void setDefaults(@NotNull CodeStyleScheme scheme) {
        for (ModuleImporter importer : ModuleImporterFactory.getAllImporters()) {
            importer.setDefaults(scheme.getCodeStyleSettings());
        }
    }
    
    private void importConfiguration(@NotNull Configuration configuration, CodeStyleScheme scheme) {
        ModuleImporter moduleImporter = 
                ModuleImporterFactory.getModuleImporter(configuration);
        if (moduleImporter != null) moduleImporter.importConfig(configuration, scheme.getCodeStyleSettings());
        for (Configuration childConfig : configuration.getChildren()) {
            importConfiguration(childConfig, scheme);
        }
    }
}

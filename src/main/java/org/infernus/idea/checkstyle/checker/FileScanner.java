package org.infernus.idea.checkstyle.checker;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.infernus.idea.checkstyle.CheckStylePlugin;
import org.infernus.idea.checkstyle.model.ConfigurationLocation;
import org.infernus.idea.checkstyle.toolwindow.CheckStyleToolWindowPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Runnable for scanning an individual file.
 */
final class FileScanner implements Runnable {

    private static final Log LOG = LogFactory.getLog(FileScanner.class);

    private final CheckStylePlugin plugin;
    private final Set<PsiFile> filesToScan;
    private final ConfigurationLocation overrideConfigLocation;

    private ConfigurationLocationStatus configurationLocationStatus = ConfigurationLocationStatus.PRESENT;
    private Map<PsiFile, List<Problem>> results;
    private Throwable error;

    FileScanner(final CheckStylePlugin checkStylePlugin,
                       final Set<PsiFile> filesToScan,
                       final ConfigurationLocation overrideConfigLocation) {
        this.plugin = checkStylePlugin;
        this.filesToScan = filesToScan;
        this.overrideConfigLocation = overrideConfigLocation;
    }

    public void run() {
        try {
            results = checkPsiFile(filesToScan, overrideConfigLocation);

            final CheckStyleToolWindowPanel toolWindowPanel = CheckStyleToolWindowPanel.panelFor(plugin.getProject());
            if (toolWindowPanel != null) {
                toolWindowPanel.incrementProgressBarBy(filesToScan.size());
            }
        } catch (Throwable e) {
            error = e;
        }
    }

    @NotNull
    public Map<PsiFile, List<Problem>> getResults() {
        if (results != null) {
            return Collections.unmodifiableMap(results);
        }

        return Collections.emptyMap();
    }

    public Throwable getError() {
        return error;
    }

    ConfigurationLocationStatus getConfigurationLocationStatus() {
        return configurationLocationStatus;
    }

    private Map<PsiFile, List<Problem>> checkPsiFile(final Set<PsiFile> psiFilesToScan,
                                                     final ConfigurationLocation override) {
        if (psiFilesToScan == null || psiFilesToScan.isEmpty()) {
            LOG.debug("No elements were specified");
            return null;
        }

        final List<ScannableFile> scannableFiles = new ArrayList<>();

        try {
            final Module module = ModuleUtil.findModuleForPsiElement(psiFilesToScan.iterator().next());
            final ConfigurationLocation location = configurationLocation(override, module);
            if (module == null || location == null) {
                return null;
            }

            scannableFiles.addAll(ScannableFile.createAndValidate(psiFilesToScan, plugin, module));

            return checkerFactory(module)
                    .checker(module, location)
                    .map(checker -> checker.scan(scannableFiles, plugin.getConfiguration()))
                    .orElseGet(Collections::emptyMap);

        } finally {
            scannableFiles.forEach(ScannableFile::deleteIfRequired);
        }
    }

    @Nullable
    private ConfigurationLocation configurationLocation(final ConfigurationLocation override, final Module module) {
        final ConfigurationLocation location = plugin.getConfigurationLocation(module, override);
        if (location == null) {
            configurationLocationStatus = ConfigurationLocationStatus.NOT_PRESENT;
            return null;
        }
        if (location.isBlacklisted()) {
            configurationLocationStatus = ConfigurationLocationStatus.BLACKLISTED;
            return null;
        }
        return location;
    }

    private CheckerFactory checkerFactory(final Module project) {
        return ServiceManager.getService(project.getProject(), CheckerFactory.class);
    }

}

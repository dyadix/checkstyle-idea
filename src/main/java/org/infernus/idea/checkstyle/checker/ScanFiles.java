package org.infernus.idea.checkstyle.checker;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.RuntimeInterruptedException;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.infernus.idea.checkstyle.CheckStylePlugin;
import org.infernus.idea.checkstyle.exception.CheckStylePluginException;
import org.infernus.idea.checkstyle.model.ConfigurationLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.Callable;

import static com.intellij.openapi.util.Pair.pair;
import static java.util.Collections.emptyMap;
import static org.infernus.idea.checkstyle.checker.ConfigurationLocationStatus.*;

public class ScanFiles implements Callable<Map<PsiFile, List<Problem>>> {
    private static final Log LOG = LogFactory.getLog(ScanFiles.class);

    private final List<PsiFile> files;
    private final Map<Module, Set<PsiFile>> moduleToFiles;
    private final Set<ScannerListener> listeners = new HashSet<>();
    private final CheckStylePlugin plugin;
    private final ConfigurationLocation overrideConfigLocation;

    public ScanFiles(@NotNull final CheckStylePlugin checkStylePlugin,
                     @NotNull final List<VirtualFile> virtualFiles,
                     @Nullable final ConfigurationLocation overrideConfigLocation) {
        this.plugin = checkStylePlugin;
        this.overrideConfigLocation = overrideConfigLocation;

        files = findAllFilesFor(virtualFiles);
        moduleToFiles = mapsModulesToFiles();
    }

    private List<PsiFile> findAllFilesFor(@NotNull final List<VirtualFile> virtualFiles) {
        final List<PsiFile> childFiles = new ArrayList<>();
        final PsiManager psiManager = PsiManager.getInstance(this.plugin.getProject());
        for (final VirtualFile virtualFile : virtualFiles) {
            childFiles.addAll(buildFilesList(psiManager, virtualFile));
        }
        return childFiles;
    }

    private Map<Module, Set<PsiFile>> mapsModulesToFiles() {
        final Map<Module, Set<PsiFile>> modulesToFiles = new HashMap<>();
        for (final PsiFile file : files) {
            final Module module = ModuleUtil.findModuleForPsiElement(file);
            Set<PsiFile> filesForModule = modulesToFiles.get(module);
            if (filesForModule == null) {
                filesForModule = new HashSet<>();
                modulesToFiles.put(module, filesForModule);
            }
            filesForModule.add(file);
        }
        return modulesToFiles;
    }

    @Override
    public final Map<PsiFile, List<Problem>> call() throws Exception {
        Map<PsiFile, List<Problem>> scanResults = emptyMap();

        try {
            fireCheckStarting(files);
            final Pair<ConfigurationLocationStatus, Map<PsiFile, List<Problem>>> scanResult = processFilesForModuleInfoAndScan();
            fireCheckComplete(scanResult.first, scanResult.second);

        } catch (final RuntimeInterruptedException e) {
            LOG.debug("Scan cancelled by IDEA", e);
            fireCheckComplete(PRESENT, emptyMap());

        } catch (final Throwable e) {
            final CheckStylePluginException processedError = CheckStylePluginException.wrap(
                    "An error occurred during a file scan.", e);

            if (processedError != null) {
                LOG.error("An error occurred while scanning a file.", processedError);
                fireErrorCaught(processedError);
            }

            fireCheckComplete(PRESENT, emptyMap());
        }
        return scanResults;
    }

    public void addListener(ScannerListener listener) {
        listeners.add(listener);
    }

    private void fireCheckStarting(final List<PsiFile> filesToScan) {
        listeners.forEach(listener -> listener.scanStarting(filesToScan));
    }

    private void fireCheckComplete(final ConfigurationLocationStatus configLocationStatus, Map<PsiFile, List<Problem>> fileResults) {
        listeners.forEach(listener -> listener.scanComplete(configLocationStatus, fileResults));
    }

    private void fireErrorCaught(final CheckStylePluginException error) {
        listeners.forEach(listener -> listener.errorCaught(error));
    }

    private void fireFilesScanned(final int count) {
        listeners.forEach(listener -> listener.filesScanned(count));
    }

    private List<PsiFile> buildFilesList(final PsiManager psiManager, final VirtualFile virtualFile) {
        final List<PsiFile> allChildFiles = new ArrayList<>();
        ApplicationManager.getApplication().runReadAction(() -> {
            final FindChildFiles visitor = new FindChildFiles(virtualFile, psiManager);
            VfsUtilCore.visitChildrenRecursively(virtualFile, visitor);
            allChildFiles.addAll(visitor.locatedFiles);
        });
        return allChildFiles;
    }

    private Pair<ConfigurationLocationStatus, Map<PsiFile, List<Problem>>> processFilesForModuleInfoAndScan() {
        final Map<PsiFile, List<Problem>> fileResults = new HashMap<>();

        for (final Module module : moduleToFiles.keySet()) {
            if (module == null) {
                continue;
            }

            final Pair<ConfigurationLocationStatus, ConfigurationLocation> location = configurationLocation(overrideConfigLocation, module);
            if (location.first != PRESENT) {
                return pair(location.first, emptyMap());
            }

            final Set<PsiFile> filesForModule = moduleToFiles.get(module);
            if (filesForModule.isEmpty()) {
                continue;
            }

            fileResults.putAll(filesWithProblems(filesForModule, checkFiles(module, filesForModule, location.second)));
            fireFilesScanned(filesForModule.size());
        }

        return pair(PRESENT, fileResults);
    }

    @NotNull
    private Map<PsiFile, List<Problem>> filesWithProblems(final Set<PsiFile> filesForModule,
                                                          final Map<PsiFile, List<Problem>> moduleFileResults) {
        final Map<PsiFile, List<Problem>> moduleResults = new HashMap<>();
        for (final PsiFile psiFile : filesForModule) {
            final List<Problem> resultsForFile = moduleFileResults.get(psiFile);
            if (resultsForFile != null && !resultsForFile.isEmpty()) {
                moduleResults.put(psiFile, new ArrayList<>(resultsForFile));
            }
        }
        return moduleResults;
    }

    @NotNull
    private Pair<ConfigurationLocationStatus, ConfigurationLocation> configurationLocation(final ConfigurationLocation override,
                                                                                           final Module module) {
        final ConfigurationLocation location = plugin.getConfigurationLocation(module, override);
        if (location == null) {
            return pair(NOT_PRESENT, null);
        }
        if (location.isBlacklisted()) {
            return pair(BLACKLISTED, null);
        }
        return pair(PRESENT, location);
    }

    private Map<PsiFile, List<Problem>> checkFiles(final Module module,
                                                   final Set<PsiFile> filesToScan,
                                                   final ConfigurationLocation configurationLocation) {
        final List<ScannableFile> scannableFiles = new ArrayList<>();
        try {
            scannableFiles.addAll(ScannableFile.createAndValidate(filesToScan, plugin, module));

            return checkerFactory(module)
                    .checker(module, configurationLocation)
                    .map(checker -> checker.scan(scannableFiles, plugin.getConfiguration()))
                    .orElseGet(Collections::emptyMap);

        } finally {
            scannableFiles.forEach(ScannableFile::deleteIfRequired);
        }
    }

    private CheckerFactory checkerFactory(final Module project) {
        return ServiceManager.getService(project.getProject(), CheckerFactory.class);
    }

    private class FindChildFiles extends VirtualFileVisitor {
        private final VirtualFile virtualFile;
        private final PsiManager psiManager;

        public final List<PsiFile> locatedFiles = new ArrayList<>();

        FindChildFiles(final VirtualFile virtualFile, final PsiManager psiManager) {
            this.virtualFile = virtualFile;
            this.psiManager = psiManager;
        }

        @Override
        public boolean visitFile(@NotNull final VirtualFile file) {
            if (!file.isDirectory()) {
                final PsiFile psiFile = psiManager.findFile(virtualFile);
                if (psiFile != null) {
                    locatedFiles.add(psiFile);
                }
            }
            return true;
        }

    }
}

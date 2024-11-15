package com.zhipin.diamond.utils;

import com.intellij.ide.util.JavaAnonymousClassesHelper;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.zhipin.diamond.config.ApplicationConfig;
import com.zhipin.diamond.config.PluginConfig;
import com.zhipin.diamond.http.HttpService;
import com.zhipin.diamond.http.HttpServiceFactory;
import com.zhipin.diamond.model.ApplicationInfo;
import com.zhipin.diamond.model.HotfixResult;
import com.zhipin.diamond.model.Result;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.zhipin.diamond.constants.DiamondConstants.EMPTY_APPLICATION_LIST_TXT;
import static com.zhipin.diamond.model.Result.SUCCESS_CODE;
import static com.zhipin.diamond.utils.ReloadUtil.filterApplicationInfoList;
import static com.zhipin.diamond.utils.ReloadUtil.notifyFailed;
import static com.zhipin.diamond.utils.ReloadUtil.notifySuccess;
import static java.util.stream.Collectors.toList;

/**
 * @author yinnan@kanzhun.com
 * @date 2023/8/23 11:51
 */
public class HotFixActionUtils {
    private static Logger logger = Logger.getInstance(HotFixActionUtils.class);

    public static void handleHotfixAction(Project project, VirtualFile virtualFile, ApplicationConfig applicationConfig) {
        logger.info("Reload action performed");
        if (project == null || virtualFile == null || applicationConfig == null || StringUtils.isBlank(applicationConfig.getSelectedApplicationName())) {
            return;
        }
        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (psiFile instanceof PsiClassOwner) {
            Module module = ModuleUtilCore.findModuleForPsiElement(psiFile);
            CompilerModuleExtension compilerModuleExtension = CompilerModuleExtension.getInstance(module);
            CompilerManager compilerManager = CompilerManager.getInstance(project);
            VirtualFile[] files = {virtualFile};
            if ("class".equals(virtualFile.getExtension())) {
                reloadClassFile(project, virtualFile, applicationConfig);
            } else if (!virtualFile.isInLocalFileSystem() && !virtualFile.isWritable()) {
                // source file in a library
            } else {
                Application application = ApplicationManager.getApplication();
                application.runWriteAction(() -> FileDocumentManager.getInstance().saveAllDocuments());

                CompileScope compileScope = compilerManager.createFilesCompileScope(files);
                final VirtualFile[] result = {null};
                VirtualFile[] outputDirectories = compilerModuleExtension == null ? null
                        : compilerModuleExtension.getOutputRoots(true);

                if (outputDirectories != null && compilerManager.isUpToDate(compileScope)) {
                    result[0] = findClassFile(outputDirectories, psiFile);
                }
                log("Files " + Arrays.toString(files));
                compilerManager.compile(files, (aborted, errors, warnings, compileContext) -> {
                    if (errors == 0) {
                        VirtualFile[] outputRoots =
                                compilerModuleExtension.getOutputRoots(true);
                        log("Output Directories " + Arrays.toString(outputRoots));
                        if (outputRoots != null) {
                            result[0] = findClassFile(outputRoots, psiFile);
                        }
                        reloadClassFile(project, result[0], applicationConfig);
                    } else {
                        log("Compile error " + errors);
                    }
                });
            }
        }
    }

    private static VirtualFile findClassFile(final VirtualFile[] outputDirectories, final PsiFile psiFile) {
        return ApplicationManager.getApplication().runReadAction(new Computable<VirtualFile>() {

            @Override
            public VirtualFile compute() {
                if (outputDirectories != null && psiFile instanceof PsiClassOwner) {
                    FileEditor editor = FileEditorManager.getInstance(psiFile.getProject())
                            .getSelectedEditor(psiFile.getVirtualFile());
                    int caretOffset = editor == null ? -1 : ((TextEditor) editor).getEditor().getCaretModel()
                            .getOffset();
                    if (caretOffset >= 0) {
                        PsiElement psiElement = psiFile.findElementAt(caretOffset);
                        PsiClass classAtCaret = findClassAtCaret(psiElement);
                        if (classAtCaret != null) {
                            return getClassFile(classAtCaret);
                        }
                    }
                    PsiClassOwner psiJavaFile = (PsiClassOwner) psiFile;
                    for (PsiClass psiClass : psiJavaFile.getClasses()) {
                        final VirtualFile file = getClassFile(psiClass);
                        if (file != null) {
                            return file;
                        }
                    }
                }
                return null;
            }

            private VirtualFile getClassFile(PsiClass psiClass) {
                String className = psiClass.getQualifiedName();
                if (className == null) {
                    if (psiClass instanceof PsiAnonymousClass) {
                        PsiClass parentClass = PsiTreeUtil.getParentOfType(psiClass, PsiClass.class);
                        if (parentClass != null) {
                            className = parentClass.getQualifiedName() + JavaAnonymousClassesHelper
                                    .getName((PsiAnonymousClass) psiClass);
                        }
                    } else {
                        className = PsiTreeUtil.getParentOfType(psiClass, PsiClass.class).getQualifiedName();
                    }
                }
                StringBuilder sb = new StringBuilder(className);
                while (psiClass.getContainingClass() != null) {
                    sb.setCharAt(sb.lastIndexOf("."), '$');
                    psiClass = psiClass.getContainingClass();
                }
                String classFileName = sb.toString().replace('.', '/') + ".class";
                for (VirtualFile outputDirectory : outputDirectories) {
                    final VirtualFile file = outputDirectory.findFileByRelativePath(classFileName);
                    if (file != null && file.exists()) {
                        return file;
                    }
                }
                return null;
            }

            private PsiClass findClassAtCaret(PsiElement psiElement) {
                while (psiElement != null) {
                    if (psiElement instanceof PsiClass) {
                        return (PsiClass) psiElement;
                    }
                    psiElement = psiElement.getParent();
                    findClassAtCaret(psiElement);
                }
                return null;
            }
        });
    }

    private static void reloadClassFile(final Project project, final VirtualFile file, ApplicationConfig applicationConfig) {
        log("Sync " + project + ' ' + file);
        if (file == null) {
            return;
        }
        Application application = ApplicationManager.getApplication();
        application.invokeLater(() -> file.refresh(false, false,
                () -> startReloadTask(project, file, applicationConfig)));
    }

    private static void startReloadTask(Project project, VirtualFile file, ApplicationConfig applicationConfig) {
        ProgressManager.getInstance().run(new Backgroundable(project, "Uploading Class" + file.getName()) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    uploadAndReloadClass(project, file, applicationConfig);
                    notifySuccess();
                } catch (Exception e) {
                    notifyFailed(e.getMessage());
                    log(e.getMessage(), e);
                }
            }
        });
    }

    private static void uploadAndReloadClass(final Project project, final VirtualFile file, ApplicationConfig applicationConfig) throws Exception {
        if (Objects.isNull(applicationConfig)
                || StringUtils.isBlank(applicationConfig.getSelectedApplicationName())) {
            showDialogWithError(project, EMPTY_APPLICATION_LIST_TXT);
        }
        Result<HotfixResult> result = doContainerHotfix(file, applicationConfig);
        checkNotNull(result);
        if (result.getCode() == SUCCESS_CODE) {
            return;
        }
        boolean success = tryRefreshApplicationList(project, applicationConfig);
        if (!success) {
            showDialogWithError(project, EMPTY_APPLICATION_LIST_TXT);
        }
        result = doContainerHotfix(file, applicationConfig);
        checkNotNull(result);
        if (result.getCode() == SUCCESS_CODE) {
            return;
        }
        throw new RuntimeException(result.getMsg());
    }

    private static Result<HotfixResult> doContainerHotfix(VirtualFile file, ApplicationConfig applicationConfig) throws IOException {
        HttpService httpService = HttpServiceFactory.getInstance();
        RequestBody requestBody = RequestBody.create(MediaType.get("multipart/form-data"), file.contentsToByteArray());
        MultipartBody.Part classFile = MultipartBody.Part.createFormData("file", URLEncoder.encode(file.getName(), "UTF-8"), requestBody);
        RequestBody applicationNamePart = RequestBody.create(MediaType.parse("multipart/form-data"), applicationConfig.getSelectedApplicationName());
        RequestBody groupPart = RequestBody.create(MediaType.parse("multipart/form-data"), applicationConfig.calculateGroupName());
        Call<Result<HotfixResult>> hotReloadResultCall = httpService.reloadContainerClass(classFile, applicationNamePart, groupPart);
        return hotReloadResultCall.execute().body();
    }

    private static boolean tryRefreshApplicationList(Project project, ApplicationConfig applicationConfig) {
        List<ApplicationInfo> filterApplicationInfoList = filterApplicationInfoList(applicationConfig);
        List<String> applicationNameList = filterApplicationInfoList.stream().map(ApplicationInfo::getApplicationName).collect(toList());
        String selectedApplication = applicationNameList.stream()
                .filter(meta -> meta.equalsIgnoreCase(applicationConfig.getSelectedApplicationName()))
                .findFirst()
                .orElse(null);
        if (StringUtils.isBlank(selectedApplication)) {
            return false;
        }
        applicationConfig.setSelectedApplicationName(selectedApplication);
        return true;
    }

    private static void showDialogWithError(Project project, String errorMsg) {
        ApplicationManager.getApplication().invokeLater(
                () -> ShowSettingsUtil.getInstance().showSettingsDialog(project, PluginConfig.class));
        throw new RuntimeException(errorMsg);
    }

    public static void handleHotfixSourceAction(Project project, VirtualFile virtualFile, ApplicationConfig applicationConfig) {
        logger.info("Reload Source action performed");
        if (project == null || virtualFile == null || applicationConfig == null || StringUtils.isBlank(applicationConfig.getSelectedApplicationName())) {
            return;
        }


        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (psiFile instanceof PsiClassOwner) {
            if ("class".equals(virtualFile.getExtension())) {
                reloadClassFile(project, virtualFile, applicationConfig);
            } else if (!virtualFile.isInLocalFileSystem() && !virtualFile.isWritable()) {
                // source file in a library
            } else {
                Application application = ApplicationManager.getApplication();
                application.runWriteAction(() -> FileDocumentManager.getInstance().saveAllDocuments());
                Document currentDoc = FileEditorManager.getInstance(project).getSelectedTextEditor().getDocument();
                VirtualFile currentFile = FileDocumentManager.getInstance().getFile(currentDoc);
                reloadSourceFile(project, currentFile, applicationConfig);
            }
        }
//        if (psiFile instanceof PsiPlainTextFile) {
//            if ("sql".equals(virtualFile.getExtension())) {
//                Application application = ApplicationManager.getApplication();
//                application.runWriteAction(() -> FileDocumentManager.getInstance().saveAllDocuments());
//                Document currentDoc = FileEditorManager.getInstance(project).getSelectedTextEditor().getDocument();
//                VirtualFile currentFile = FileDocumentManager.getInstance().getFile(currentDoc);
//                reloadSourceFile(project, currentFile, applicationName);
//            }
//        }
    }

    private static void reloadSourceFile(final Project project, final VirtualFile file, ApplicationConfig applicationConfig) {
        log("Sync Source " + project + ' ' + file);
        if (file == null) {
            return;
        }
        Application application = ApplicationManager.getApplication();
        application.invokeLater(() -> file.refresh(false, false,
                () -> startReloadSourceTask(project, file, applicationConfig)));
    }


    private static void startReloadSourceTask(Project project, VirtualFile file, ApplicationConfig applicationConfig) {
        ProgressManager.getInstance().run(new Backgroundable(project, "Uploading Source " + file.getName()) {
            long timeMillis = System.currentTimeMillis();

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    uploadAndReloadClass(project, file, applicationConfig);
                    notifySuccess();
                } catch (Exception e) {
                    notifyFailed(e.getMessage());
                    log(e.getMessage(), e);
                }
            }
        });
    }

    private static void log(String message) {
        logger.info(message);
    }

    private static void log(String message, Throwable throwable) {
        logger.info(message, throwable);
    }
}

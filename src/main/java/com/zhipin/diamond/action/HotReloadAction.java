package com.zhipin.diamond.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.IPopupChooserBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.zhipin.diamond.component.SettingStorage;
import com.zhipin.diamond.config.ApplicationConfig;
import com.zhipin.diamond.config.PluginConfig;
import com.zhipin.diamond.model.HotFixAction;
import com.zhipin.diamond.utils.HotFixActionUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

import static com.zhipin.diamond.utils.ReloadUtil.getApplicationList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author yinnan@kanzhun.com
 * @date 2023/8/18 11:59
 */
public class HotReloadAction  extends AnAction {

    private Logger logger = Logger.getInstance(HotReloadAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        Project project = e.getData(CommonDataKeys.PROJECT);
        assert project != null;
        List<String> applicationNameList = getApplicationList();
        if (isEmpty(applicationNameList)) {
            return;
        }
        ApplicationConfig applicationConfig = SettingStorage.getApplicationConfig(project);
        String applicationNameKeyword = applicationConfig.getApplicationNameKeyword();
        if (isNotBlank(applicationNameKeyword)) {
            String lowerCase = applicationNameKeyword.toLowerCase();
            applicationNameList = applicationNameList.stream()
                    .filter(application -> application.toLowerCase().contains(lowerCase))
                    .collect(toList());
        }
        if (CollectionUtils.isEmpty(applicationNameList)) {
            ApplicationManager.getApplication().invokeLater(
                    () -> ShowSettingsUtil.getInstance().showSettingsDialog(project, PluginConfig.class));
            throw new RuntimeException("application 为空，请检查 applicationNameKeyword配置");
        }
        if (isNotBlank(applicationConfig.getSelectedApplicationName())) {
            Comparator<String> lastSelectedComparator = Comparator.comparing(app -> applicationConfig.getSelectedApplicationName().equalsIgnoreCase(app), Comparator.reverseOrder());
            applicationNameList.sort(lastSelectedComparator);
        }
        JBPopupFactory instance = JBPopupFactory.getInstance();
        IPopupChooserBuilder<String> popupChooserBuilder = instance.createPopupChooserBuilder(applicationNameList)
                .setItemChosenCallback(applicationName -> {
            applicationConfig.setSelectedApplicationName(applicationName);
            HotFixActionUtils.handleHotfixAction(project, virtualFile, applicationName);
        }).setTitle("Select One Application");
        popupChooserBuilder.createPopup().showInFocusCenter();
    }
}

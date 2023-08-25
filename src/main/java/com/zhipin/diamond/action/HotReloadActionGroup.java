package com.zhipin.diamond.action;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.zhipin.diamond.component.SettingStorage;
import com.zhipin.diamond.config.ApplicationConfig;
import com.zhipin.diamond.config.PluginConfig;
import com.zhipin.diamond.utils.HotFixActionUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

import static com.zhipin.diamond.utils.ReloadUtil.getApplicationList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author yinnan@kanzhun.com
 * @date 2023/8/23 13:35
 */
public class HotReloadActionGroup extends ActionGroup {


    private static final ShowConfigAction SHOW_CONFIG_ACTION = new ShowConfigAction();

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        Project project = e.getData(CommonDataKeys.PROJECT);
        assert project != null;
        List<String> applicationNameList = getApplicationList();
        if (isEmpty(applicationNameList)) {
            return new AnAction[]{SHOW_CONFIG_ACTION};
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
            return new AnAction[]{SHOW_CONFIG_ACTION};
        }
        if (isNotBlank(applicationConfig.getSelectedApplicationName())) {
            Comparator<String> lastSelectedComparator = Comparator.comparing(app -> applicationConfig.getSelectedApplicationName().equalsIgnoreCase(app), Comparator.reverseOrder());
            applicationNameList.sort(lastSelectedComparator);
        }
        List<AnAction> childrenActionList = applicationNameList.stream().map(applicationName -> buildAction(project, virtualFile, applicationName)).collect(toList());
        return childrenActionList.toArray(AnAction[]::new);
    }

    private AnAction buildAction(Project project, VirtualFile virtualFile, String applicationName) {
        return new HotReloadChildrenAction(project, virtualFile, applicationName);
    }

    public static final class HotReloadChildrenAction extends AnAction {

        private Project project;

        private VirtualFile virtualFile;

        private String applicationName;

        public HotReloadChildrenAction(Project project, VirtualFile virtualFile, String applicationName) {
            super(applicationName);
            this.project = project;
            this.virtualFile = virtualFile;
            this.applicationName = applicationName;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            ApplicationConfig applicationConfig = SettingStorage.getApplicationConfig(project);
            applicationConfig.setSelectedApplicationName(applicationName);
            HotFixActionUtils.handleHotfixAction(project, virtualFile, applicationName);
        }
    }

    public static final class ShowConfigAction extends AnAction {

        public ShowConfigAction() {
            super("Application Is Empty,Please Check ServerUrl or ApplicationNameKeyword");
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = e.getData(CommonDataKeys.PROJECT);
            ShowSettingsUtil.getInstance().showSettingsDialog(project, PluginConfig.class);
        }
    }
}

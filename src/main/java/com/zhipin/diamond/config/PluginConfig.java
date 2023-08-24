package com.zhipin.diamond.config;

import javax.swing.JComponent;

import com.intellij.openapi.options.Configurable.VariableProjectAppLevel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.roots.ui.configuration.ProjectConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.roots.ui.configuration.projectRoot.StructureConfigurableContext;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import com.zhipin.diamond.component.SettingPanel;
import com.zhipin.diamond.component.SettingStorage;
import com.intellij.openapi.options.Configurable;

/**
 * @author yinnan@kanzhun.com
 * @date 2023/8/18 12:04
 */
public class PluginConfig implements Configurable {


    private Project project;
    private SettingPanel hotReloadConfiguration;

    public PluginConfig(Project project) {
        this.project = project;
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "HotReloadConfig";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (hotReloadConfiguration == null) {
            hotReloadConfiguration = new SettingPanel(project);
            hotReloadConfiguration.initComponent();
        }
        return hotReloadConfiguration.getRootPanel();
    }

    @Override
    public boolean isModified() {
        return hotReloadConfiguration != null && hotReloadConfiguration.isModifiable();
    }

    @Override
    public void apply() {
        if (hotReloadConfiguration != null) {
            hotReloadConfiguration.getData();
        }
    }

    @Override
    public void reset() {
        if (hotReloadConfiguration != null) {
            hotReloadConfiguration.setData();
        }
    }
}

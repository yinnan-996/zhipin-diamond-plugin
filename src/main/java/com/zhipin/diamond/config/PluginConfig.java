package com.zhipin.diamond.config;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.zhipin.diamond.component.SettingPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

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

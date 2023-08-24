package com.zhipin.diamond.component;

import com.intellij.openapi.options.Configurable.VariableProjectAppLevel;
import com.intellij.openapi.project.Project;
import com.zhipin.diamond.model.HotFixAction;
import com.zhipin.diamond.utils.HotFixActionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.zhipin.diamond.config.ApplicationConfig;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author yinnan@kanzhun.com
 * @date 2023/8/18 12:01
 */
@State(name = "DiamondPluginConfiguration", storages = {@Storage("diamondPlugin.xml")})
public class SettingStorage implements PersistentStateComponent<ApplicationConfig>, VariableProjectAppLevel {

    private ApplicationConfig applicationConfig = new ApplicationConfig();

    @Nullable
    @Override
    public ApplicationConfig getState() {
        return applicationConfig;
    }

    @Override
    public void loadState(@NotNull ApplicationConfig state) {
        applicationConfig = state;
    }

    public static SettingStorage getInstance(Project project) {
        return project.getComponent(SettingStorage.class);
    }

    public static ApplicationConfig getApplicationConfig(Project project) {
        SettingStorage settingStorage = project.getComponent(SettingStorage.class);
        return settingStorage.getState();
    }
    @Override
    public boolean isProjectLevel() {
        return true;
    }
}

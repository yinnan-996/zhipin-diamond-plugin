package com.zhipin.diamond.component;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.Configurable.VariableProjectAppLevel;
import com.zhipin.diamond.config.ApplicationConfig;
import com.zhipin.diamond.http.HttpServiceFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yinnan@kanzhun.com
 * @date 2023/8/25 15:34
 */
@State(name = "DiamondServerPluginConfiguration", storages = {@Storage("diamondServerPlugin.xml")})
public class ServerUrlSettingStorage implements PersistentStateComponent<String> {

    private static String SERVER_URL = "https://diamond-web-qa.weizhipin.com/";
    public static String DEFAULT_SERVER_URL = "https://diamond-web-qa.weizhipin.com/";
    @Override
    public @Nullable String getState() {
        return SERVER_URL;
    }

    @Override
    public void loadState(@NotNull String state) {
        SERVER_URL = state;
        HttpServiceFactory.setServer(SERVER_URL);
    }

    public static ServerUrlSettingStorage getInstance() {
        return ApplicationManager.getApplication().getComponent(ServerUrlSettingStorage.class);
    }



    public static String getServerUrl() {
        return ApplicationManager.getApplication().getComponent(ServerUrlSettingStorage.class).getState();
    }

    public static void setServerUrl(String serverUrl) {
        ApplicationManager.getApplication().getComponent(ServerUrlSettingStorage.class).loadState(serverUrl);
    }
}

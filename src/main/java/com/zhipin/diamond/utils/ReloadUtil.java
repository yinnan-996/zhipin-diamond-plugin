package com.zhipin.diamond.utils;

import static com.zhipin.diamond.utils.Constants.DEFAULT_HOST;
import static com.zhipin.diamond.utils.Constants.DEFAULT_PROTOCOL;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.zhipin.diamond.http.HttpService;
import com.zhipin.diamond.http.HttpServiceFactory;
import com.zhipin.diamond.model.JvmProcess;
import com.zhipin.diamond.model.Result;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.ui.popup.Balloon;

/**
 * @author yinnan@kanzhun.com
 * @date 2023/8/18 12:10
 */

public class ReloadUtil {

    private static final Joiner KEYWORD_JOINER = Joiner.on(",").skipNulls();
    private static final Splitter KEYWORD_SPLITTER = Splitter.on(",").trimResults();

    private ReloadUtil() {
    }

    public static List<JvmProcess> filterProcess(List<JvmProcess> allProcesses, List<String> keywords) {
        if (isEmpty(allProcesses) || isEmpty(keywords)) {
            return emptyList();
        }
        return allProcesses.stream()
                .filter(process -> process.containsKeywords(keywords))
                .collect(toList());
    }

    public static List<String> filterHost(List<String> hostList, String keyword) {
        if (isEmpty(hostList) || StringUtils.isBlank(keyword)) {
            return emptyList();
        }
        return hostList.stream()
                .filter(host -> StringUtils.contains(host, keyword))
                .collect(toList());
    }

    public static String joinKeywords(Collection<String> keywords) {
        return KEYWORD_JOINER.join(keywords);
    }

    public static List<String> splitKeywordsText(String keywordText) {
        return KEYWORD_SPLITTER.splitToList(keywordText);
    }

    public static List<String> getApplicationList() {
        HttpService httpService = HttpServiceFactory.getInstance();
        try {
            Result<List<String>> result = httpService.applicationList().execute().body();
            return ofNullable(result)
                    .map(Result::getData)
                    .map(list -> list.stream().sorted().collect(Collectors.toList()))
                    .orElse(emptyList());
        } catch (IOException e) {
            return emptyList();
        }
    }


    public static List<JvmProcess> getProcessList(String hostName) {
        HttpService httpService = HttpServiceFactory.getInstance();
        if (StringUtils.equals(hostName, DEFAULT_HOST)) {
            hostName = null;
        }
        if (hostName != null) {
            hostName = DEFAULT_PROTOCOL + hostName;
        }
        try {
            // TODO: replace port
            Result<List<JvmProcess>> result = httpService.processList(hostName).execute().body();
            return ofNullable(result).map(Result::getData).filter(CollectionUtils::isNotEmpty).orElse(emptyList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void notifySuccess() {
        notify("Hotfix success.", "", NotificationType.INFORMATION);
    }

    public static void notifyFailed(String message) {
        notify("Hotfix error: ", message, NotificationType.ERROR);
    }

    public static void notify(String title, String message, NotificationType type) {
        Notification notification = new Notification("hotfix", title, Strings.nullToEmpty(message), type);
        Notifications.Bus.notify(notification);
        Optional.ofNullable(notification.getBalloon()).ifPresent(Balloon::hide);
    }
}

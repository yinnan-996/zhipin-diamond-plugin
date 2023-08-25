package com.zhipin.diamond.component;

import com.intellij.openapi.project.Project;
import com.zhipin.diamond.config.ApplicationConfig;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.zhipin.diamond.component.ServerUrlSettingStorage.DEFAULT_SERVER_URL;
import static com.zhipin.diamond.utils.Constants.NEED_SELECT_APPLICATION;
import static com.zhipin.diamond.utils.ReloadUtil.getApplicationList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author yinnan@kanzhun.com
 * @date 2023/8/18 12:03
 */
public class SettingPanel {

    private Project project;
    private JPanel rootPanel;
    private JTextField applicationNameKeywordField;

    private JComboBox<String> applicationNameBox;
    private JButton reset;
    private JTextField serverUrlField;

    public SettingPanel(Project project) {
        this.project = project;
    }


    public JTextField getApplicationNameKeywordField() {
        return applicationNameKeywordField;
    }

    public JComboBox<String> getApplicationNameBox() {
        return applicationNameBox;
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public void setData() {
        serverUrlField.setText(ServerUrlSettingStorage.getServerUrl());
        ApplicationConfig applicationConfig = SettingStorage.getApplicationConfig(project);
        resetDate(applicationConfig.getApplicationNameKeyword());
        if (StringUtils.isNotBlank(applicationConfig.getSelectedApplicationName())) {
            applicationNameBox.setSelectedItem(applicationConfig.getSelectedApplicationName());
        }
    }

    public void getData() {
        ServerUrlSettingStorage.setServerUrl(serverUrlField.getText());
        ApplicationConfig applicationConfig = SettingStorage.getApplicationConfig(project);
        if (applicationNameBox.getSelectedItem() != null) {
            applicationConfig.setSelectedApplicationName((String) applicationNameBox.getSelectedItem());
        }
        applicationConfig.setApplicationNameKeyword(applicationNameKeywordField.getText());
    }

    public boolean isModifiable() {
        try {
            ApplicationConfig applicationConfig = SettingStorage.getApplicationConfig(project);
            checkArgument(Objects.equals(applicationNameBox.getSelectedItem(), applicationConfig.getSelectedApplicationName()));
            checkArgument(Objects.equals(applicationNameKeywordField.getText(), applicationConfig.getApplicationNameKeyword()));
            checkArgument(Objects.equals(serverUrlField.getText(), ServerUrlSettingStorage.getServerUrl()));
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    public void initComponent() {
        addServerFocusListener();
        addApplicationNameChangeListener();
        addRestListener();
    }

    private void addServerFocusListener() {
        serverUrlField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // do nothing
            }

            @Override
            public void focusLost(FocusEvent e) {
                String serverUrl = serverUrlField.getText();
                if (StringUtils.isEmpty(serverUrl)) {
                    return;
                }
                ServerUrlSettingStorage.setServerUrl(serverUrlField.getText());
                fillApplicationBox();
            }
        });
    }


    private void addApplicationNameChangeListener() {
        applicationNameKeywordField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                fillApplicationBox();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                fillApplicationBox();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                fillApplicationBox();
            }
        });
        fillApplicationBox();
    }

    private void addRestListener() {
        reset.addActionListener(e -> {
            ServerUrlSettingStorage.setServerUrl(DEFAULT_SERVER_URL);
            resetDate("");
        });
    }

    public void resetDate(String t) {
        applicationNameKeywordField.setText(t);
        fillApplicationBox();
    }


    private void fillApplicationBox() {
        List<String> applicationNameList = getApplicationList();
        if (isEmpty(applicationNameList)) {
            applicationNameBox.removeAllItems();
            applicationNameBox.addItem(NEED_SELECT_APPLICATION);
            applicationNameBox.setSelectedIndex(0);
            return;
        }
        applicationNameBox.removeAllItems();
        String applicationText = applicationNameKeywordField.getText().toLowerCase();
        if (isNotBlank(applicationText)) {
            List<String> filterApplicationList = applicationNameList.stream()
                    .filter(application -> application.toLowerCase().contains(applicationText))
                    .collect(toList());
            if (isEmpty(filterApplicationList)) {
                applicationNameBox.addItem(NEED_SELECT_APPLICATION);
                applicationNameBox.setSelectedIndex(0);
            } else {
                filterApplicationList.forEach(applicationNameBox::addItem);
            }
            return;
        }
        applicationNameBox.addItem(NEED_SELECT_APPLICATION);
        applicationNameBox.setSelectedIndex(0);
        applicationNameList.forEach(applicationNameBox::addItem);
    }

}

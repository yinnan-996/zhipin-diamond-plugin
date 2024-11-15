package com.zhipin.diamond.component;

import com.intellij.openapi.project.Project;
import com.zhipin.diamond.config.ApplicationConfig;
import com.zhipin.diamond.model.ApplicationInfo;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.zhipin.diamond.component.ServerUrlSettingStorage.DEFAULT_SERVER_URL;
import static com.zhipin.diamond.constants.DiamondConstants.EMPTY_APPLICATION_LIST_TXT;
import static com.zhipin.diamond.utils.ReloadUtil.filterApplicationInfoList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

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
    private JComboBox<Boolean> enableGroupBox;
    private JLabel groupNameLabel;
    private JTextField groupNameField;
    private JComboBox<Boolean> disableLocalCompileBox;


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
        resetDate(applicationConfig);
        if (StringUtils.isNotBlank(applicationConfig.getSelectedApplicationName())) {
            applicationNameBox.setSelectedItem(applicationConfig.getSelectedApplicationName());
        }
        applicationNameBox.setSelectedItem(applicationConfig.isEnableGroup());
    }

    public void getData() {
        ServerUrlSettingStorage.setServerUrl(serverUrlField.getText());
        ApplicationConfig applicationConfig = SettingStorage.getApplicationConfig(project);
        if (applicationNameBox.getSelectedItem() != null) {
            applicationConfig.setSelectedApplicationName((String) applicationNameBox.getSelectedItem());
        }
        applicationConfig.setApplicationNameKeyword(applicationNameKeywordField.getText());
        applicationConfig.setEnableGroup((Boolean) ofNullable(enableGroupBox.getSelectedItem()).orElse(false));
        applicationConfig.setDisableLocalCompile((Boolean) ofNullable(disableLocalCompileBox.getSelectedItem()).orElse(false));
        applicationConfig.setGroupName(groupNameField.getText());
    }

    public boolean isModifiable() {
        try {
            ApplicationConfig applicationConfig = SettingStorage.getApplicationConfig(project);
            checkArgument(Objects.equals(applicationNameBox.getSelectedItem(), applicationConfig.getSelectedApplicationName()));
            checkArgument(Objects.equals(applicationNameKeywordField.getText(), applicationConfig.getApplicationNameKeyword()));
            checkArgument(Objects.equals(enableGroupBox.getSelectedItem(), applicationConfig.isEnableGroup()));
            checkArgument(Objects.equals(disableLocalCompileBox.getSelectedItem(), applicationConfig.isDisableLocalCompile()));
            checkArgument(Objects.equals(groupNameField.getText(), applicationConfig.getGroupName()));
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    public void initComponent() {
        addServerFocusListener();
        addApplicationNameChangeListener();
        addRestListener();
        addGroupListener();
        addDisableLocalCompileListener();
        fillApplicationBox();
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
        applicationNameKeywordField.getDocument().addDocumentListener(getDocumentListener());
    }

    private void addRestListener() {
        reset.addActionListener(e -> {
            ServerUrlSettingStorage.setServerUrl(DEFAULT_SERVER_URL);
            resetDate(new ApplicationConfig());
        });
    }


    private void addGroupListener() {
        if (enableGroupBox.getItemCount() < 1) {
            enableGroupBox.addItem(Boolean.FALSE);
            enableGroupBox.addItem(Boolean.TRUE);
            enableGroupBox.setSelectedIndex(0);
        }
        enableGroupBox.addItemListener(e -> {
            if (Objects.equals(e.getItem(), false)) {
                groupNameField.setText("");
            }
            groupNameLabel.setEnabled(Objects.equals(enableGroupBox.getSelectedItem(), true));
            groupNameField.setEnabled(Objects.equals(enableGroupBox.getSelectedItem(), true));
            groupNameField.setEditable(Objects.equals(enableGroupBox.getSelectedItem(), true));
        });
        groupNameField.getDocument().addDocumentListener(getDocumentListener());
    }

    private void addDisableLocalCompileListener() {
        if (disableLocalCompileBox.getItemCount() < 1) {
            disableLocalCompileBox.addItem(Boolean.FALSE);
            disableLocalCompileBox.addItem(Boolean.TRUE);
            disableLocalCompileBox.setSelectedIndex(0);
        }
    }

    private @NotNull DocumentListener getDocumentListener() {
        return new DocumentListener() {
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
        };
    }

    public void resetDate(ApplicationConfig applicationConfig) {
        applicationNameKeywordField.setText(StringUtils.isNotBlank(applicationConfig.getApplicationNameKeyword())
                ? applicationConfig.getApplicationNameKeyword()
                : "");
        enableGroupBox.setSelectedItem(applicationConfig.isEnableGroup());
        disableLocalCompileBox.setSelectedItem(applicationConfig.isDisableLocalCompile());
        groupNameField.setText(StringUtils.isNotBlank(applicationConfig.getGroupName())
                ? applicationConfig.getGroupName()
                : "");
        fillApplicationBox();
    }


    private void fillApplicationBox() {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setApplicationNameKeyword(applicationNameKeywordField.getText().toLowerCase());
        applicationConfig.setEnableGroup(Objects.equals(Boolean.TRUE, enableGroupBox.getSelectedItem()));
        applicationConfig.setDisableLocalCompile(Objects.equals(Boolean.TRUE, disableLocalCompileBox.getSelectedItem()));
        applicationConfig.setGroupName(groupNameField.getText().toLowerCase());
        List<ApplicationInfo> filterApplicationList = filterApplicationInfoList(applicationConfig);
        if (isEmpty(filterApplicationList)) {
            applicationNameBox.removeAllItems();
            applicationNameBox.addItem(EMPTY_APPLICATION_LIST_TXT);
            applicationNameBox.setSelectedIndex(0);
            return;
        }
        applicationNameBox.removeAllItems();
        if (isEmpty(filterApplicationList)) {
            applicationNameBox.addItem(EMPTY_APPLICATION_LIST_TXT);
            applicationNameBox.setSelectedIndex(0);
        } else {
            filterApplicationList.stream().map(ApplicationInfo::getApplicationName).forEach(applicationNameBox::addItem);
        }
        applicationNameBox.addItem(EMPTY_APPLICATION_LIST_TXT);
        applicationNameBox.setSelectedIndex(0);
    }

}

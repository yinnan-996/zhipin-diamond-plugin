package com.zhipin.diamond.model;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yinnan@kanzhun.com
 * @date 2023/8/23 11:47
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotFixAction {
    private String applicationName;
    private VirtualFile virtualFile;
    private Project project;
}

package com.zhipin.diamond.config;

import lombok.Data;


/**
 * @author yinnan@kanzhun.com
 * @date 2023/8/18 12:04
 */
@Data
public class ApplicationConfig {
    private String applicationNameKeyword;
    private String selectedApplicationName;
    private boolean enableGroup;
    private String groupName;
    private boolean disableLocalCompile;

    public String calculateGroupName() {
        return enableGroup ? groupName : "";
    }
}

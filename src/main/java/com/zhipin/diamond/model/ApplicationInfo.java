package com.zhipin.diamond.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yinnan@kanzhun.com
 * @date 2024/11/7 15:04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationInfo {

    public String applicationName;

    private String group;

}

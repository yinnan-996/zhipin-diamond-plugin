package com.zhipin.diamond.config;

import static com.zhipin.diamond.utils.ReloadUtil.joinKeywords;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.List;

import com.zhipin.diamond.model.JvmProcess;

import lombok.Data;


/**
 * @author yinnan@kanzhun.com
 * @date 2023/8/18 12:04
 */
@Data
public class ApplicationConfig {
    private String applicationNameKeyword;
    private String selectedApplicationName;
}

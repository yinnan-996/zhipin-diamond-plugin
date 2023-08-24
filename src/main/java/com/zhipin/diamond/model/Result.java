package com.zhipin.diamond.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yinnan@kanzhun.com
 * @date 2023/8/18 12:09
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    public static final int SUCCESS_CODE = 0;

    private int code;
    private String msg;
    private T data;
}

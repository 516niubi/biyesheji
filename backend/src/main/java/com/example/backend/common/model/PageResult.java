package com.example.backend.common.model;

import lombok.Data;

/**
 * @Author caiya
 * @Description 通用分页返回结果
 * @Version 1.0
 */
@Data
public class PageResult<T> {
    private T records;
    private long current;
    private long size;
    private long total;
}

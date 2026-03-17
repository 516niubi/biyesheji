package com.example.backend.entity.vo.article;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 资讯表 视图对象
 * </p>
 *
 * @author 
 * @since 2025-01-20
 */
@Data
@ApiModel(value = "ArticleVO对象", description = "资讯表 视图对象")
public class ArticleVO {

    @ApiModelProperty("ID")
    private Integer id;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty(value = "简介")
    private String intro;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("浏览次数")
    private Integer viewCount;

    @ApiModelProperty("封面图片URL")
    private String coverUrl;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
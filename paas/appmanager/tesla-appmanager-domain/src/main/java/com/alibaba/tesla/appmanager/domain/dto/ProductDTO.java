package com.alibaba.tesla.appmanager.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 产品 DTO
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    /**
     * ID
     */
    private Long id;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 最后修改时间
     */
    private Date gmtModified;

    /**
     * 产品 ID
     */
    private String productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 基线 Git 地址
     */
    private String baselineGitAddress;

    /**
     * 基线 Git User
     */
    private String baselineGitUser;

    /**
     * 基线 Git Token
     */
    private String baselineGitToken;
}
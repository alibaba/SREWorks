package com.alibaba.tesla.appmanager.domain.container;

import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * Biz App Container
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BizAppContainer {

    /**
     * Namespace ID
     */
    private String namespaceId;

    /**
     * Stage ID
     */
    private String stageId;

    /**
     * 根据 X-Biz-App Header 获取解析后的 BizAppContainer
     * @param headerBizApp Header
     * @return BizAppContainer 容器
     */
    public static BizAppContainer valueOf(String headerBizApp) {
        if (StringUtils.isEmpty(headerBizApp)) {
            return BizAppContainer.builder().namespaceId("").stageId("").build();
        }
        String[] array = headerBizApp.split(",", 3);
        if (array.length <= 1) {
            if ("sreworks".equals(System.getenv("K8S_NAMESPACE"))) {
                return BizAppContainer.builder().namespaceId("sreworks").stageId("dev").build();
            } else {
                return BizAppContainer.builder().namespaceId("default").stageId("pre").build();
            }
        } else if (array.length < 3) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "invalid X-Biz-App header");
        }
        return BizAppContainer.builder()
                .namespaceId(array[1])
                .stageId(array[2])
                .build();
    }
}

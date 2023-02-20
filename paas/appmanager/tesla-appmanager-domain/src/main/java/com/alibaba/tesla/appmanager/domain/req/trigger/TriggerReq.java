package com.alibaba.tesla.appmanager.domain.req.trigger;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 触发器请求
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerReq {

    /**
     * 触发构建的 Token
     */
    private String token;

    /**
     * 触发构建的组件列表
     */
    private List<TriggerComponent> builds;

    /**
     * Repo 信息
     */
    private Repository repository;

    /**
     * 触发构建的组件
     */
    public static class TriggerComponent {

        /**
         * 组件类型
         */
        private String componentType;

        /**
         * 组件名称
         */
        private String componentName;

        /**
         * 配置选项
         */
        private JSONObject options;
    }

    /**
     * Repo 信息
     */
    public static class Repository {

        /**
         * 仓库地址
         */
        private String address;

        /**
         * Commit ID
         */
        private String commit;

        /**
         * 分支
         */
        private String branch;

        /**
         * 提交用户
         */
        private String user;

        /**
         * 评论内容
         */
        private String comment;
    }
}

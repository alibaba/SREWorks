package com.alibaba.sreworks.domain.DO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.common.util.YamlUtil;
import com.alibaba.sreworks.domain.DTO.AppComponentDetail;
import com.alibaba.sreworks.domain.DTO.AppComponentRepoDetail;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * @author jinghua.yjh
 */
@Slf4j
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppComponent {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private Long gmtCreate;

    @Column
    private Long gmtModified;

    @Column
    private String creator;

    @Column
    private String lastModifier;

    @Column
    private Long appId;

    @Column
    private String name;

    @Column
    private Long exId;

    @Column(columnDefinition = "text")
    private String repoDetail;

    @Column(columnDefinition = "text")
    private String detail;

    @Column(columnDefinition = "text")
    private String description;

    public AppComponentRepoDetail repoDetail() {
        AppComponentRepoDetail repoDetail = JSONObject.parseObject(this.repoDetail, AppComponentRepoDetail.class);
        return repoDetail == null ? new AppComponentRepoDetail() : repoDetail;
    }

    public AppComponentDetail detail() {
        AppComponentDetail detail = JSONObject.parseObject(this.detail, AppComponentDetail.class);
        return detail == null ? new AppComponentDetail() : detail;
    }

    public JSONObject toJsonObject() {
        return JSONObject.parseObject(JSONObject.toJSONString(this));
    }

}

package com.alibaba.sreworks.job.master.domain.DO;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.job.utils.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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
public class SreworksStreamJob {

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
    private String operator;

    @Column
    private String appId;

    @Column
    private String name;

    @Column
    private String alias;

    @Column
    private String status;

    @Column
    private Long executionPoolId;

    @Column(columnDefinition = "longtext")
    private String tags;

    @Column(columnDefinition = "longtext")
    private String description;

    @Column(columnDefinition = "longtext")
    private String options;

    @Column
    private String jobType;

}

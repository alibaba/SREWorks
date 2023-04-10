package com.alibaba.sreworks.job.master.domain.DO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

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
public class SreworksStreamJobSink {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private Long streamJobId;

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

    @Column(columnDefinition = "longtext")
    private String options;

}

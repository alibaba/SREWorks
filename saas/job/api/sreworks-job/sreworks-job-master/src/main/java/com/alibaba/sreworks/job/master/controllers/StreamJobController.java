package com.alibaba.sreworks.job.master.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.job.master.domain.DO.SreworksJob;
import com.alibaba.sreworks.job.master.domain.DTO.ElasticJobInstanceDTO;
import com.alibaba.sreworks.job.master.domain.DTO.FlinkConnectorDTO;
import com.alibaba.sreworks.job.master.domain.DTO.SreworksJobDTO;
import com.alibaba.sreworks.job.master.domain.DTO.SreworksStreamJobDTO;
import com.alibaba.sreworks.job.master.domain.repository.SreworksJobRepository;
import com.alibaba.sreworks.job.master.jobscene.JobSceneService;
import com.alibaba.sreworks.job.master.jobschedule.JobScheduleService;
import com.alibaba.sreworks.job.master.jobtrigger.JobTriggerService;
import com.alibaba.sreworks.job.master.params.*;
import com.alibaba.sreworks.job.master.services.JobService;
import com.alibaba.sreworks.job.master.services.StreamJobService;
import com.alibaba.sreworks.job.master.services.VvpService;
import com.alibaba.sreworks.job.utils.JsonUtil;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import com.alibaba.tesla.web.controller.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.alibaba.sreworks.job.utils.PageUtil.pageable;

/**
 * @author jinghua.yjh
 */
@Slf4j
@RestController
@RequestMapping("/stream-job")
public class StreamJobController extends BaseController {

    @Autowired
    StreamJobService streamJobService;

    @Autowired
    VvpService vvpService;

    @RequestMapping(value = "create", method = RequestMethod.POST)
    public TeslaBaseResult create(@RequestBody StreamJobCreateParam param) throws Exception {
        param.setCreator(getUserEmployeeId());
        param.setOperator(getUserEmployeeId());
        if (param.getAppId() == null) {
            param.setAppId(getAppId());
        }
        if (param.getTags() == null) {
            param.setTags(new JSONArray());
        }
        if (param.getDescription() == null) {
            param.setDescription("");
        }
        if (param.getOptions() == null) {
            param.setOptions(new JSONObject());
        }
        return buildSucceedResult(streamJobService.create(param));
    }

    @RequestMapping(value = "gets", method = RequestMethod.GET)
    public TeslaBaseResult gets(Integer page, Integer pageSize) throws Exception {
        Page<SreworksStreamJobDTO> jobList = streamJobService.gets(pageable(page, pageSize));
        return buildSucceedResult(JsonUtil.map(
        "page", jobList.getNumber(),
            "pageSize", jobList.getSize(),
            "total", jobList.getTotalElements(),
                "items", jobList.getContent()
        ));

    }

    @RequestMapping(value = "connectors", method = RequestMethod.GET)
    public TeslaBaseResult getConnectors(StreamJobListConnectorParam param) throws Exception {
        List<FlinkConnectorDTO> connectors = vvpService.listConnector(param.getSource(), param.getSink(), param.getType());
        return buildSucceedResult(connectors);
    }

    @RequestMapping(value = "connector/properties", method = RequestMethod.GET)
    public TeslaBaseResult getConnectorProperties(String type, String columns) throws Exception {
        if(!StringUtils.isEmpty(type)){
            List<FlinkConnectorDTO> connectors = vvpService.listConnector(null, null, type);
            if (connectors.size() > 0) {

                // 初始化包含 # 和不包含 # 的 JSONArray 数组
                JSONArray propertiesWithSharp = new JSONArray();
                JSONArray properties = new JSONArray();

                for (int i = 0; i < connectors.get(0).getProperties().size(); i++) {
                    JSONObject obj = connectors.get(0).getProperties().getJSONObject(i);
                    if (obj.getString("key").contains("#")) {
                        propertiesWithSharp.add(obj);
                    } else {
                        obj.put("label", obj.getString("key"));
                        obj.put("value", obj.getString("key"));
                        properties.add(obj);
                    }
                }

                // 将含有#的配置按照columns拓展开
                if(propertiesWithSharp.size() > 0 && !StringUtils.isEmpty(columns)) {
                    for (String column : columns.split(",")) {
                        for (int i = 0; i < propertiesWithSharp.size(); i++) {
                            JSONObject property = propertiesWithSharp.getJSONObject(i);
                            JSONObject obj = property.clone();
                            String key = obj.getString("key").replace("#", column);
                            obj.put("key", key);
                            obj.put("label", key);
                            obj.put("value", key);
                            properties.add(obj);
                        }
                    }
                }

                return buildSucceedResult(JsonUtil.map("options", properties));
            }
        }
        return buildSucceedResult(null);
    }


}

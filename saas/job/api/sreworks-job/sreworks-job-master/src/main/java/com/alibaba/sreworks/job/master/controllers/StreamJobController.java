package com.alibaba.sreworks.job.master.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.job.master.domain.DTO.*;
import com.alibaba.sreworks.job.master.params.*;
import com.alibaba.sreworks.job.master.services.StreamJobService;
import com.alibaba.sreworks.job.master.services.VvpService;
import com.alibaba.sreworks.job.utils.JsonUtil;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import com.alibaba.tesla.web.controller.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @RequestMapping(value = "{id}/source", method = RequestMethod.POST)
    public TeslaBaseResult addSource(@PathVariable("id") Long streamJobId, @RequestBody StreamJobSourceCreateParam param) throws Exception {
        SreworksStreamJobDTO job = streamJobService.get(streamJobId);
        if(job == null){
            return buildClientErrorResult("streamJob not found");
        }
        param.setCreator(getUserEmployeeId());
        param.setOperator(getUserEmployeeId());
        return buildSucceedResult(streamJobService.addSource(streamJobId, job.getAppId(), param));
    }

    @RequestMapping(value = "{id}/block/{blockId}", method = RequestMethod.DELETE)
    public TeslaBaseResult deleteBlock(@PathVariable("id") Long streamJobId, @PathVariable("blockId") Long streamJobBlockId) throws Exception {
        SreworksStreamJobDTO job = streamJobService.get(streamJobId);
        if(job == null){
            return buildClientErrorResult("streamJob not found");
        }
        streamJobService.deleteBlock(streamJobBlockId);
        return buildSucceedResult(JsonUtil.map(
            "streamJobId", streamJobId,
                "blockId", streamJobBlockId
        ));
    }

    @RequestMapping(value = "{id}/sink", method = RequestMethod.POST)
    public TeslaBaseResult addSink(@PathVariable("id") Long streamJobId, @RequestBody StreamJobSinkCreateParam param) throws Exception {
        SreworksStreamJobDTO job = streamJobService.get(streamJobId);
        if(job == null){
            return buildClientErrorResult("streamJob not found");
        }
        param.setCreator(getUserEmployeeId());
        param.setOperator(getUserEmployeeId());
        return buildSucceedResult(streamJobService.addSink(streamJobId, job.getAppId(), param));
    }

    @RequestMapping(value = "{id}/python", method = RequestMethod.POST)
    public TeslaBaseResult addPython(@PathVariable("id") Long streamJobId, @RequestBody StreamJobPythonCreateParam param) throws Exception {
        SreworksStreamJobDTO job = streamJobService.get(streamJobId);
        if(job == null){
            return buildClientErrorResult("streamJob not found");
        }
        param.setCreator(getUserEmployeeId());
        param.setOperator(getUserEmployeeId());
        return buildSucceedResult(streamJobService.addPython(streamJobId, job.getAppId(), param));
    }

    @RequestMapping(value = "{id}/blocks", method = RequestMethod.GET)
    public TeslaBaseResult getBlocks(@PathVariable("id") Long streamJobId) throws Exception {
        SreworksStreamJobDTO job = streamJobService.get(streamJobId);
        if(job == null){
            return buildClientErrorResult("streamJob not found");
        }
        return buildSucceedResult(streamJobService.listBlockByStreamJobId(streamJobId));
    }

    @RequestMapping(value = "{id}/preview", method = RequestMethod.GET)
    public TeslaBaseResult getPreview(@PathVariable("id") Long streamJobId) throws Exception {
        SreworksStreamJobDTO job = streamJobService.get(streamJobId);
        if(job == null){
            return buildClientErrorResult("streamJob not found");
        }
        return buildSucceedResult(JsonUtil.map(
        "content", streamJobService.generateScript(streamJobId)
        ));
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

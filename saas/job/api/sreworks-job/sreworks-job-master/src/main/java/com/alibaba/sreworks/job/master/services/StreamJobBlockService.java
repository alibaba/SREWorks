package com.alibaba.sreworks.job.master.services;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.job.master.domain.DO.SreworksStreamJob;
import com.alibaba.sreworks.job.master.domain.DO.SreworksStreamJobBlock;
import com.alibaba.sreworks.job.master.domain.DO.SreworksStreamJobRuntime;
import com.alibaba.sreworks.job.master.domain.DTO.*;
import com.alibaba.sreworks.job.master.domain.repository.SreworksStreamJobBlockRepository;
import com.alibaba.sreworks.job.master.domain.repository.SreworksStreamJobRepository;
import com.alibaba.sreworks.job.master.domain.repository.SreworksStreamJobRuntimeRepository;
import com.alibaba.sreworks.job.master.params.*;
import com.alibaba.sreworks.job.utils.JsonUtil;
import com.alibaba.sreworks.job.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StreamJobBlockService {

    @Autowired
    SreworksStreamJobRepository streamJobRepository;

    @Autowired
    SreworksStreamJobBlockRepository sreworksStreamJobBlockRepository;

    @Autowired
    SreworksStreamJobRuntimeRepository sreworksStreamJobRuntimeRepository;

    @Autowired
    VvpService vvpService;

    public int deleteByStreamJobId(Long streamJobId) {
        return sreworksStreamJobBlockRepository.deleteAllByStreamJobId(streamJobId);
    }
    private SreworksStreamJobBlock convertDO(SreworksStreamJobSourceDTO source){
        return SreworksStreamJobBlock.builder()
                .id(source.getId())
                .gmtCreate(source.getGmtCreate())
                .gmtModified(source.getGmtModified())
                .streamJobId(source.getStreamJobId())
                .appId(source.getAppId())
                .name(source.getName())
                .blockType("source")
                .data(JsonUtil.map(
                "options", source.getOptions(),
                      "columns", source.getColumns(),
                        "sourceType", source.getSourceType()
                ).toJSONString())
                .build();
    }

    private SreworksStreamJobBlock convertDO(SreworksStreamJobPythonDTO python){
        return SreworksStreamJobBlock.builder()
                .id(python.getId())
                .gmtCreate(python.getGmtCreate())
                .gmtModified(python.getGmtModified())
                .streamJobId(python.getStreamJobId())
                .appId(python.getAppId())
                .name(python.getName())
                .blockType("python")
                .data(JsonUtil.map(
                        "content", python.getContent()
                ).toJSONString())
                .build();
    }

    private SreworksStreamJobBlock convertDO(SreworksStreamJobSinkDTO sink){
        return SreworksStreamJobBlock.builder()
                .id(sink.getId())
                .gmtCreate(sink.getGmtCreate())
                .gmtModified(sink.getGmtModified())
                .streamJobId(sink.getStreamJobId())
                .appId(sink.getAppId())
                .name(sink.getName())
                .blockType("sink")
                .data(JsonUtil.map(
                        "options", sink.getOptions(),
                        "columns", sink.getColumns(),
                        "sinkType", sink.getSinkType()
                ).toJSONString())
                .build();
    }

    public void delete(Long streamJobBlockId) {
        sreworksStreamJobBlockRepository.deleteById(streamJobBlockId);
    }

    public SreworksStreamJobSourceDTO addSource(Long streamJobId, String appId, StreamJobSourceCreateParam param) throws Exception {
        SreworksStreamJobBlock jobSource = param.init(streamJobId, appId);
        jobSource = sreworksStreamJobBlockRepository.saveAndFlush(jobSource);
        return new SreworksStreamJobSourceDTO(jobSource);
    }

    public SreworksStreamJobSourceDTO getSource(Long blockId){
        SreworksStreamJobBlock block = sreworksStreamJobBlockRepository.findFirstById(blockId);
        return new SreworksStreamJobSourceDTO(block);
    }

    public SreworksStreamJobPythonDTO getPython(Long blockId){
        SreworksStreamJobBlock block = sreworksStreamJobBlockRepository.findFirstById(blockId);
        return new SreworksStreamJobPythonDTO(block);
    }

    public SreworksStreamJobSinkDTO getSink(Long blockId){
        SreworksStreamJobBlock block = sreworksStreamJobBlockRepository.findFirstById(blockId);
        return new SreworksStreamJobSinkDTO(block);
    }

    public SreworksStreamJobSourceDTO updateSource(SreworksStreamJobSourceDTO source, StreamJobSourceCreateParam param){
        source.setColumns(param.getColumns());
        source.setOptions(param.getOptions());
        SreworksStreamJobBlock block = convertDO(source);
        block = sreworksStreamJobBlockRepository.saveAndFlush(block);
        return new SreworksStreamJobSourceDTO(block);
    }

    public SreworksStreamJobPythonDTO updatePython(SreworksStreamJobPythonDTO python, StreamJobPythonCreateParam param){
        python.setContent(param.getScriptContent());
        SreworksStreamJobBlock block = convertDO(python);
        block = sreworksStreamJobBlockRepository.saveAndFlush(block);
        return new SreworksStreamJobPythonDTO(block);
    }

    public SreworksStreamJobPythonDTO updateSink(SreworksStreamJobSinkDTO sink, StreamJobSinkCreateParam param){
        sink.setColumns(param.getColumns());
        sink.setOptions(param.getOptions());
        SreworksStreamJobBlock block = convertDO(sink);
        block = sreworksStreamJobBlockRepository.saveAndFlush(block);
        return new SreworksStreamJobPythonDTO(block);
    }

    public SreworksStreamJobSinkDTO addSink(Long streamJobId, String appId, StreamJobSinkCreateParam param) throws Exception {
        SreworksStreamJobBlock jobSink = param.init(streamJobId, appId);
        jobSink = sreworksStreamJobBlockRepository.saveAndFlush(jobSink);
        return new SreworksStreamJobSinkDTO(jobSink);
    }

    public SreworksStreamJobPythonDTO addPython(Long streamJobId, String appId, StreamJobPythonCreateParam param) throws Exception {
        SreworksStreamJobBlock jobPython = param.init(streamJobId, appId);
        jobPython = sreworksStreamJobBlockRepository.saveAndFlush(jobPython);
        return new SreworksStreamJobPythonDTO(jobPython);
    }

    public List<SreworksStreamJobBlockDTO> listByStreamJobId(Long streamJobId) throws Exception {
        List<SreworksStreamJobBlock> sourceList = sreworksStreamJobBlockRepository.findAllByStreamJobId(streamJobId);
        List<String> blockTypeOrder = Arrays.asList("source", "python", "sink");
        Comparator<SreworksStreamJobBlock> blockTypeComparator = Comparator.comparingInt(block -> blockTypeOrder.indexOf(block.getBlockType()));
        sourceList.sort(blockTypeComparator);
        return sourceList.stream().map(SreworksStreamJobBlockDTO::new)
        .collect(Collectors.toList());
    }


}

package com.alibaba.sreworks.job.master.services;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.job.master.common.JobTriggerType;
import com.alibaba.sreworks.job.master.domain.DO.*;
import com.alibaba.sreworks.job.master.domain.DTO.*;
import com.alibaba.sreworks.job.master.domain.repository.*;
import com.alibaba.sreworks.job.master.jobscene.JobSceneService;
import com.alibaba.sreworks.job.master.jobschedule.JobScheduleService;
import com.alibaba.sreworks.job.master.jobtrigger.JobTriggerService;
import com.alibaba.sreworks.job.master.params.*;
import com.alibaba.sreworks.job.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StreamJobService {

    @Autowired
    SreworksStreamJobRepository streamJobRepository;

    @Autowired
    SreworksStreamJobBlockRepository sreworksStreamJobBlockRepository;

    public Page<SreworksStreamJobDTO> gets(Pageable pageable) throws Exception {
        Page<SreworksStreamJob> jobs = streamJobRepository.findAll(pageable);

        return jobs.map(streamJob -> {
            SreworksStreamJobDTO jobDTO = new SreworksStreamJobDTO(streamJob);
            return jobDTO;
        });
    }

    public SreworksStreamJobDTO get(Long streamJobId) throws Exception {
        SreworksStreamJob job = streamJobRepository.findFirstById(streamJobId);
        return new SreworksStreamJobDTO(job);
    }

    public SreworksStreamJobDTO create(StreamJobCreateParam param) throws Exception {
        SreworksStreamJob job = param.job();
        job = streamJobRepository.saveAndFlush(job);
        return new SreworksStreamJobDTO(job);
    }

    public void deleteBlock(Long streamJobBlockId) {
        sreworksStreamJobBlockRepository.deleteById(streamJobBlockId);
    }

    public String generateScript(Long streamJobId) throws Exception {
        List<SreworksStreamJobBlockDTO>  blocks = this.listBlockByStreamJobId(streamJobId);
        StringBuilder scriptContent = new StringBuilder(
                "from pyflink.common.typeinfo import Types\n"
              + "from pyflink.datastream import StreamExecutionEnvironment\n"
              + "from pyflink.table import StreamTableEnvironment\n"
              + "from pyflink.common import Row\n"
              + "\n"
              + "env = StreamExecutionEnvironment.get_execution_environment()\n"
              + "t_env = StreamTableEnvironment.create(stream_execution_environment=env)\n"
              + "\n"
        );
        for (SreworksStreamJobBlockDTO block : blocks){
            if(StringUtils.equals(block.getBlockType(), "source")){
                /**
                 * t_env.execute_sql("""
                 *      CREATE TABLE my_source (
                 *          a INT,
                 *          b STRING
                 *      ) WITH (
                 *         'connector' = 'datagen',
                 *         'rows-per-second' = '4',
                 *         'fields.a.min' = '1',
                 *         'fields.a.max' = '1000'
                 *      )
                 *  """)
                 *  ds = t_env.to_append_stream(
                 *    t_env.from_path('my_source'),
                 *    Types.ROW([Types.INT(), Types.STRING()])
                 *  )
                 *
                 */
                scriptContent.append("# source " + block.getName() + "\n");
                scriptContent.append("t_env.execute_sql(\"\"\"\n");
                scriptContent.append("    CREATE TABLE " + block.getName() +" (\n");
                JSONArray columns = block.getData().getJSONArray("columns");
                if(columns != null){
                    for (int i = 0; i < columns.size(); i++) {
                        JSONObject column = columns.getJSONObject(i);
                        String columnName = column.getString("columnName");
                        String columnType = column.getString("columnType");
                        if (columnType != null && columnName != null){
                            scriptContent.append("      " + columnName + " " + columnType +"\n");
                        }
                    }
                }
                scriptContent.append("    ) WITH (\n");
                JSONArray options = block.getData().getJSONArray("options");
                if(options != null){
                    for (int i = 0; i < options.size(); i++) {
                        JSONObject option = options.getJSONObject(i);
                        String key = option.getString("key");
                        String value = option.getString("value");
                        if (key != null && value != null){
                            scriptContent.append("      '" + key + "' = '" + value +"'\n");
                        }
                    }
                }
                scriptContent.append("    )\n");
                scriptContent.append("\"\"\")\n");
                scriptContent.append("ds = t_env.to_append_stream(\n");
                scriptContent.append("    t_env.from_path('" + block.getName() + "'),\n");
                scriptContent.append("    Types.ROW([");
                if(columns != null){
                    for (int i = 0; i < columns.size(); i++) {
                        JSONObject column = columns.getJSONObject(i);
                        String columnType = column.getString("columnType");
                        if (columnType != null){
                            scriptContent.append("Types."+columnType+"(), ");
                        }
                    }
                }
                scriptContent.append("])\n");
                scriptContent.append(")\n");
            }else if(StringUtils.equals(block.getBlockType(), "python")){
                scriptContent.append(block.getData());
            }else if(StringUtils.equals(block.getBlockType(), "sink")){
                scriptContent.append("sink\n");
            }
        }

        return scriptContent.toString();
    }

    public SreworksStreamJobSourceDTO addSource(Long streamJobId, String appId, StreamJobSourceCreateParam param) throws Exception {
        SreworksStreamJobBlock jobSource = param.init(streamJobId, appId);
        jobSource = sreworksStreamJobBlockRepository.saveAndFlush(jobSource);
        return new SreworksStreamJobSourceDTO(jobSource);
    }

    public SreworksStreamJobSinkDTO addSink(Long streamJobId, String appId, StreamJobSinkCreateParam param) throws Exception {
        SreworksStreamJobBlock jobSink = param.init(streamJobId, appId);
        jobSink = sreworksStreamJobBlockRepository.saveAndFlush(jobSink);
        return new SreworksStreamJobSinkDTO(jobSink);
    }

    public SreworksStreamJobSinkDTO addPython(Long streamJobId, String appId, StreamJobPythonCreateParam param) throws Exception {
        SreworksStreamJobBlock jobPython = param.init(streamJobId, appId);
        jobPython = sreworksStreamJobBlockRepository.saveAndFlush(jobPython);
        return new SreworksStreamJobSinkDTO(jobPython);
    }

    public List<SreworksStreamJobBlockDTO> listBlockByStreamJobId(Long streamJobId) throws Exception {
        List<SreworksStreamJobBlock> sourceList = sreworksStreamJobBlockRepository.findAllByStreamJobId(streamJobId);
        List<String> blockTypeOrder = Arrays.asList("source", "python", "sink");
        Comparator<SreworksStreamJobBlock> blockTypeComparator = Comparator.comparingInt(block -> blockTypeOrder.indexOf(block.getBlockType()));
        sourceList.sort(blockTypeComparator);
        return sourceList.stream().map(SreworksStreamJobBlockDTO::new)
        .collect(Collectors.toList());
    }

    public void modify(Long id, JobModifyParam param) throws Exception {

    }


}

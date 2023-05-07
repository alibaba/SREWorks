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
import com.alibaba.sreworks.job.utils.JsonUtil;
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

    @Autowired
    SreworksStreamJobRuntimeRepository sreworksStreamJobRuntimeRepository;

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

    public String pythonTemplate(){
        String templateContent = StringUtil.readResourceFile("python-default.py");
        return templateContent;
    }

    public SreworksStreamJobDTO create(StreamJobCreateParam param) throws Exception {
        SreworksStreamJob job = param.job();
        String templateContent = StringUtil.readResourceFile("pyflink-template.py");
        JSONObject options = JsonUtil.map(
         "template", templateContent,
                "settings", JsonUtil.map(
                "resources", JsonUtil.map(
                        "jobmanager", JsonUtil.map(
                               "cpu", 1,
                                    "memory","4G"
                                ),
                                "taskmanager", JsonUtil.map(
                                "cpu", 1,
                                    "memory","4G"
                                )
                        ),
                        "parallelism", 1,
                        "numberOfTaskManagers", 1
                )
        );
        job.setOptions(options.toJSONString());
        job = streamJobRepository.saveAndFlush(job);
        return new SreworksStreamJobDTO(job);
    }

    private SreworksStreamJob convertDO(SreworksStreamJobDTO jobDTO){
        return SreworksStreamJob.builder()
                .id(jobDTO.getId())
                .gmtCreate(System.currentTimeMillis())
                .gmtModified(System.currentTimeMillis())
                .creator(jobDTO.getCreator())
                .operator(jobDTO.getOperator())
                .appId(jobDTO.getAppId())
                .name(jobDTO.getName())
                .alias(jobDTO.getAlias())
                .tags(jobDTO.getTags().toJSONString())
                .description(jobDTO.getDescription())
                .options(jobDTO.getOptions().toJSONString())
                .jobType(jobDTO.getJobType())
                .build();
    }

    public SreworksStreamJobDTO updateSettings(SreworksStreamJobDTO job, JSONObject settings) throws Exception {
        JSONObject options = job.getOptions();
        options.put("settings", settings);
        job.setOptions(options);

        SreworksStreamJob updateJob = streamJobRepository.saveAndFlush(convertDO(job));
        return new SreworksStreamJobDTO(updateJob);
    }

    public SreworksStreamJobDTO updateTemplate(SreworksStreamJobDTO job, String templateContent){
        JSONObject options = job.getOptions();
        options.put("template", templateContent);
        job.setOptions(options);

        log.info("options {}", options.toJSONString());

        SreworksStreamJob updateJob = streamJobRepository.saveAndFlush(convertDO(job));
        return new SreworksStreamJobDTO(updateJob);
    }

    public void deleteBlock(Long streamJobBlockId) {
        sreworksStreamJobBlockRepository.deleteById(streamJobBlockId);
    }

    private JSONArray generateTable(StringBuilder scriptContent, SreworksStreamJobBlockDTO block){
        
            /**
             *   t_env.execute_sql("""
             *    CREATE TABLE my_sink (
             *      a INT,
             *      b STRING
             *    ) WITH (
             *       'connector' = 'jdbc',
             *       'lookup.cache.max-rows' = '1000',
             *       'lookup.cache.ttl' = '600s',
             *       'password' = '***',
             *       'table-name' = 'datagen',
             *       'url' = 'jdbc:mysql://**:3306/test_db?useUnicode=true&characterEncoding=utf-8&useSSL=false',
             *       'username' = 'root'
             *     );
             *   """)
             * 
             *
             */
            
        scriptContent.append("t_env.execute_sql(\"\"\"\n");
        scriptContent.append("    CREATE TABLE " + block.getName() +" (\n");
        JSONArray columns = block.getData().getJSONArray("columns");
        if(columns != null){
            for (int i = 0; i < columns.size(); i++) {
                JSONObject column = columns.getJSONObject(i);
                String columnName = column.getString("columnName");
                String columnType = column.getString("columnType");
                if (columnType != null && columnName != null){
                    scriptContent.append("      " + columnName + " " + columnType);
                    if (i == columns.size() - 1){
                        scriptContent.append("\n");
                    } else {
                        scriptContent.append(",\n");
                    }
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
                    scriptContent.append("      '" + key + "' = '" + value +"'");
                    if (i == options.size() - 1 ){
                        scriptContent.append("\n");
                    }else{
                        scriptContent.append(",\n");
                    }
                }
            }
        }
        scriptContent.append("    )\n");
        scriptContent.append("\"\"\")\n");
        return columns;
    }

    public String getScriptName(Long streamJobId) {
        return "streamJob-" + streamJobId.toString() + ".py";
    }

    public String getDeploymentName(Long streamJobId) {
        return "streamJob-" + streamJobId.toString();
    }

    public String generateScript(Long streamJobId, String templateContent) throws Exception {
        List<SreworksStreamJobBlockDTO>  blocks = this.listBlockByStreamJobId(streamJobId);
        StringBuilder sourceBlocks = new StringBuilder();
        StringBuilder sinkBlocks = new StringBuilder();
        StringBuilder pythonBlocks = new StringBuilder();
        for (SreworksStreamJobBlockDTO block : blocks){
            if(StringUtils.equals(block.getBlockType(), "source") || StringUtils.equals(block.getBlockType(), "sink")){

                sourceBlocks.append("\n# " + block.getBlockType() + " " + block.getName() + "\n");
                generateTable(sourceBlocks, block);

            }else if(StringUtils.equals(block.getBlockType(), "python")) {
                pythonBlocks.append("\n# python script " + block.getName() + "\n");
                pythonBlocks.append(block.getData().getString("content"));
                pythonBlocks.append("\n");
            }
        }

        Map<String, StringBuilder> blockTypes = new HashMap<>() {{
            put("# SOURCE_BLOCK #", sourceBlocks);
            put("# SINK_BLOCK #", sinkBlocks);
            put("# EXEC_BLOCK #", pythonBlocks);
        }};

        for (Map.Entry<String, StringBuilder> entry : blockTypes.entrySet()) {
            String blockType = entry.getKey();
            StringBuilder subBlocks = entry.getValue();
            templateContent = templateContent.replace(
                    blockType,
                    StringUtil.intendLines(
                            subBlocks,
                            StringUtil.getMatchLine(templateContent, blockType).replace(blockType, "")
                    ).toString()
            );
        }

        return templateContent;
    }

    public JSONObject generateDeployment(StreamJobDeploymentParam param) throws Exception {
        StringBuilder mainArgs = new StringBuilder();
        String storePath = "s3://vvp/artifacts/namespaces/default/";
        JSONObject flinkConfiguration = new JSONObject();
        JSONObject artifact = new JSONObject();
        JSONObject spec = new JSONObject();
        JSONArray additionalDependencies = new JSONArray();

        if (param.getAdditionalDependencies() != null){
            for (int i = 0; i < param.getAdditionalDependencies().size(); i++) {
                JSONObject item = param.getAdditionalDependencies().getJSONObject(i);
                additionalDependencies.add(storePath + item.getString("filename"));
            }
        }

        spec.put("flinkConfiguration", flinkConfiguration);
        spec.put("artifact", artifact);
        if(param.getResources() != null){
            spec.put("resources", param.getResources());
        }
        JSONObject deployment = JsonUtil.map(
      "metadata", JsonUtil.map(
                 "name", param.getName()
              ),
             "spec",  JsonUtil.map(
             "template", JsonUtil.map(
                    "spec", spec
                )
            )
        );
        artifact.put("kind", "JAR");

        // Flink 运行环境
        artifact.put("flinkVersion", param.getFlinkVersion());
        if(param.getFlinkImage() != null && param.getFlinkImage().indexOf(':') != -1){
            String[] imageRaws = param.getFlinkImage().split(":")[0].split("/");
            artifact.put("flinkImageTag", param.getFlinkImage().split(":")[1]);
            artifact.put("flinkImageRepository", imageRaws[imageRaws.length - 1]);
            artifact.put("flinkImageRegistry", param.getFlinkImage().substring(0, param.getFlinkImage().lastIndexOf("/")));
        }

        // pyflink运行环境
        artifact.put("jarUri", param.getJarUri());

        // Python venv 运行环境
        artifact.put("entryClass", param.getEntryClass());
        if(!StringUtils.isEmpty(param.getPyArchives())){
            mainArgs.append(" --pyArchives /flink/usrlib/" + param.getPyArchives());
            additionalDependencies.add(storePath + param.getPyArchives());
            if(param.getPyClientExecutable() != null){
                mainArgs.append(" --pyClientExecutable " + param.getPyArchives() + param.getPyClientExecutable());
                mainArgs.append(" --pyExecutable " + param.getPyArchives() + param.getPyClientExecutable());
            }
        }

        // 主执行文件
        mainArgs.append("--python /flink/usrlib/" + param.getPythonScriptName());
        additionalDependencies.add(storePath + param.getPythonScriptName());

        artifact.put("additionalDependencies", additionalDependencies);
        artifact.put("mainArgs", mainArgs.toString());
        return deployment;
    }

    public JSONObject generateDeploymentByStreamJob(SreworksStreamJobDTO job) throws Exception {
        JSONObject settings = job.getOptions().getJSONObject("settings");
        if(settings.getLong("pythonRuntimeId") != null){
            SreworksStreamJobRuntimeDTO runtime = runtimeGetById(settings.getLong("pythonRuntimeId"));
            StreamJobDeploymentParam deploymentParam = StreamJobDeploymentParam.builder()
                    .name(getDeploymentName(job.getId()))
                    .entryClass(runtime.getSettings().getString("entryClass"))
                    .jarUri(runtime.getSettings().getString("jarUri"))
                    .flinkVersion(runtime.getSettings().getString("flinkVersion"))
                    .flinkImage(runtime.getSettings().getString("flinkImage"))
                    .resources(settings.getJSONObject("resources"))
                    .pyArchives(runtime.getSettings().getString("pyArchives"))
                    .pyClientExecutable(runtime.getSettings().getString("pyClientExecutable"))
                    .additionalDependencies(runtime.getSettings().getJSONArray("additionalDependencies"))
                    .pythonScriptName(getScriptName(job.getId()))
                    .build();
            return generateDeployment(deploymentParam);
        }else{
            return new JSONObject();
        }
    }


    public SreworksStreamJobRuntimeDTO addRuntime(StreamJobRuntimeCreateParam param) throws Exception {
        SreworksStreamJobRuntime runtime = param.init();
        runtime = sreworksStreamJobRuntimeRepository.saveAndFlush(runtime);
        return new SreworksStreamJobRuntimeDTO(runtime);
    }

    public Page<SreworksStreamJobRuntimeDTO> runtimes(Pageable pageable) throws Exception {
        Page<SreworksStreamJobRuntime> runtimes = sreworksStreamJobRuntimeRepository.findAll(pageable);

        return runtimes.map(runtime -> {
            SreworksStreamJobRuntimeDTO runtimeDTO = new SreworksStreamJobRuntimeDTO(runtime);
            return runtimeDTO;
        });
    }

    public SreworksStreamJobRuntimeDTO runtimeGetById(Long runtimeId){
        SreworksStreamJobRuntime runtime = sreworksStreamJobRuntimeRepository.findFirstById(runtimeId);
        return  new SreworksStreamJobRuntimeDTO(runtime);
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

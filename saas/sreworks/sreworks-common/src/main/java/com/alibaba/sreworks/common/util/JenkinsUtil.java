package com.alibaba.sreworks.common.util;

import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;

import com.cdancy.jenkins.rest.JenkinsClient;
import com.cdancy.jenkins.rest.domain.common.RequestStatus;
import com.cdancy.jenkins.rest.domain.job.BuildInfo;
import com.cdancy.jenkins.rest.domain.job.JobInfo;
import com.cdancy.jenkins.rest.features.JobsApi;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class JenkinsUtil {

    public JobsApi jobsApi;

    public JenkinsUtil(String endpoint, String credentials) {
        JenkinsClient client = JenkinsClient.builder()
            .endPoint(endpoint)
            .credentials(credentials)
            .build();
        jobsApi = client.api().jobsApi();
    }

    public List<JSONObject> listJob() {
        return jobsApi.jobList("").jobs().parallelStream().map(job -> {
            String config = jobConfig(job.name());
            JSONObject configJsonObject = XmlUtil.toJsonObject(config);
            JSONObject definition = configJsonObject.getJSONObject("flow-definition");
            String timeTrigger = "";
            try {
                timeTrigger = definition
                    .getJSONObject("properties")
                    .getJSONObject("org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty")
                    .getJSONObject("triggers")
                    .getJSONObject("hudson.triggers.TimerTrigger")
                    .getString("spec");
            } catch (Exception ignored) {
            }

            return JsonUtil.map(
                "name", job.name(),
                "description", definition.getString("description"),
                "timeTrigger", timeTrigger,
                "authToken", definition.getString("authToken"),
                "script", definition.getJSONObject("definition").getString("script")
            );
        }).collect(Collectors.toList());
    }

    public String jobConfig(String jobName) {
        return jobsApi.config(null, jobName);
    }

    public void createJob(String jobName, String timeTrigger, String authToken, String description, String script) {
        jobsApi.create(
            null, jobName,
            genConfig(timeTrigger, script, authToken, description));
    }

    public void updateJob(String jobName, String timeTrigger, String authToken, String description, String script) {
        jobsApi.config(
            null, jobName,
            genConfig(timeTrigger, script, authToken, description)
        );
    }

    public void renameJob(String jobName, String newName) {
        if (!jobName.equals(newName)) {
            jobsApi.rename(null, jobName, newName);
        }
    }

    public void deleteJob(String jobName) {
        jobsApi.delete(null, jobName);
    }

    public void updateDescription(String jobName, String description) {
        jobsApi.description(null, jobName, description);
    }

    public List<BuildInfo> listBuilds(String name) {
        return jobsApi.jobInfo(null, name).builds();
    }

    private String genConfig(String timerTrigger, String script, String authToken, String description) {
        String scmTrigger = "* * * * *";
        return ""
            + "<?xml version='1.1' encoding='UTF-8'?>\n"
            + "<flow-definition plugin=\"workflow-job@2.41\">\n"
            + "  <actions>\n"
            + "    <org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobAction "
            + "plugin=\"pipeline-model-definition@1.8.5\"/>\n"
            + "    <org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobPropertyTrackerAction "
            + "plugin=\"pipeline-model-definition@1.8.5\">\n"
            + "      <jobProperties/>\n"
            + "      <triggers/>\n"
            + "      <parameters/>\n"
            + "      <options/>\n"
            + "    </org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobPropertyTrackerAction>\n"
            + "  </actions>\n"
            + "  <description>" + description + "</description>\n"
            + "  <keepDependencies>false</keepDependencies>\n"
            + "  <properties>\n"
            + "    <com.dabsquared.gitlabjenkins.connection.GitLabConnectionProperty plugin=\"gitlab-plugin@1.5.20\">\n"
            + "      <gitLabConnection></gitLabConnection>\n"
            + "      <jobCredentialId></jobCredentialId>\n"
            + "      <useAlternativeCredential>false</useAlternativeCredential>\n"
            + "    </com.dabsquared.gitlabjenkins.connection.GitLabConnectionProperty>\n"
            + "    <org.jenkinsci.plugins.workflow.job.properties.DisableConcurrentBuildsJobProperty/>\n"
            + "    <org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>\n"
            + "      <triggers>\n"
            + "        <hudson.triggers.TimerTrigger>\n"
            + "          <spec>" + timerTrigger + "</spec>\n"
            + "        </hudson.triggers.TimerTrigger>\n"
            + "        <hudson.triggers.SCMTrigger>\n"
            + "          <spec>" + scmTrigger + "</spec>\n"
            + "          <ignorePostCommitHooks>false</ignorePostCommitHooks>\n"
            + "        </hudson.triggers.SCMTrigger>\n"
            + "      </triggers>\n"
            + "    </org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>\n"
            + "  </properties>\n"
            + "  <definition class=\"org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition\" plugin=\"workflow-cps@2"
            + ".92\">\n"
            + "    <script>" + script + "</script>\n"
            + "    <sandbox>true</sandbox>\n"
            + "  </definition>\n"
            + "  <triggers/>\n"
            + "  <authToken>" + authToken + "</authToken>\n"
            + "  <disabled>false</disabled>\n"
            + "</flow-definition>";
    }

    public static void main(String[] args) {
        JenkinsUtil jenkinsUtil = new JenkinsUtil(
            "http://jenkins.ca221ae8860d9421688e59c8ab45c8b21.cn-hangzhou.alicontainer.com/", "admin:admin");
    }

}

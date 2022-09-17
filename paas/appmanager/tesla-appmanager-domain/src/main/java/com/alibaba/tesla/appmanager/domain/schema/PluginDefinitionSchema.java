package com.alibaba.tesla.appmanager.domain.schema;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.enums.PluginKindEnum;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Plugin Definition Schema
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginDefinitionSchema implements Schema {

    public static final String ANNOTATIONS_LABEL = "definition.oam.dev/label";
    public static final String ANNOTATIONS_DESCRIPTION = "definition.oam.dev/description";
    public static final String ANNOTATIONS_VERSION = "definition.oam.dev/version";
    public static final String ANNOTATIONS_TAGS = "definition.oam.dev/tags";

    private String apiVersion;
    private String kind;
    private MetaData metadata;
    private Spec spec;

    public PluginKindEnum getPluginKind() {
        return PluginKindEnum.fromString(kind);
    }

    public String getPluginName() {
        if (metadata == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "metadata is required");
        }
        return metadata.getName();
    }

    public String getPluginVersion() {
        if (metadata == null || metadata.getAnnotations() == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "annotations is required");
        }
        return metadata.getAnnotations().getString(ANNOTATIONS_VERSION);
    }

    public String getPluginDescription() {
        if (metadata == null || metadata.getAnnotations() == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "annotations is required");
        }
        return metadata.getAnnotations().getString(ANNOTATIONS_DESCRIPTION);
    }

    public List<String> getPluginTags() {
        if (metadata == null || metadata.getAnnotations() == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "annotations is required");
        }
        String tags = metadata.getAnnotations().getString(ANNOTATIONS_TAGS);
        if (StringUtils.isEmpty(tags)) {
            return new ArrayList<>();
        }
        return Arrays.asList(tags.split(","));
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetaData {

        private String name;
        private JSONObject annotations;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Spec {

        private Workload workload;
        private Schematic schematic;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Workload {

        private WorkloadDefinition definition;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkloadDefinition {

        private String apiVersion;
        private String kind;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Schematic {

        private SchematicGroovy groovy;
        private SchematicFrontend frontend;
        private SchematicResource resources;
        private JSONObject cue;
        private JSONObject helm;
        private JSONObject kustomize;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SchematicGroovy {

        private List<SchematicGroovyFile> files = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SchematicGroovyFile {

        private String kind;
        private String name;
        private String path;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SchematicFrontend {

        private List<SchematicFrontendFile> files = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SchematicFrontendFile {

        private String kind;
        private String path;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SchematicResource {

        private List<SchematicResourceFile> files = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SchematicResourceFile {

        private String kind;
        private String path;
    }
}

package com.alibaba.tesla.appmanager.meta.helm.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.deployconfig.service.DeployConfigService;
import com.alibaba.tesla.appmanager.domain.container.DeployConfigTypeId;
import com.alibaba.tesla.appmanager.domain.req.deployconfig.DeployConfigDeleteReq;
import com.alibaba.tesla.appmanager.domain.req.deployconfig.DeployConfigUpdateReq;
import com.alibaba.tesla.appmanager.meta.helm.repository.HelmMetaRepository;
import com.alibaba.tesla.appmanager.meta.helm.repository.condition.HelmMetaQueryCondition;
import com.alibaba.tesla.appmanager.meta.helm.repository.domain.HelmMetaDO;
import com.alibaba.tesla.appmanager.meta.helm.service.HelmMetaService;
import groovy.util.logging.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Service
@Slf4j
public class HelmMetaServiceImpl implements HelmMetaService {
    @Autowired
    HelmMetaRepository helmMetaRepository;

    @Autowired
    private DeployConfigService deployConfigService;

    @Override
    public Pagination<HelmMetaDO> list(HelmMetaQueryCondition condition) {
        List<HelmMetaDO> helmMetaDOs = helmMetaRepository.selectByCondition(condition);
        return Pagination.valueOf(helmMetaDOs, Function.identity());
    }

    @Override
    public HelmMetaDO get(Long id) {
        return helmMetaRepository.selectByPrimaryKey(id);
    }

    @Override
    public HelmMetaDO getByHelmPackageId(String appId, String helmPackageId){
        HelmMetaQueryCondition condition = HelmMetaQueryCondition.builder()
                .helmPackageId(helmPackageId)
                .appId(appId)
                .withBlobs(true)
                .build();
        List<HelmMetaDO> metaList = helmMetaRepository.selectByCondition(condition);
        if (metaList.size() > 0){
            return metaList.get(0);
        }else{
            return null;
        }
    }

    private void refreshDeployConfig(HelmMetaDO record) {

        JSONObject helmExt = JSONObject.parseObject(record.getHelmExt());
        String defaultValuesYaml = helmExt.getString("defaultValuesYaml");
        JSONObject gatewayRoute = helmExt.getJSONObject("gatewayRoute");
        JSONObject configObject = new JSONObject();
        JSONArray traits = new JSONArray();
        JSONArray scopes = new JSONArray();

        JSONObject nsScopeObject = new JSONObject();
        JSONObject nsObject = new JSONObject();
        JSONObject nsSpecObject = new JSONObject();
        nsSpecObject.put("autoCreate", true);
        nsObject.put("spec", nsSpecObject);
        nsObject.put("apiVersion", "core.oam.dev/v1alpha2");
        nsObject.put("kind", "Namespace");
        nsScopeObject.put("scopeRef", nsObject);
        scopes.add(nsScopeObject);

        configObject.put("revisionName", "HELM|"+record.getHelmPackageId()+"|_");

        JSONArray parameterValues = new JSONArray();
        JSONObject valuesObject = new JSONObject();
        if (StringUtils.isNotBlank(defaultValuesYaml)){
            JSONObject defaultValuesObject = SchemaUtil.createYaml(JSONObject.class).loadAs(defaultValuesYaml, JSONObject.class);
            JSONArray toFieldPaths = new JSONArray();
            toFieldPaths.add("spec.values");

            valuesObject.put("name", "values");
            valuesObject.put("value", defaultValuesObject);
            valuesObject.put("toFieldPaths", toFieldPaths);

            parameterValues.add(valuesObject);
        }

        /**   HELM部署重命名
         *         - name: name
         *           value: '{{ Global.STAGE_ID }}-{{ Global.APP_ID }}-elasticsearch'
         *           toFieldPaths:
         *             - spec.name
         */

        JSONObject renameObject = new JSONObject();
        JSONArray toNameFieldPaths = new JSONArray();
        toNameFieldPaths.add("spec.name");
        renameObject.put("name", "name");
        renameObject.put("value", "{{ Global.STAGE_ID }}-" + record.getAppId() + "-" + record.getHelmPackageId());
        renameObject.put("toFieldPaths", toNameFieldPaths);
        parameterValues.add(renameObject);

        if (gatewayRoute != null && StringUtils.isNotBlank(gatewayRoute.getString("path")) && StringUtils.isNotBlank(gatewayRoute.getString("service"))){

            JSONObject gatewayTrait = new JSONObject();
            JSONObject gatewaySpec = new JSONObject();
            String gatewayRoutePath = gatewayRoute.getString("path");
            String gatewayRouteService = gatewayRoute.getString("service");

            if(!gatewayRoutePath.startsWith("/")){
                gatewayRoutePath = "/" + gatewayRoute;
            }
            if(!gatewayRoutePath.endsWith("*")){
                gatewayRoutePath = gatewayRoute + "/**";
            }

            gatewaySpec.put("path", gatewayRoutePath);

            if(gatewayRouteService.split(":").length > 0){
                gatewaySpec.put("serviceName", gatewayRouteService.split(":")[0]);
                gatewaySpec.put("servicePort", gatewayRouteService.split(":")[1]);
            }else {
                gatewaySpec.put("serviceName", gatewayRouteService);
            }

            gatewayTrait.put("name", "gateway.trait.abm.io");
            gatewayTrait.put("runtime", "post");
            gatewayTrait.put("spec", gatewaySpec);

            traits.add(gatewayTrait);
        }

        configObject.put("parameterValues", parameterValues);
        configObject.put("traits", traits);
        configObject.put("scopes", scopes);

        Yaml yaml = SchemaUtil.createYaml(JSONObject.class);
        String typeId = new DeployConfigTypeId(ComponentTypeEnum.HELM, record.getHelmPackageId()).toString();
        deployConfigService.update(DeployConfigUpdateReq.builder()
                .apiVersion(DefaultConstant.API_VERSION_V1_ALPHA2)
                .appId(record.getAppId())
                .typeId(typeId)
                .envId("")
                .inherit(false)
                .config(yaml.dumpAsMap(configObject))
                .build());

    }

    @Override
    public int create(HelmMetaDO record) {
        int res = helmMetaRepository.insert(record);
        this.refreshDeployConfig(record);
        return res;
    }

    @Override
    public int update(HelmMetaDO record, HelmMetaQueryCondition condition) {
        int res = helmMetaRepository.updateByCondition(record, condition);
        this.refreshDeployConfig(record);
        return res;
    }

    @Override
    public int delete(Long id) {
        if (Objects.isNull(id)) {
            return 0;
        }

        HelmMetaDO record = this.get(id);
        String typeId = new DeployConfigTypeId(ComponentTypeEnum.HELM, record.getHelmPackageId()).toString();
        deployConfigService.delete(DeployConfigDeleteReq.builder()
                .apiVersion(DefaultConstant.API_VERSION_V1_ALPHA2)
                .appId(record.getAppId())
                .typeId(typeId)
                .envId("")
                .build());
        return helmMetaRepository.deleteByPrimaryKey(id);
    }
}

package com.alibaba.tesla.appmanager.meta.k8smicroservice.api;

import com.alibaba.tesla.appmanager.api.provider.K8sMicroServiceMetaProvider;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.common.service.GitService;
import com.alibaba.tesla.appmanager.common.util.*;
import com.alibaba.tesla.appmanager.deployconfig.service.DeployConfigService;
import com.alibaba.tesla.appmanager.domain.container.DeployConfigTypeId;
import com.alibaba.tesla.appmanager.domain.dto.InitContainerDTO;
import com.alibaba.tesla.appmanager.domain.dto.K8sMicroServiceMetaDTO;
import com.alibaba.tesla.appmanager.domain.dto.RepoDTO;
import com.alibaba.tesla.appmanager.domain.req.K8sMicroServiceMetaQueryReq;
import com.alibaba.tesla.appmanager.domain.req.K8sMicroServiceMetaQuickUpdateReq;
import com.alibaba.tesla.appmanager.domain.req.K8sMicroServiceMetaUpdateReq;
import com.alibaba.tesla.appmanager.domain.req.deployconfig.DeployConfigDeleteReq;
import com.alibaba.tesla.appmanager.meta.k8smicroservice.assembly.K8sMicroServiceMetaDtoConvert;
import com.alibaba.tesla.appmanager.meta.k8smicroservice.repository.condition.K8sMicroserviceMetaQueryCondition;
import com.alibaba.tesla.appmanager.meta.k8smicroservice.repository.domain.K8sMicroServiceMetaDO;
import com.alibaba.tesla.appmanager.meta.k8smicroservice.service.K8sMicroserviceMetaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * K8S 微应用元信息接口
 *
 * @author qianmo.zm@alibaba-inc.com
 */
@Slf4j
@Service
public class K8sMicroServiceMetaProviderImpl implements K8sMicroServiceMetaProvider {

    @Autowired
    private K8sMicroServiceMetaDtoConvert k8sMicroServiceMetaDtoConvert;

    @Autowired
    private K8sMicroserviceMetaService k8SMicroserviceMetaService;

    @Autowired
    private GitService gitService;

    @Autowired
    private DeployConfigService deployConfigService;

    /**
     * 分页查询微应用元信息
     */
    @Override
    public Pagination<K8sMicroServiceMetaDTO> list(K8sMicroServiceMetaQueryReq request) {
        K8sMicroserviceMetaQueryCondition condition = new K8sMicroserviceMetaQueryCondition();
        ClassUtil.copy(request, condition);
        Pagination<K8sMicroServiceMetaDO> metaList = k8SMicroserviceMetaService.list(condition);
        metaList.getItems().forEach(K8sMicroServiceMetaDO::repoFromString);
        return Pagination.transform(metaList, item -> k8sMicroServiceMetaDtoConvert.to(item));
    }

    /**
     * 通过微应用 ID 查询微应用元信息
     */
    @Override
    public K8sMicroServiceMetaDTO get(Long id) {
        K8sMicroserviceMetaQueryCondition condition = K8sMicroserviceMetaQueryCondition.builder()
                .id(id)
                .withBlobs(true)
                .build();
        Pagination<K8sMicroServiceMetaDO> page = k8SMicroserviceMetaService.list(condition);
        if (page.isEmpty()) {
            return null;
        }

        K8sMicroServiceMetaDO k8sMicroServiceMetaDO = page.getItems().get(0);
        k8sMicroServiceMetaDO.extFromString();

        K8sMicroServiceMetaDTO dto = new K8sMicroServiceMetaDTO();
        ClassUtil.copy(k8sMicroServiceMetaDO, dto);
        return dto;
    }

    /**
     * 通过微应用 ID 删除微应用元信息
     */
    @Override
    public boolean delete(Long id) {
        if (Objects.isNull(id)) {
            return true;
        }
        K8sMicroServiceMetaDTO metaDO = this.get(id);
        String typeId = new DeployConfigTypeId(ComponentTypeEnum.K8S_MICROSERVICE, metaDO.getMicroServiceId()).toString();
        deployConfigService.delete(DeployConfigDeleteReq.builder()
                .apiVersion(DefaultConstant.API_VERSION_V1_ALPHA2)
                .appId(metaDO.getAppId())
                .typeId(typeId)
                .envId("")
                .build());
        k8SMicroserviceMetaService.delete(id);
        return true;
    }

    /**
     * 创建 K8S Microservice
     */
    @Override
    public K8sMicroServiceMetaDTO create(K8sMicroServiceMetaUpdateReq request) {
        K8sMicroServiceMetaDO metaDO = new K8sMicroServiceMetaDO();
        ClassUtil.copy(request, metaDO);
        return create(metaDO);
    }

    /**
     * 更新 K8s Microservice
     */
    @Override
    public K8sMicroServiceMetaDTO update(K8sMicroServiceMetaUpdateReq request) {
        K8sMicroServiceMetaDO metaDO = new K8sMicroServiceMetaDO();
        ClassUtil.copy(request, metaDO);
        return update(metaDO);
    }

    @Override
    public K8sMicroServiceMetaDTO create(K8sMicroServiceMetaQuickUpdateReq request) {
        K8sMicroServiceMetaDO metaDO = new K8sMicroServiceMetaDO();
        ClassUtil.copy(request, metaDO);
        return create(metaDO);
    }

    @Override
    public K8sMicroServiceMetaDTO update(K8sMicroServiceMetaQuickUpdateReq request) {
        K8sMicroServiceMetaDO metaDO = new K8sMicroServiceMetaDO();
        ClassUtil.copy(request, metaDO);
        return update(metaDO);
    }

    private K8sMicroServiceMetaDTO create(K8sMicroServiceMetaDO metaDO) {
        metaDO.init();

        if (metaDO.getRepoObject() != null) {
            RepoDTO repo = metaDO.getRepoObject();
            if(!ShellUtil.check(repo.getRepo(), repo.getCiAccount(), repo.getCiToken())){
                if(StringUtils.isNotEmpty(metaDO.getRepoObject().getRepoTemplateUrl())){
                    autoCreateRepo(metaDO);
                }else{
                    /**
                     * 如果没有设置模板且Project不存在，则直接创建Project
                     */
                    GitlabUtil.createProject(repo.getRepoDomain(), repo.getRepoGroup(), repo.getRepoProject(), repo.getCiToken());
                }
            }
        }

        k8SMicroserviceMetaService.create(metaDO);

        if (CollectionUtils.isNotEmpty(metaDO.getContainerObjectList())) {
            gitService.createRepoList(metaDO.getContainerObjectList());
        }

        return k8sMicroServiceMetaDtoConvert.to(metaDO);
    }

    private K8sMicroServiceMetaDTO update(K8sMicroServiceMetaDO metaDO) {
        metaDO.init();

        K8sMicroserviceMetaQueryCondition condition = K8sMicroserviceMetaQueryCondition.builder()
                .appId(metaDO.getAppId())
                .microServiceId(metaDO.getMicroServiceId())
                .build();
        k8SMicroserviceMetaService.update(metaDO, condition);
        return k8sMicroServiceMetaDtoConvert.to(metaDO);
    }

    private void autoCreateRepo(K8sMicroServiceMetaDO metaDO) throws AppException {
        RepoDTO repoDTO = metaDO.getRepoObject();
        GitlabUtil.createProject(repoDTO.getRepoDomain(), repoDTO.getRepoGroup(), repoDTO.getRepoProject(),
                repoDTO.getCiToken());

        String zipFile = metaDO.getAppId() + "." + metaDO.getMicroServiceId() + ".zip";
        File zipFilePath = HttpUtil.download(repoDTO.getRepoTemplateUrl(), zipFile);

        String unZipDir = StringUtils.substringBeforeLast(zipFilePath.getAbsolutePath(), ".");
        log.info(">>>k8sMicroServiceMetaProvider|autoCreateRepo|unZipDir={}", unZipDir);
        ZipUtil.unzip(zipFilePath.getAbsolutePath(), unZipDir);

        List<InitContainerDTO> initContainerList = metaDO.getInitContainerList();
        String scriptPath = unZipDir + File.separator + "sw.sh";
        if (CollectionUtils.isNotEmpty(initContainerList)) {
            for (InitContainerDTO initContainerDTO : initContainerList) {
                ShellUtil.init(scriptPath, initContainerDTO.createContainerName(), "initContainer",
                        initContainerDTO.createDockerFileTemplate(),
                        initContainerDTO.getType().toLowerCase());
            }
        }


        ShellUtil.push(scriptPath, repoDTO.getRepo(), repoDTO.getCiAccount(), repoDTO.getCiToken());

        FileUtil.deleteDir(zipFilePath);
        FileUtil.deleteDir(new File(unZipDir));
    }
}

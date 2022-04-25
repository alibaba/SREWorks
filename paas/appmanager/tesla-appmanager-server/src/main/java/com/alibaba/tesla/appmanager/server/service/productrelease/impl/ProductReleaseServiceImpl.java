package com.alibaba.tesla.appmanager.server.service.productrelease.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.api.provider.AppPackageTaskProvider;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum;
import com.alibaba.tesla.appmanager.common.enums.ProductReleaseTaskStatusEnum;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.service.GitService;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.domain.req.apppackage.AppPackageTaskCreateReq;
import com.alibaba.tesla.appmanager.domain.req.apppackage.ComponentBinder;
import com.alibaba.tesla.appmanager.domain.req.productrelease.*;
import com.alibaba.tesla.appmanager.domain.res.apppackage.AppPackageTaskCreateRes;
import com.alibaba.tesla.appmanager.domain.res.productrelease.CheckProductReleaseTaskRes;
import com.alibaba.tesla.appmanager.domain.res.productrelease.CreateAppPackageTaskInProductReleaseTaskRes;
import com.alibaba.tesla.appmanager.domain.res.productrelease.CreateProductReleaseTaskRes;
import com.alibaba.tesla.appmanager.server.repository.*;
import com.alibaba.tesla.appmanager.server.repository.condition.*;
import com.alibaba.tesla.appmanager.server.repository.domain.*;
import com.alibaba.tesla.appmanager.server.service.productrelease.ProductReleaseService;
import com.alibaba.tesla.appmanager.server.service.productrelease.business.ProductReleaseBO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 产品发布版本服务
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@Service
public class ProductReleaseServiceImpl implements ProductReleaseService {

    @Autowired
    private ProductReleaseSchedulerRepository productReleaseSchedulerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private ProductReleaseRelRepository productReleaseRelRepository;

    @Autowired
    private ProductReleaseAppRelRepository productReleaseAppRelRepository;

    @Autowired
    private ProductReleaseTaskRepository productReleaseTaskRepository;

    @Autowired
    private ProductReleaseTaskAppPackageTaskRelRepository productReleaseTaskAppPackageTaskRelRepository;

    @Autowired
    private GitService gitService;

    @Autowired
    private AppPackageTaskProvider appPackageTaskProvider;

    /**
     * 获取当前系统中的全部 Schduler 列表
     *
     * @return Scheduler List 对象
     */
    @Override
    public List<ProductReleaseSchedulerDO> listScheduler() {
        ProductReleaseSchedulerQueryCondition condition = ProductReleaseSchedulerQueryCondition.builder().build();
        return productReleaseSchedulerRepository.selectByCondition(condition);
    }

    /**
     * 获取当前系统中的指定 Scheduler
     *
     * @param productId 产品 ID
     * @param releaseId 发布版本 ID
     * @return Scheduler 对象
     */
    @Override
    public ProductReleaseSchedulerDO getScheduler(String productId, String releaseId) {
        ProductReleaseSchedulerQueryCondition condition = ProductReleaseSchedulerQueryCondition.builder()
                .productId(productId)
                .releaseId(releaseId)
                .build();
        return productReleaseSchedulerRepository.getByCondition(condition);
    }

    /**
     * 根据 productId 和 releaseId 获取对应产品、发布版本及相关引用的全量信息
     *
     * @param productId 产品 ID
     * @param releaseId 发布版本 ID
     * @return 全量信息
     */
    @Override
    public ProductReleaseBO get(String productId, String releaseId) {
        ProductDO product = productRepository.getByCondition(ProductQueryCondition.builder()
                .productId(productId).build());
        ReleaseDO release = releaseRepository.getByCondition(ReleaseQueryCondition.builder()
                .releaseId(releaseId).build());
        ProductReleaseRelDO productReleaseRel = productReleaseRelRepository
                .getByCondition(ProductReleaseRelQueryCondition.builder()
                        .productId(productId)
                        .releaseId(releaseId)
                        .build());
        List<ProductReleaseAppRelDO> appRelList = productReleaseAppRelRepository
                .selectByCondition(ProductReleaseAppRelQueryCondition.builder()
                        .productId(productId)
                        .releaseId(releaseId)
                        .build());

        // 任意为空均返回无法找到数据
        if (product == null || release == null || productReleaseRel == null || CollectionUtils.isEmpty(appRelList)) {
            return null;
        }
        return ProductReleaseBO.builder()
                .product(product)
                .release(release)
                .productReleaseRel(productReleaseRel)
                .appRelList(appRelList)
                .build();
    }

    /**
     * 创建产品发布版本任务实例
     *
     * @param request 请求内容
     * @return 任务 ID 及应用包 ID 内容
     */
    @Override
    public CreateProductReleaseTaskRes createProductReleaseTask(CreateProductReleaseTaskReq request) {
        // 创建任务
        String taskId = UUID.randomUUID().toString().replaceAll("-", "");
        ProductReleaseTaskDO taskDO = ProductReleaseTaskDO.builder()
                .productId(request.getProductId())
                .releaseId(request.getReleaseId())
                .taskId(taskId)
                .schedulerType(request.getSchedulerType())
                .schedulerValue(request.getSchedulerValue())
                .status(request.getStatus().toString())
                .build();
        int count = productReleaseTaskRepository.insert(taskDO);
        if (count == 0) {
            throw new AppException(AppErrorCode.UNKNOWN_ERROR, "cannot insert product release task, count=0|task={}",
                    JSONObject.toJSONString(taskDO));
        }
        log.info("product release task has created|taskId={}|request={}", taskId, JSONObject.toJSONString(request));
        return CreateProductReleaseTaskRes.builder()
                .taskId(taskId)
                .build();
    }

    /**
     * 检查当前是否存在冲突的产品发布版本运行项任务
     *
     * @param request 请求内容
     * @return 检测结果
     */
    @Override
    public CheckProductReleaseTaskRes checkProductReleaseTask(CheckProductReleaseTaskReq request) {
        ProductReleaseTaskQueryCondition condition = ProductReleaseTaskQueryCondition.builder()
                .productId(request.getProductId())
                .releaseId(request.getReleaseId())
                .status(Arrays.asList(
                        ProductReleaseTaskStatusEnum.RUNNING.toString(),
                        ProductReleaseTaskStatusEnum.PENDING.toString()))
                .build();
        List<ProductReleaseTaskDO> tasks = productReleaseTaskRepository.selectByCondition(condition);
        if (tasks.size() == 0) {
            return CheckProductReleaseTaskRes.builder().exists(false).build();
        }
        return CheckProductReleaseTaskRes.builder()
                .exists(true)
                .conflict(tasks.stream().map(ProductReleaseTaskDO::getTaskId).collect(Collectors.toList()))
                .build();
    }

    /**
     * 根据过滤条件获取产品发布版本任务列表
     *
     * @param request 请求内容
     * @return 过滤出的列表
     */
    @Override
    public List<ProductReleaseTaskDO> listProductReleaseTask(ListProductReleaseTaskReq request) {
        ProductReleaseTaskQueryCondition condition = ProductReleaseTaskQueryCondition.builder()
                .productId(request.getProductId())
                .releaseId(request.getReleaseId())
                .status(request.getStatus())
                .build();
        return productReleaseTaskRepository.selectByCondition(condition);
    }

    /**
     * 更新 ProductRelease Task
     *
     * @param task 任务
     * @return 更新成功行数
     */
    @Override
    public int updateProductReleaseTask(ProductReleaseTaskDO task) {
        return productReleaseTaskRepository.updateByCondition(task, ProductReleaseTaskQueryCondition.builder()
                .taskId(task.getTaskId())
                .build());
    }

    /**
     * 根据过滤条件获取产品发布版本任务关联的应用包任务列表
     *
     * @param request 请求内容
     * @return 过滤出的列表
     */
    @Override
    public List<ProductReleaseTaskAppPackageTaskRelDO> listProductReleaseTaskAppPackageTask(
            ListProductReleaseTaskAppPackageTaskReq request) {
        ProductReleaseTaskAppPackageTaskRelQueryCondition condition =
                ProductReleaseTaskAppPackageTaskRelQueryCondition.builder()
                        .taskId(request.getTaskId())
                        .build();
        return productReleaseTaskAppPackageTaskRelRepository.selectByCondition(condition);
    }

    /**
     * 将产品发布版本任务设置状态
     *
     * @param taskId     任务 ID
     * @param fromStatus From 状态
     * @param toStatus   To 状态
     */
    @Override
    public void markProductReleaseTaskStatus(
            String taskId, ProductReleaseTaskStatusEnum fromStatus, ProductReleaseTaskStatusEnum toStatus) {
        ProductReleaseTaskQueryCondition condition;
        if (fromStatus != null) {
            condition = ProductReleaseTaskQueryCondition.builder()
                    .taskId(taskId)
                    .status(Collections.singletonList(fromStatus.toString()))
                    .build();
        } else {
            condition = ProductReleaseTaskQueryCondition.builder().taskId(taskId).build();
        }
        ProductReleaseTaskDO task = productReleaseTaskRepository.getByCondition(condition);
        if (task == null) {
            log.info("cannot find product release task by taskId {} and fromStatus {}, skip", taskId, fromStatus);
            return;
        }

        task.setStatus(toStatus.toString());
        int count = productReleaseTaskRepository.updateByCondition(task, condition);
        if (count == 0) {
            log.info("mark product release task failed, skip|taskId={}|count=0|status={}", taskId, toStatus);
        } else {
            log.info("mark product release task success|taskId={}|count=1|status={}", taskId, toStatus);
        }
    }

    /**
     * 在产品发布版本任务中创建 AppPackage 打包任务
     *
     * @param request 请求内容
     * @return 应用包任务 ID
     */
    @Override
    public CreateAppPackageTaskInProductReleaseTaskRes createAppPackageTaskInProductReleaseTask(
            CreateAppPackageTaskInProductReleaseTaskReq request) {
        // Git 切换到自己的分支
        gitService.checkoutBranch(request.getLogContent(), request.getBranch(), request.getDir());
        log.info("git repo has checkout to branch {}|dir={}|productId={}|releaseId={}",
                request.getBranch(), request.getDir().toString(), request.getProductId(), request.getReleaseId());

        // 创建应用包任务
        AppPackageTaskCreateRes res = appPackageTaskProvider.create(
                AppPackageTaskCreateReq.builder()
                        .appId(request.getAppId())
                        .tags(Collections.singletonList(request.getTag()))
                        .components(buildYamlToComponentBinderList(request.getDir(), request.getBuildPath()))
                        .build(),
                DefaultConstant.SYSTEM_OPERATOR);
        Long appPackageTaskId = res.getAppPackageTaskId();

        // 插入关联数据
        ProductReleaseTaskAppPackageTaskRelDO appPackageTaskRelDO = ProductReleaseTaskAppPackageTaskRelDO.builder()
                .taskId(request.getTaskId())
                .appPackageTaskId(appPackageTaskId)
                .build();
        int count = productReleaseTaskAppPackageTaskRelRepository.insert(appPackageTaskRelDO);
        if (count == 0) {
            throw new AppException(AppErrorCode.UNKNOWN_ERROR, "cannot insert product release task app package task " +
                    "rel, count=0|task={}", JSONObject.toJSONString(appPackageTaskRelDO));
        }
        log.info("product release task app package task rel has created|taskId={}|appPackageTaskId={}|request={}",
                request.getTaskId(), appPackageTaskId, JSONObject.toJSONString(request));

        return CreateAppPackageTaskInProductReleaseTaskRes.builder()
                .appPackageTaskId(appPackageTaskId)
                .build();
    }

    /**
     * 将指定的 build.yaml 文件转换为内部的 ComponentBinder List 对象
     *
     * @param dir       Git Clone 的本地目录
     * @param buildPath 构建文件 Yaml 路径
     * @return List of ComponentBinder
     */
    private List<ComponentBinder> buildYamlToComponentBinderList(Path dir, String buildPath) {
        Yaml yaml = SchemaUtil.createYaml(Arrays.asList(Iterable.class, Object.class));
        Path actualPath = dir.resolve(buildPath);
        Iterable<Object> iterable;
        try {
            iterable = yaml.loadAll(new String(Files.readAllBytes(actualPath)));
        } catch (IOException e) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("load build path failed, path=%s", actualPath));
        }

        List<ComponentBinder> results = new ArrayList<>();
        for (Object object : iterable) {
            JSONObject root = new JSONObject((Map) object);
            JSONObject options = root.getJSONObject("options");
            String componentName = root.getString("componentName");
            ComponentTypeEnum componentType = ComponentTypeEnum.valueOf(root.getString("componentType"));
            results.add(ComponentBinder.builder()
                    .componentType(componentType)
                    .componentName(componentName)
                    .useRawOptions(true)
                    .options(options)
                    .build());
        }
        return results;
    }
}

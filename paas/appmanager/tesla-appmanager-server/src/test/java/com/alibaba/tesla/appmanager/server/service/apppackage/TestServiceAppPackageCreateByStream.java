package com.alibaba.tesla.appmanager.server.service.apppackage;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.autoconfig.PackageProperties;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.common.util.PackageUtil;
import com.alibaba.tesla.appmanager.common.util.VersionUtil;
import com.alibaba.tesla.appmanager.deployconfig.service.DeployConfigService;
import com.alibaba.tesla.appmanager.domain.req.apppackage.AppPackageCreateByStreamReq;
import com.alibaba.tesla.appmanager.server.repository.AppPackageComponentRelRepository;
import com.alibaba.tesla.appmanager.server.repository.AppPackageRepository;
import com.alibaba.tesla.appmanager.server.repository.AppPackageTagRepository;
import com.alibaba.tesla.appmanager.server.repository.CustomAddonMetaRepository;
import com.alibaba.tesla.appmanager.server.repository.condition.AppPackageQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.domain.AppPackageDO;
import com.alibaba.tesla.appmanager.server.service.apppackage.impl.AppPackageServiceImpl;
import com.alibaba.tesla.appmanager.server.service.componentpackage.ComponentPackageService;
import com.alibaba.tesla.appmanager.server.service.deploy.DeployAppService;
import com.alibaba.tesla.appmanager.server.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(SpringRunner.class)
@Slf4j
public class TestServiceAppPackageCreateByStream {

    private static final String BUCKET_NAME = "appmanager";
    private static final String APP_ID = "testapp";
    private static final String PACKAGE_CREATOR = "SYSTEM";
    private static final String PACKAGE_VERSION = "2.1.1+20220201234212";
    private static final InputStream BODY = IOUtils.toInputStream("body", StandardCharsets.UTF_8);

    @Mock
    private AppPackageRepository appPackageRepository;

    @Mock
    private AppPackageTagRepository appPackageTagRepository;

    @Mock
    private AppPackageComponentRelRepository relRepository;

    @Mock
    private ComponentPackageService componentPackageService;

    @Mock
    private CustomAddonMetaRepository customAddonMetaRepository;

    @Mock
    private DeployAppService deployAppService;

    @Mock
    private PackageProperties packageProperties;

    @Mock
    private Storage storage;

    @Mock
    private DeployConfigService deployConfigService;

    private AppPackageService appPackageService;

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);

        appPackageService = Mockito.spy(new AppPackageServiceImpl(
                appPackageRepository,
                appPackageTagRepository,
                relRepository,
                componentPackageService,
                customAddonMetaRepository,
                deployAppService,
                packageProperties,
                storage,
                deployConfigService
        ));
        Mockito.doReturn(BUCKET_NAME).when(packageProperties).getBucketName();
    }

    /**
     * ????????? AppPackage ??????????????? Mock ??????
     */
    private void prepareAppPackageForNotExistsScene() {
        AppPackageQueryCondition condition = AppPackageQueryCondition.builder()
                .appId(APP_ID)
                .packageVersion(PACKAGE_VERSION)
                .withBlobs(true)
                .build();
        Mockito.doReturn(null).when(appPackageRepository).getByCondition(condition);
        Mockito.doReturn(new Pagination<AppPackageDO>())
                .when(appPackageService)
                .list(AppPackageQueryCondition.builder().appId(APP_ID).build());
    }

    /**
     * ????????? AppPackage ???????????? Mock ??????
     */
    private void prepareAppPackageForExistsScene() {
        AppPackageQueryCondition condition = AppPackageQueryCondition.builder()
                .appId(APP_ID)
                .packageVersion(PACKAGE_VERSION)
                .withBlobs(true)
                .build();
        AppPackageDO res = AppPackageDO.builder()
                .id(1L)
                .appId(APP_ID)
                .packageVersion(PACKAGE_VERSION)
                .packagePath(PackageUtil.buildAppPackagePath(BUCKET_NAME, APP_ID, PACKAGE_VERSION))
                .packageCreator(PACKAGE_CREATOR)
                .componentCount(1L)
                .version(0)
                .build();
        Mockito.doReturn(res).when(appPackageRepository).getByCondition(condition);
        Pagination<AppPackageDO> listRes = new Pagination<>();
        listRes.setItems(Collections.singletonList(res));
        listRes.setTotal(1);
        listRes.setPage(1);
        listRes.setPageSize(1);
        Mockito.doReturn(listRes)
                .when(appPackageService)
                .list(AppPackageQueryCondition.builder().appId(APP_ID).build());
    }

    /**
     * ????????? force == false && resetVersion == true ????????????????????????
     */
    @Test
    public void testCreateWithResetVersion() throws Exception {
        AppPackageCreateByStreamReq request = AppPackageCreateByStreamReq.builder()
                .appId(APP_ID)
                .packageVersion(PACKAGE_VERSION)
                .packageCreator(PACKAGE_CREATOR)
                .body(BODY)
                .force(false)
                .resetVersion(true)
                .build();

        // ????????????????????? => 1.0.1
        prepareAppPackageForNotExistsScene();
        AppPackageDO res = appPackageService.createByStream(request);
        verifyCreateRes(res, request, false);
        Mockito.verify(appPackageRepository, Mockito.times(1)).insert(Mockito.any());
        Mockito.verify(appPackageRepository, Mockito.times(0)).updateByPrimaryKeySelective(Mockito.any());

        // ?????????????????? => 2.1.2
        prepareAppPackageForExistsScene();
        res = appPackageService.createByStream(request);
        verifyCreateRes(res, request, true);
        Mockito.verify(appPackageRepository, Mockito.times(2)).insert(Mockito.any());
        Mockito.verify(appPackageRepository, Mockito.times(0)).updateByPrimaryKeySelective(Mockito.any());
    }

    /**
     * ????????? force == true && resetVersion == false ????????????????????????
     */
    @Test
    public void testCreateWithForce() throws Exception {
        AppPackageCreateByStreamReq request = AppPackageCreateByStreamReq.builder()
                .appId(APP_ID)
                .packageVersion(PACKAGE_VERSION)
                .packageCreator(PACKAGE_CREATOR)
                .body(BODY)
                .force(true)
                .resetVersion(false)
                .build();

        // ????????????????????? => 2.1.1
        prepareAppPackageForNotExistsScene();
        AppPackageDO res = appPackageService.createByStream(request);
        verifyCreateRes(res, request, false);
        Mockito.verify(appPackageRepository, Mockito.times(1)).insert(Mockito.any());
        Mockito.verify(appPackageRepository, Mockito.times(0)).updateByPrimaryKeySelective(Mockito.any());

        // ?????????????????? => 2.1.1
        prepareAppPackageForExistsScene();
        res = appPackageService.createByStream(request);
        verifyCreateRes(res, request, true);
        Mockito.verify(appPackageRepository, Mockito.times(1)).insert(Mockito.any());
        Mockito.verify(appPackageRepository, Mockito.times(1)).updateByPrimaryKeySelective(Mockito.any());
    }

    /**
     * ????????? force == false && resetVersion == false ????????????????????????
     */
    @Test
    public void testCreateWithNothing() throws Exception {
        final AppPackageCreateByStreamReq request = AppPackageCreateByStreamReq.builder()
                .appId(APP_ID)
                .packageVersion(PACKAGE_VERSION)
                .packageCreator(PACKAGE_CREATOR)
                .body(BODY)
                .force(false)
                .resetVersion(false)
                .build();

        // ????????????????????? => 2.1.1
        prepareAppPackageForNotExistsScene();
        AppPackageDO res = appPackageService.createByStream(request);
        verifyCreateRes(res, request, false);
        Mockito.verify(appPackageRepository, Mockito.times(1)).insert(Mockito.any());
        Mockito.verify(appPackageRepository, Mockito.times(0)).updateByPrimaryKeySelective(Mockito.any());

        // ?????????????????? => ?????? Exception
        prepareAppPackageForExistsScene();
        assertThatThrownBy(() -> appPackageService.createByStream(request))
                .isInstanceOf(AppException.class);
    }

    /**
     * ??????????????????????????????????????????
     */
    @Test
    public void testCreateWithInvalidParameters() throws Exception {
        AppPackageCreateByStreamReq request = AppPackageCreateByStreamReq.builder()
                .appId(APP_ID)
                .packageVersion(PACKAGE_VERSION)
                .packageCreator(PACKAGE_CREATOR)
                .body(BODY)
                .force(true)
                .resetVersion(true)
                .build();
        assertThatThrownBy(() -> appPackageService.createByStream(request))
                .isInstanceOf(AppException.class);
    }

    /**
     * ?????? packageVersion ?????????
     *
     * @param packageVersion   PackageVersion
     * @param request          ????????????
     * @param reqPackageExists ????????????????????????
     */
    private void verifyPackageVersion(
            String packageVersion, AppPackageCreateByStreamReq request, boolean reqPackageExists) {
        String clearVersion = VersionUtil.clear(packageVersion);
        if (request.isResetVersion()) {
            if (reqPackageExists) {
                assertThat(clearVersion).isEqualTo(
                        VersionUtil.clear(VersionUtil.buildNextPatch(request.getPackageVersion())));
            } else {
                assertThat(clearVersion).isEqualTo(VersionUtil.clear(VersionUtil.buildNextPatch()));
            }
        } else {
            assertThat(clearVersion).isEqualTo(VersionUtil.clear(request.getPackageVersion()));
        }
    }

    /**
     * ?????? packagePath ?????????
     *
     * @param packagePath      PackagePath
     * @param request          ????????????
     * @param reqPackageExists ????????????????????????
     */
    private void verifyPackagePath(String packagePath, AppPackageCreateByStreamReq request, boolean reqPackageExists) {
        String[] arr = packagePath.split("/");
        assertThat(arr.length).isEqualTo(4);
        assertThat(arr[0]).isEqualTo(BUCKET_NAME);
        assertThat(arr[1]).isEqualTo("apps");
        assertThat(arr[2]).isEqualTo(request.getAppId());

        // ?????? packagePath ?????? filename ?????????
        String filename = arr[3];
        assertThat(filename.endsWith(".zip")).isTrue();
        String packageVersion = filename.substring(0, filename.length() - 4);
        verifyPackageVersion(packageVersion, request, reqPackageExists);
    }

    /**
     * ????????????????????????????????????
     *
     * @param res              ????????????
     * @param request          ????????????
     * @param reqPackageExists ????????????????????????
     */
    private void verifyCreateRes(AppPackageDO res, AppPackageCreateByStreamReq request, boolean reqPackageExists) {
        assertThat(res).isNotNull();
        log.info("createRes={}", JSONObject.toJSONString(res));
        assertThat(res.getAppId()).isEqualTo(request.getAppId());
        assertThat(res.getComponentCount()).isEqualTo(0L);
        assertThat(res.getPackageCreator()).isEqualTo(request.getPackageCreator());
        verifyPackageVersion(res.getPackageVersion(), request, reqPackageExists);
        verifyPackagePath(res.getPackagePath(), request, reqPackageExists);
    }
}

package com.alibaba.tesla.appmanager.server.provider.impl;

import com.alibaba.tesla.appmanager.api.provider.RtAppInstanceProvider;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.common.util.ClassUtil;
import com.alibaba.tesla.appmanager.domain.dto.RtAppInstanceDTO;
import com.alibaba.tesla.appmanager.domain.dto.RtAppInstanceHistoryDTO;
import com.alibaba.tesla.appmanager.domain.dto.RtComponentInstanceDTO;
import com.alibaba.tesla.appmanager.domain.dto.RtComponentInstanceHistoryDTO;
import com.alibaba.tesla.appmanager.domain.req.rtappinstance.RtAppInstanceHistoryQueryReq;
import com.alibaba.tesla.appmanager.domain.req.rtappinstance.RtAppInstanceQueryReq;
import com.alibaba.tesla.appmanager.domain.req.rtappinstance.RtComponentInstanceHistoryQueryReq;
import com.alibaba.tesla.appmanager.server.assembly.RtAppInstanceDtoConvert;
import com.alibaba.tesla.appmanager.server.assembly.RtAppInstanceHistoryDtoConvert;
import com.alibaba.tesla.appmanager.server.assembly.RtComponentInstanceDtoConvert;
import com.alibaba.tesla.appmanager.server.assembly.RtComponentInstanceHistoryDtoConvert;
import com.alibaba.tesla.appmanager.server.repository.condition.RtAppInstanceHistoryQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.condition.RtAppInstanceQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.condition.RtComponentInstanceHistoryQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.condition.RtComponentInstanceQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.domain.RtAppInstanceDO;
import com.alibaba.tesla.appmanager.server.repository.domain.RtAppInstanceHistoryDO;
import com.alibaba.tesla.appmanager.server.repository.domain.RtComponentInstanceDO;
import com.alibaba.tesla.appmanager.server.repository.domain.RtComponentInstanceHistoryDO;
import com.alibaba.tesla.appmanager.server.service.rtappinstance.RtAppInstanceService;
import com.alibaba.tesla.appmanager.server.service.rtcomponentinstance.RtComponentInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * ?????????????????? Provider
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@Service
public class RtAppInstanceProviderImpl implements RtAppInstanceProvider {

    @Autowired
    private RtAppInstanceService rtAppInstanceService;

    @Autowired
    private RtComponentInstanceService rtComponentInstanceService;

    @Autowired
    private RtAppInstanceDtoConvert rtAppInstanceConvert;

    @Autowired
    private RtAppInstanceHistoryDtoConvert rtAppInstanceHistoryConvert;

    @Autowired
    private RtComponentInstanceDtoConvert rtComponentInstanceConvert;

    @Autowired
    private RtComponentInstanceHistoryDtoConvert rtComponentInstanceHistoryConvert;

    /**
     * ????????????????????????????????????
     *
     * @param request ????????????
     * @return ????????????
     */
    @Override
    public Pagination<RtAppInstanceDTO> queryByCondition(RtAppInstanceQueryReq request) {
        RtAppInstanceQueryCondition condition = new RtAppInstanceQueryCondition();
        ClassUtil.copy(request, condition);
        Pagination<RtAppInstanceDO> results = rtAppInstanceService.list(condition);
        return Pagination.transform(results, item -> rtAppInstanceConvert.to(item));
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param request ????????????
     * @return ????????????
     */
    @Override
    public Pagination<RtAppInstanceHistoryDTO> queryAppInstanceHistoryByCondition(RtAppInstanceHistoryQueryReq request) {
        RtAppInstanceHistoryQueryCondition condition = new RtAppInstanceHistoryQueryCondition();
        ClassUtil.copy(request, condition);
        Pagination<RtAppInstanceHistoryDO> results = rtAppInstanceService.listHistory(condition);
        return Pagination.transform(results, item -> rtAppInstanceHistoryConvert.to(item));
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param request ????????????
     * @return ????????????
     */
    @Override
    public Pagination<RtComponentInstanceHistoryDTO> queryComponentInstanceHistoryByCondition(
            RtComponentInstanceHistoryQueryReq request) {
        RtComponentInstanceHistoryQueryCondition condition = new RtComponentInstanceHistoryQueryCondition();
        ClassUtil.copy(request, condition);
        Pagination<RtComponentInstanceHistoryDO> results = rtComponentInstanceService.listHistory(condition);
        return Pagination.transform(results, item -> rtComponentInstanceHistoryConvert.to(item));
    }

    /**
     * ???????????? appInstanceId ?????????????????????????????????
     *
     * @param appInstanceId ???????????? ID
     * @return ??????????????????????????????????????? null
     */
    @Override
    public RtAppInstanceDTO get(String appInstanceId) {
        RtAppInstanceQueryCondition condition = RtAppInstanceQueryCondition.builder()
                .appInstanceId(appInstanceId)
                .build();
        Pagination<RtAppInstanceDO> results = rtAppInstanceService.list(condition);
        if (results.isEmpty()) {
            return null;
        }
        if (results.getTotal() > 1) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("multiple app instance found, appInstanceId=%s", appInstanceId));
        }

        // ?????? response ??????
        RtComponentInstanceQueryCondition componentCondition = RtComponentInstanceQueryCondition.builder()
                .appInstanceId(appInstanceId)
                .build();
        Pagination<RtComponentInstanceDO> componentResults = rtComponentInstanceService.list(componentCondition);
        RtAppInstanceDTO response = rtAppInstanceConvert.to(results.getItems().get(0));
        response.setComponents(componentResults.getItems().stream()
                .map(p -> rtComponentInstanceConvert.to(p))
                .collect(Collectors.toList()));
        return response;
    }

    /**
     * ???????????? componentInstanceId ?????????????????????????????????
     *
     * @param appInstanceId       ???????????? ID
     * @param componentInstanceId ???????????? ID
     * @return ??????????????????????????????????????? null
     */
    @Override
    public RtComponentInstanceDTO getComponentInstance(String appInstanceId, String componentInstanceId) {
        RtComponentInstanceDO result = rtComponentInstanceService.get(RtComponentInstanceQueryCondition.builder()
                .appInstanceId(appInstanceId)
                .componentInstanceId(componentInstanceId)
                .build());
        return rtComponentInstanceConvert.to(result);
    }

    /**
     * ???????????????????????????
     *
     * @param appInstanceId ???????????? ID
     * @return RtAppInstanceDTO
     */
    @Override
    public int delete(String appInstanceId) {
        return rtAppInstanceService.delete(appInstanceId);
    }
}

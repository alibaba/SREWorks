package com.alibaba.tesla.appmanager.server.controller;

import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
import com.alibaba.tesla.appmanager.common.constants.RedisKeyConstant;
import com.alibaba.tesla.appmanager.common.enums.DeployComponentStateEnum;
import com.alibaba.tesla.appmanager.server.service.deploy.DeployComponentService;
import com.alibaba.tesla.appmanager.server.service.deploy.business.DeployComponentBO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;

import static com.alibaba.tesla.appmanager.common.constants.RedisKeyConstant.STREAM_LOG_KEY;

/**
 * WebSocketDeployLog Controller
 */
@Slf4j
@Component
@ServerEndpoint("/ws/deploy/{deployComponentId}/ws-logs")
@Tag(name = "WebSocketDeployLog API")
public class WebSocketDeployLogController extends AppManagerBaseController {

    private static DeployComponentService deployComponentService;

    private static StreamMessageListenerContainer streamMessageListenerContainer;

    @Autowired
    public void setComponentPackageProvider(DeployComponentService deployComponentService) {
        WebSocketDeployLogController.deployComponentService = deployComponentService;
    }

    @Autowired
    public void setStreamMessageListenerContainer(StreamMessageListenerContainer streamMessageListenerContainer) {
        WebSocketDeployLogController.streamMessageListenerContainer = streamMessageListenerContainer;
    }


    @OnOpen
    public void onOpen(@PathParam("deployComponentId") Long deployComponentId, Session session) throws IOException {
        log.info("new ws connection: {}", deployComponentId);
        DeployComponentBO response = deployComponentService.get(deployComponentId, false);
        String status = response.getSubOrder().getDeployStatus();
        if (status.equals(DeployComponentStateEnum.CREATED.toString())
                || status.equals(DeployComponentStateEnum.PROCESSING.toString())
                || status.equals(DeployComponentStateEnum.RUNNING.toString())) {
            String streamKey = String.format("%s_%s", RedisKeyConstant.DEPLOY_TASK_LOG, deployComponentId);
            streamMessageListenerContainer.receive(StreamOffset.fromStart(streamKey),
                    message -> {
                        Object stream = message.getStream();
                        RecordId id = message.getId();
                        Map<String, String> messageValue = (Map) message.getValue();
                        log.debug("receive a message. stream: [{}],id: [{}],value: [{}]", stream, id, messageValue);
                        try {
                            session.getBasicRemote().sendText(messageValue.get(STREAM_LOG_KEY));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } else {
            session.getBasicRemote().sendText("");
        }
    }

    //关闭时执行
    @OnClose
    public void onClose() {
        log.warn("ws连接关闭");
    }

    //收到消息时执行
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        log.debug("ignore client message: {}", message);
    }

    //连接错误时执行
    @OnError
    public void onError(Session session, Throwable error) {
        log.warn("ws连接发送错误");
        error.printStackTrace();
    }
}

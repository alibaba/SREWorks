package com.alibaba.tesla.appmanager.server.controller;

import com.alibaba.tesla.appmanager.api.provider.ComponentPackageProvider;
import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
import com.alibaba.tesla.appmanager.common.constants.RedisKeyConstant;
import com.alibaba.tesla.appmanager.common.enums.ComponentPackageTaskStateEnum;
import com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum;
import com.alibaba.tesla.appmanager.domain.dto.ComponentPackageTaskDTO;
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
 * WebSocketPackageLog Controller
 */
@Slf4j
@Component
@ServerEndpoint("/ws/package/{taskId}/ws-logs")
@Tag(name = "WebSocketPackageLog API")
public class WebSocketPackageLogController extends AppManagerBaseController {

    private static ComponentPackageProvider componentPackageProvider;

    private static StreamMessageListenerContainer streamMessageListenerContainer;

    @Autowired
    public void setComponentPackageProvider(ComponentPackageProvider componentPackageProvider) {
        WebSocketPackageLogController.componentPackageProvider = componentPackageProvider;
    }

    @Autowired
    public void setStreamMessageListenerContainer(StreamMessageListenerContainer streamMessageListenerContainer) {
        WebSocketPackageLogController.streamMessageListenerContainer = streamMessageListenerContainer;
    }


    @OnOpen
    public void onOpen(@PathParam("taskId") Long taskId, Session session) throws IOException {
        log.info("new ws connection: {}", taskId);
        ComponentPackageTaskDTO response = componentPackageProvider.getTask(taskId, null);
        String componentType = response.getComponentType();
        String status = response.getTaskStatus();
        String taskLog = response.getTaskLog();
        if (taskLog == null) {
            taskLog = "";
        }
        if ((status.equals(ComponentPackageTaskStateEnum.CREATED.toString())
                || status.equals(ComponentPackageTaskStateEnum.RUNNING.toString())) &&
                (componentType.equals(ComponentTypeEnum.K8S_JOB.toString())
                        || componentType.equals(ComponentTypeEnum.K8S_MICROSERVICE.toString()))) {
            String streamKey = String.format("%s_%s_%s_%s_%s", RedisKeyConstant.COMPONENT_PACKAGE_TASK_LOG,
                    response.getAppId(), response.getComponentType(), response.getComponentName(), response.getPackageVersion());
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
            session.getBasicRemote().sendText(taskLog);
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

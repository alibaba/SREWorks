package com.alibaba.tesla.appmanager.workflow.controller;

import com.alibaba.tesla.appmanager.api.provider.WorkflowTaskProvider;
import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
import com.alibaba.tesla.appmanager.autoconfig.PackageProperties;
import com.alibaba.tesla.appmanager.common.constants.RedisKeyConstant;
import com.alibaba.tesla.appmanager.common.enums.WorkflowTaskStateEnum;
import com.alibaba.tesla.appmanager.domain.dto.WorkflowTaskDTO;
import com.alibaba.tesla.appmanager.server.storage.Storage;
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
import java.text.SimpleDateFormat;
import java.util.Map;

import static com.alibaba.tesla.appmanager.common.constants.RedisKeyConstant.STREAM_LOG_KEY;

/**
 * WorkflowLogWebSocket Controller
 */
@Slf4j
@Component
@ServerEndpoint("/ws/workflow/{taskId}/ws-logs")
@Tag(name = "WorkflowLogWebSocket API")
public class WebSocketWorkflowLogController extends AppManagerBaseController {
    private static WorkflowTaskProvider workflowTaskProvider;

    private static StreamMessageListenerContainer streamMessageListenerContainer;

    private static Storage storage;

    private static PackageProperties packageProperties;

    @Autowired
    public void setComponentPackageProvider(WorkflowTaskProvider workflowTaskProvider) {
        WebSocketWorkflowLogController.workflowTaskProvider = workflowTaskProvider;
    }

    @Autowired
    public void setStreamMessageListenerContainer(StreamMessageListenerContainer streamMessageListenerContainer) {
        WebSocketWorkflowLogController.streamMessageListenerContainer = streamMessageListenerContainer;
    }

    @Autowired
    public void setStorage(Storage storage) {
        WebSocketWorkflowLogController.storage = storage;
    }

    @Autowired
    public void setPackageProperties(PackageProperties packageProperties) {
        WebSocketWorkflowLogController.packageProperties = packageProperties;
    }

    @OnOpen
    public void onOpen(@PathParam("taskId") Long taskId, Session session) throws IOException {
        log.info("new ws connection: {}", taskId);

        WorkflowTaskDTO response = workflowTaskProvider.get(taskId, true);
        String streamKey = String.format("%s_%s", RedisKeyConstant.WORKFLOW_TASK_LOG,
                response.getId());
        String status = response.getTaskStatus();
        if (status.equals(WorkflowTaskStateEnum.PENDING.toString())
                || status.equals(WorkflowTaskStateEnum.RUNNING.toString())
                || status.equals(WorkflowTaskStateEnum.WAITING.toString())
                || status.equals(WorkflowTaskStateEnum.WAITING_SUSPEND.toString())) {

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
            String date = new SimpleDateFormat("yyyyMMdd").format(response.getGmtCreate());
            String bucketName = packageProperties.getBucketName();
            String objectName = String.format("stream_log/%s/%s.txt", date, streamKey);
            session.getBasicRemote().sendText(storage.getObjectContent(bucketName,objectName));
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

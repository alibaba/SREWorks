package com.alibaba.tdata.sever;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.tdata.aisp.server.common.dto.UserSimpleInfo;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.ClientOptions.DisconnectedBehavior;
import io.lettuce.core.RedisClient;
import org.junit.Test;
import org.springframework.util.StringUtils;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @Test
    public void userTest() {
        List<String> empInfoList = Arrays.asList("265412");
        List<UserSimpleInfo> userSimpleInfoList= new LinkedList<>();
        for (String s : empInfoList) {
            UserSimpleInfo userSimpleInfo = from(s);
            if (userSimpleInfo!=null){
                userSimpleInfoList.add(userSimpleInfo);
            }
        }
        System.out.println(userSimpleInfoList.size());
    }

    public UserSimpleInfo from(String creator) {
        if (StringUtils.isEmpty(creator)){
            return null;
        }
        int i = creator.indexOf(":");
        if (i>-1){
            String[] split = creator.split(":");
            UserSimpleInfo userInfo = new UserSimpleInfo();
            if (split.length>1){
                userInfo.setEmpId(split[0]);
                userInfo.setNickNameCn(split[1]);
            } else if (split.length==1){
                if (i==0){
                    userInfo.setNickNameCn(split[0]);
                } else {
                    userInfo.setEmpId(split[0]);
                }
            } else {
                return null;
            }
            return userInfo;
        } else {
            return null;
        }
    }

    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        RedisClient redisClient = RedisClient.create("redis://siJEULmQYQ@11.158.188.56:30007/0?timeout=5s");
        redisClient.setOptions(ClientOptions.builder()
            .disconnectedBehavior(DisconnectedBehavior.REJECT_COMMANDS)
            .build());
        System.out.println(redisClient.connect());
    }
}

//package com.alibaba.tesla.appmanager.common.service;
//
//import com.alibaba.tesla.appmanager.autoconfig.ImageBuilderProperties;
//import com.alibaba.tesla.appmanager.common.service.impl.GitServiceImpl;
//import com.alibaba.tesla.appmanager.domain.req.git.GitCloneReq;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.nio.file.Files;
//import java.nio.file.Path;
//
//@RunWith(SpringRunner.class)
//@Slf4j
//public class TestServiceGitCloneRepo {
//
//    @Mock
//    private ImageBuilderProperties imageBuilderProperties;
//
//    @Before
//    public void init() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    public void testClone() throws Exception {
//        GitServiceImpl gitService = new GitServiceImpl(imageBuilderProperties);
//        StringBuilder logContent = new StringBuilder();
//        GitCloneReq request = GitCloneReq.builder()
//                .repo("http://gitlab.alibaba-inc.com/pe3/elasticsearch.git")
//                .branch("live")
////                .repoPath("elasticsearch/plugins")
//                .ciAccount("alisre-deploy")
//                .ciToken("CmWtxka7M2FyWVzbRkzs")
//                .build();
//        Path dir = Files.createTempDirectory("test");
//        log.info("current dir: {}", dir.toString());
//        gitService.cloneRepo(logContent, request, dir);
//    }
//}

package com.alibaba.tesla.tkgone.server.controllers.database.neo4j;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import com.alibaba.tesla.web.controller.BaseController;
import com.alibaba.tesla.tkgone.server.services.database.elasticsearch.ElasticSearchDeleteByQueryService;
import com.alibaba.tesla.tkgone.server.services.database.elasticsearch.ElasticSearchSearchService;
import com.alibaba.tesla.tkgone.server.services.database.elasticsearch.ElasticSearchUpsertService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yangjinghua
 */
@RestController
@RequestMapping("/database/neo4j/relation")
public class Neo4jRelationController extends BaseController {

    @Autowired
    ElasticSearchSearchService elasticSearchSearchService;

    @Autowired
    ElasticSearchDeleteByQueryService elasticSearchDeleteByQueryService;

    @Autowired
    ElasticSearchUpsertService elasticSearchUpsertService;

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public TeslaBaseResult create(@RequestBody JSONArray relation) {
        elasticSearchUpsertService.insertRelation(relation.toJavaList(JSONObject.class));
        return buildSucceedResult("OK");
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public TeslaBaseResult get(String startNodeType, String startNodeId, String endNodeType, String endNodeId,
            String relationType, String relationId, Integer limit) throws Exception {
        return buildSucceedResult(elasticSearchSearchService.getRelationNodes(startNodeType, startNodeId, endNodeType,
                endNodeId, relationType, relationId, limit));
    }

    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public TeslaBaseResult delete(String startNodeType, String startNodePartition, String startNodeId,
            String endNodeType, String endNodePartition, String endNodeId, String relationType, String relationId)
            throws Exception {
        elasticSearchSearchService.delete(startNodeType, startNodePartition, startNodeId, endNodeType, endNodePartition,
                endNodeId, relationType, relationId);
        return buildSucceedResult("started");

    }
}

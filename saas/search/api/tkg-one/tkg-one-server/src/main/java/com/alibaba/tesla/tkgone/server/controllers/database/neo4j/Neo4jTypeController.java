package com.alibaba.tesla.tkgone.server.controllers.database.neo4j;

import com.alibaba.tesla.common.base.TeslaBaseResult;
import com.alibaba.tesla.tkgone.server.services.database.elasticsearch.ElasticSearchSearchService;
import com.alibaba.tesla.tkgone.server.services.database.elasticsearch.ElasticSearchUpsertService;
import com.alibaba.tesla.web.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yangjinghua
 */
@RestController
@RequestMapping("/database/neo4j/type")
public class Neo4jTypeController extends BaseController {

    @Autowired
    ElasticSearchUpsertService elasticSearchUpsertService;

    @Autowired
    ElasticSearchSearchService elasticSearchSearchService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public TeslaBaseResult list() throws Exception {
        return buildSucceedResult(elasticSearchSearchService.listRelationType().keySet());
    }

    @RequestMapping(value = "/delete/{relationType}", method = RequestMethod.DELETE)
    public TeslaBaseResult delete(@PathVariable String relationType) throws Exception {
        elasticSearchSearchService.deleteRelationType(relationType);
        return buildSucceedResult("started");
    }

    @RequestMapping(value = "/count/{relationType}", method = RequestMethod.DELETE)
    public TeslaBaseResult count(@PathVariable String relationType) throws Exception {
        return buildSucceedResult(elasticSearchSearchService.listRelationType());
    }

}

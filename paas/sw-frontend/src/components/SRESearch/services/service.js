/**
 * Created by xuwei on 17/12/29.
 */

import httpClient from "../../../utils/httpClient";


class Service {


  searchTypeList(userEmpId, teslaSearchPath, category, search_content) {
    // httpClient.get("gateway/v2/foundation/appmanager/instances", {params: params})
    return httpClient.get(`${teslaSearchPath}/teslasearch/query/node_nums_group_by_type`,
      {
        params: {
          _userEmpId: userEmpId,
          category: category,
          query: search_content,
        },
      });
  }

  searchTypeElements(userEmpId, teslaSearchPath, category, search_content, type, page, pageSize) {
    return httpClient.get(`${teslaSearchPath}/teslasearch/query/query_nodes_from_size_add_exlink`,
      {
        params: {
          _userEmpId: userEmpId,
          category: category,
          query: search_content,
          type: type,
          page: page,
          size: pageSize,
        },
      });
  }

  searchSuggestions(userEmpId, teslaSearchPath, category, search_content, page, pageSize) {
    return httpClient.get(`${teslaSearchPath}/sreworkssearch/query/suggest`, {
      params: {
        _userEmpId: userEmpId,
        category: category,
        query: search_content,
        page: page,
        size: pageSize,
      },
    });
  }

  getHotKeywords(userEmpId, teslaSearchPath, category, limit = 10) {
    return httpClient.get(`${teslaSearchPath}/sreworkssearch/query/get_hot_keywords`, {
      params: {
        _userEmpId: userEmpId,
        category: category,
        limit: limit,
      },
    });
  }

  getCommonKeywords(userEmpId, teslaSearchPath, category, limit = 5) {
    return httpClient.get(`${teslaSearchPath}/teslasearch/get_meta/get_common_keyword`, {
      params: {
        _userEmpId: userEmpId,
        category: category,
        limit: limit,
      },
    });
  }
  desktopKeySearch(userEmpId, category, query, currentPage = 1, size = 10) {
    return httpClient.get(`sreworkssearch/query/query_simple_nodes_from_size?__userEmpId=${userEmpId}&category=${category}&query=${query}&page=${currentPage}&size=${size}`)
  }


}

export default new Service();

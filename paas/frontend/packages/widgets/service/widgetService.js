 import { httpClient }  from '@sreworks/shared-tools';

 class Service {
     getCustomList () {
         return httpClient.get('/gateway/v2/foundation/frontend-service/frontend/component/list?stageId=prod')
     }
 
 }
 
 const service = new Service();
 
 export default service;
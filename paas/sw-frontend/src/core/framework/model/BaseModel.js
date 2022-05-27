/**
 * Created by caoshuaibiao on 2020/11/30.
 * 编辑器模型
 */
import { EventEmitter } from 'eventemitter3';
import DataSource from './DataSource';

export default class BaseModel {

    events = new EventEmitter();
    dataSource;
    dataSourceMeta;
    searchParams;

    constructor(modelJson) {
        Object.assign(this, modelJson);
    }


    delete() {
        if (!this.id) {
            return new Promise(
                function (resolve, reject) {
                    return resolve({ success: true, message: "删除成功" });
                }
            );
        }
        return new Promise(
            function (resolve, reject) {
                return reject({ success: false, message: "需子类删除" });
            }
        );
    }

    /**
     * 由模型json数据初始化模型对象
     * @param modelJson
     */
    fromJSON(modelJson) {

    }

    /**
     * 获取序列化后的json定义
     */
    toJSON() {
        return {}
    }

    /**
     * 所有模型全部支持数据源
     * @param dataSource
     */
    addDataSource(dataSource) {
        this.dataSource = dataSource;
    }

    setDataSourceMeta(meta) {
        console.log(meta,"执行了1")
        this.dataSourceMeta = meta;
    }
    setSearchParams(params){
        this.searchParams = params
    }
    getDataSource() {
        if (this.dataSourceMeta) {
            return new DataSource(this.dataSourceMeta);
        }
        return null;
    }


}
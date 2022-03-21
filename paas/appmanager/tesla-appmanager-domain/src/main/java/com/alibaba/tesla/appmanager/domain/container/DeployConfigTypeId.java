package com.alibaba.tesla.appmanager.domain.container;

import com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * 部署配置 Type Id
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public class DeployConfigTypeId {

    public static final String TYPE_PARAMETER_VALUES = "parameterValues";
    public static final String TYPE_COMPONENTS = "components";

    @Getter
    private final String type;

    private List<Pair<String, String>> attrs;

    public DeployConfigTypeId(String type) {
        this.type = type;
        this.attrs = new ArrayList<>();
    }

    public DeployConfigTypeId(ComponentTypeEnum componentType) {
        this.type = TYPE_COMPONENTS;
        this.attrs = new ArrayList<>();
        this.attrs.add(Pair.of("ComponentType", componentType.toString()));
    }

    public DeployConfigTypeId(ComponentTypeEnum componentType, String componentName) {
        this.type = TYPE_COMPONENTS;
        this.attrs = new ArrayList<>();
        this.attrs.add(Pair.of("ComponentType", componentType.toString()));
        this.attrs.add(Pair.of("ComponentName", componentName));
    }

    public static DeployConfigTypeId valueOf(String typeId) {
        String[] arr = typeId.split("::");
        if (arr.length == 0) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "typeId must have :: spliter");
        } else if (!arr[0].startsWith("Type:")) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "invalid typeId item " + arr[0]);
        }

        String type = arr[0].split(":", 2)[1];
        DeployConfigTypeId result = new DeployConfigTypeId(type);
        for (int i = 1; i < arr.length; i++) {
            String[] itemArr = arr[i].split(":", 2);
            if (itemArr.length < 2) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS, "invalid typeId item " + arr[i]);
            }
            result.attrs.add(Pair.of(itemArr[0], itemArr[1]));
        }
        return result;
    }

    @Override
    public String toString() {
        List<String> arr = new ArrayList<>();
        arr.add(String.format("Type:%s", this.type));
        if (this.attrs != null) {
            for (Pair<String, String> attr : this.attrs) {
                arr.add(String.format("%s:%s", attr.getKey(), attr.getValue()));
            }
        }
        return String.join("::", arr);
    }
}

package com.alibaba.tesla.appmanager.server.assembly;

import com.alibaba.tesla.appmanager.common.assembly.BaseDtoConvert;
import com.alibaba.tesla.appmanager.domain.dto.ProductReleaseAppRelDTO;
import com.alibaba.tesla.appmanager.server.repository.domain.ProductReleaseAppRelDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProductReleaseAppRelDtoConvert extends BaseDtoConvert<ProductReleaseAppRelDTO, ProductReleaseAppRelDO> {

    public ProductReleaseAppRelDtoConvert() {
        super(ProductReleaseAppRelDTO.class, ProductReleaseAppRelDO.class);
    }
}

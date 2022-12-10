package com.alibaba.tesla.appmanager.server.assembly;

import com.alibaba.tesla.appmanager.common.assembly.BaseDtoConvert;
import com.alibaba.tesla.appmanager.domain.dto.ProductDTO;
import com.alibaba.tesla.appmanager.server.repository.domain.ProductDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProductDtoConvert extends BaseDtoConvert<ProductDTO, ProductDO> {

    public ProductDtoConvert() {
        super(ProductDTO.class, ProductDO.class);
    }
}

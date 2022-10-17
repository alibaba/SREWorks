package com.alibaba.tesla.appmanager.spring.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
class OctetStreamHttpMessageConverter {

    /**
     * 增加 Message 转换器，为流式处理文件提供支持
     */
    @Bean
    public HttpMessageConverters octetStreamConverter() {
        AbstractHttpMessageConverter<InputStream> converter
                = new AbstractHttpMessageConverter<>(MediaType.APPLICATION_OCTET_STREAM) {
            @Override
            protected boolean supports(Class<?> clazz) {
                return InputStream.class.isAssignableFrom(clazz);
            }

            @Override
            protected InputStream readInternal(Class<? extends InputStream> clazz, HttpInputMessage inputMessage) throws
                    IOException, HttpMessageNotReadableException {
                return inputMessage.getBody();
            }

            @Override
            protected void writeInternal(InputStream inputStream, HttpOutputMessage outputMessage) throws IOException,
                    HttpMessageNotWritableException {
                IOUtils.copy(inputStream, outputMessage.getBody());
            }
        };
        return new HttpMessageConverters(converter);
    }
}
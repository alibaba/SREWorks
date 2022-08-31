//package com.alibaba.tesla.appmanager.spring.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//@Slf4j
//class AppManagerWebConfig implements WebMvcConfigurer {
//
////    /**
////     * 增加 Message 转换器，为流式处理文件提供支持
////     */
////    @Override
////    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
////        super.addDefaultHttpMessageConverters(converters);
////        converters.add(new AbstractHttpMessageConverter<InputStream>(MediaType.APPLICATION_OCTET_STREAM) {
////            @Override
////            protected boolean supports(Class<?> clazz) {
////                return InputStream.class.isAssignableFrom(clazz);
////            }
////
////            @Override
////            protected InputStream readInternal(Class<? extends InputStream> clazz, HttpInputMessage inputMessage) throws
////                IOException, HttpMessageNotReadableException {
////                return inputMessage.getBody();
////            }
////
////            @Override
////            protected void writeInternal(InputStream inputStream, HttpOutputMessage outputMessage) throws IOException,
////                HttpMessageNotWritableException {
////                IOUtils.copy(inputStream, outputMessage.getBody());
////            }
////        });
////        super.configureMessageConverters(converters);
////    }
//
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("doc.html").addResourceLocations("classpath*:/META-INF/resources/");
//        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath*:/META-INF/resources/webjars/");
//    }
//}
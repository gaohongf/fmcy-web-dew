package xyz.fmcy.server.spring.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class ServerConfiguration {

    public static final String DEFAULT_SERVER_MESSAGE_SOURCE_ENCODING = "UTF-8";
    public static final String DEFAULT_SERVER_MESSAGE_SOURCE_BASENAME = "fmcy-server-message";

    @Bean
    public ControllerBuilder controllerBuilder(GenericApplicationContext applicationContext) {
        return new ControllerBuilder(applicationContext);
    }

    @Bean("fmcyServerMessageSource")
    public MessageSource messageSource(
            @Value("${fmcy.server.message.basename:#{null}}") String basename,
            @Value("${fmcy.server.message.encoding:#{null}}") String encoding
    ) {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setDefaultEncoding(encoding == null ? DEFAULT_SERVER_MESSAGE_SOURCE_ENCODING : encoding);
        messageSource.setBasename(basename == null ? DEFAULT_SERVER_MESSAGE_SOURCE_BASENAME : basename);
        return messageSource;
    }
}

package xyz.fmcy.server.spring.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import javax.annotation.Resource;

@Configuration
public class ServerResultCodeConfiguration {
    public static int ADD_FAIL_CODE;
    public static int UPDATE_FAIL_CODE;
    public static int DELETE_FAIL_CODE;
    public static int QUERY_FAIL_CODE;

    public static MessageSource message;

    @Resource(name = "fmcyServerMessageSource")
    public void setMessageSource(MessageSource messageSource) {
        ServerResultCodeConfiguration.message = messageSource;
    }
    @Autowired
    public void setAddFailCode(@Nullable @Value("${server.add.error.code:#{1001}}") int code) {
        ADD_FAIL_CODE = code;
    }
    @Autowired
    public void setDeleteFailCode(@Nullable @Value("${server.delete.error.code:#{1002}}") int code) {
        DELETE_FAIL_CODE = code;
    }
    @Autowired
    public void setQueryFailCode(@Nullable @Value("${server.query.error.code:#{1003}}") int code) {
        QUERY_FAIL_CODE = code;
    }
    @Autowired
    public void setUpdateFailCode(@Nullable @Value("${server.update.error.code:#{1004}}") int code) {
        UPDATE_FAIL_CODE = code;
    }
}
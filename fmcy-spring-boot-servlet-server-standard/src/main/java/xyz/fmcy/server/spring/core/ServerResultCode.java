package xyz.fmcy.server.spring.core;

import org.springframework.context.i18n.LocaleContextHolder;
import xyz.fmcy.server.standard.HttpCode;
import xyz.fmcy.server.standard.ResultCode;


public enum ServerResultCode implements ResultCode {
    ADD_FAIL(ServerResultCodeConfiguration.ADD_FAIL_CODE, "server.web.response.add.error"),
    UPDATE_FAIL(ServerResultCodeConfiguration.UPDATE_FAIL_CODE, "server.web.response.update.error"),
    DELETE_FAIL(ServerResultCodeConfiguration.DELETE_FAIL_CODE, "server.web.response.delete.error"),
    QUERY_FAIL(ServerResultCodeConfiguration.QUERY_FAIL_CODE, "server.web.response.query.error"),
    ADD_SUCCESS(HttpCode.HTTP_200.getCode(), "server.web.response.add.success"),
    UPDATE_SUCCESS(HttpCode.HTTP_200.getCode(), "server.web.response.update.success"),
    DELETE_SUCCESS(HttpCode.HTTP_200.getCode(), "server.web.response.delete.success"),
    QUERY_SUCCESS(HttpCode.HTTP_200.getCode(), "server.web.response.query.success");

    public final int code;
    public final String messageKey;

    private static final Object[] EMPTY_ARRAY = new Object[0];

    ServerResultCode(int code, String messageKey) {
        this.code = code;
        this.messageKey = messageKey;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getCodeMessage() {
        return ServerResultCodeConfiguration.message.getMessage(messageKey,
                EMPTY_ARRAY,
                LocaleContextHolder.getLocale()
        );
    }
}

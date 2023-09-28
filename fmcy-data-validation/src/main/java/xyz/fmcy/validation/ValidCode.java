package xyz.fmcy.validation;

import xyz.fmcy.server.standard.ResultCode;

/**
 * @author 付高宏
 * @date 2023/2/7 16:56
 */

public enum ValidCode implements ResultCode {
    PASS_2000(2000, "通过"),
    FAILED_4000(4000, "字段验证不通过");

    private final int code;
    private final String message;

    ValidCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getCodeMessage() {
        return message;
    }
}

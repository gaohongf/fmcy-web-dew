package xyz.fmcy.server.spring.core;

import xyz.fmcy.server.standard.ResultCode;

public enum ServerErrorResultCode implements ResultCode {
    ADD_FAIL(1001, "添加失败"),
    UPDATE_FAIL(1002, "修改失败"),
    DELETE_FAIL(1003, "删除失败"),
    QUERY_FAIL(1004, "查询失败");
    private final int code;
    private final String message;

    ServerErrorResultCode(int code, String message) {
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

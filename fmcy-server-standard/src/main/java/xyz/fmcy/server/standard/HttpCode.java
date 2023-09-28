package xyz.fmcy.server.standard;

/**
 * @author 付高宏
 * @date 2023/2/6 11:14
 */
public enum HttpCode implements ResultCode {
    HTTP_200(200, "请求成功"),
    HTTP_201(201, "创建成功"),
    HTTP_202(202, "请求成功,无响应结果"),
    HTTP_400(400, "错误的请求语法"),
    HTTP_401(401, "无身份"),
    HTTP_403(403, "无权访问"),
    HTTP_404(404, "无资源"),
    HTTP_405(405, "目标资源不支持该方式请求"),
    HTTP_410(410,"资源已被标记为失效"),
    HTTP_418(418,"请不要在酒吧点炒饭"),
    HTTP_500(500,"服务器异常");
    private final int code;
    private final String message;

    HttpCode(int code, String codeMessage) {
        this.code = code;
        this.message = codeMessage;
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

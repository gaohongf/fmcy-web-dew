package xyz.fmcy.server.standard;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ResultBuilder
 *
 * @author 付高宏
 * @date 2023/2/6 10:43
 */
@Deprecated
public class R {
    private final Result result;
    private static final SimpleDateFormat DATE_FORMATTER;

    static {
        DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    private R(ResultCode code) {
        result = new Result(code);
        result.setMessages(new ArrayList<>());
        result.setCreateTime(DATE_FORMATTER.format(new Date()));
    }

    public R addMsg(Message message) {
        result.getMessages().add(message);
        return this;
    }

    public R addMsgs(List<Message> message) {
        result.getMessages().addAll(message);
        return this;
    }

    public Result getResult() {
        return result;
    }

    public static R create(ResultCode code, Object data) {
        R r = new R(code);
        r.result.setData(data);
        return r;
    }

    public static R prevent() {
        return new R(HttpCode.HTTP_403);
    }

    public static R lose() {
        return new R(HttpCode.HTTP_404);
    }

    public static R info(Object data) {
        R r = new R(HttpCode.HTTP_200);
        r.result.setData(data);
        return r;
    }

    public static Message message(MessageType type, String content) {
        return new Message(type, content);
    }
}

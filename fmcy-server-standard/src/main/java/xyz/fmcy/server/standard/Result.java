package xyz.fmcy.server.standard;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author 付高宏
 * @date 2023/2/6 10:13
 */
public class Result<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 20230828214703L;
    /**
     * 请求结果代码
     */
    private int code;
    /**
     * 请求结果附带的消息
     */
    private List<Message> messages;
    /**
     * 附带数据
     */
    private T data;
    /**
     * 结果创建时间
     */
    private String createTime;

    public static DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss").toFormatter();

    public Result(ResultCode code) {
        this.code = code.getCode();
    }

    public Result() {
    }

    public Result(int code) {
        this.code = code;
    }

    public Result(ResultCode code, List<Message> messages, T data, String createTime) {
        this.code = code.getCode();
        this.messages = new ArrayList<>(messages);
        this.data = data;
        this.createTime = createTime;
    }


    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", messages=" + messages +
                ", data=" + data +
                ", createTime='" + createTime + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Result<?> result)) return false;
        return Objects.equals(code, result.code) && Objects.equals(messages, result.messages) && Objects.equals(data, result.data) && Objects.equals(createTime, result.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, messages, data, createTime);
    }

    public void setMessages(List<Message> messages) {
        this.messages = new ArrayList<>(messages);
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getCode() {
        return code;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public Object getData() {
        return data;
    }

    public String getCreateTime() {
        return createTime;
    }

    public Result<T> addMessage(Message message) {
        this.messages.add(message);
        return this;
    }

    public static <T> Result<T> createResult(int code, T data, List<Message> messages) {
        Result<T> result = new Result<>();
        result.code = code;
        result.data = data;
        result.createTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        result.messages = messages;
        return result;
    }

    public static <T> Result<T> ok(int code) {
        return ok(code, null);
    }

    public static <T> Result<T> ok(ResultCode code) {
        return ok(code,null, new Message[0]);
    }

    public static <T> Result<T> ok(int code, T data, Message... messages) {
        return createResult(code, data, Arrays.stream(messages).collect(Collectors.toList()));
    }

    public static <T> Result<T> ok(int code, T data) {
        return ok(code, data, new Message[0]);
    }


    public static <T> Result<T> ok(ResultCode code, T data, Message... messages) {
        return ok(code.getCode(), data, messages.length != 0
                ? messages
                : new Message[]{Message.info(code.getCodeMessage())}
        );
    }

    public static <T> Result<T> ok(ResultCode code, T data) {
        return ok(code.getCode(), data, new Message[0]);
    }

    public static <T> Result<T> lose() {
        return error(HttpCode.HTTP_404);
    }

    public static <T> Result<T> prevent() {
        return error(HttpCode.HTTP_403);
    }

    public static <T> Result<T> emptyMethod() {
        return error(HttpCode.HTTP_405);
    }

    public static <T> Result<T> error(ResultCode code, Message... messages) {
        return error(code, null, messages);
    }

    public static <T> Result<T> error(ResultCode code) {
        return error(code, null, new Message[0]);
    }

    public static <T> Result<T> error(int code, Message... messages) {
        return error(code, null, messages);
    }

    public static <T> Result<T> error(int code) {
        return error(code, null, new Message[0]);
    }

    public static <T> Result<T> error(int code, T data, Message... messages) {
        return createResult(code, data, Arrays.stream(messages).collect(Collectors.toList()));
    }

    public static <T> Result<T> error(int code, T data) {
        return error(code, data, new Message[0]);
    }

    public static <T> Result<T> error(ResultCode code, T data, Message... messages) {
        return error(code.getCode(), data, messages.length != 0
                ? messages
                : new Message[]{Message.error(code.getCodeMessage())}
        );
    }

    public static <T> Result<T> error(ResultCode code, T data) {
        return error(code.getCode(), data, new Message[0]);
    }

    public static <T> Result<T> success() {
        return success(null, Message.success(HttpCode.HTTP_200.getCodeMessage()));
    }

    public static <T> Result<T> success(T data, Message... messages) {
        return createResult(HttpCode.HTTP_200.getCode(), data, Arrays.stream(messages).collect(Collectors.toList()));
    }

    public static <T> Result<T> success(T data) {
        return success(data, new Message[0]);
    }
}

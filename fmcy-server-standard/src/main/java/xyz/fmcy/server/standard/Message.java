package xyz.fmcy.server.standard;

import java.util.Objects;

/**
 * @author 付高宏
 * @date 2023/2/6 10:15
 */
public class Message {
    private MessageType type;
    private String content;

    public Message() {
    }

    public Message(MessageType type, String content) {
        this.type = type;
        this.content = content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public MessageType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", content='" + content + '\'' +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message message)) return false;
        return type == message.type && Objects.equals(content, message.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, content);
    }

    public static Message error(String message) {
        return new Message(MessageType.ERROR, message);
    }

    public static Message warning(String message) {
        return new Message(MessageType.WARNING, message);
    }

    public static Message info(String message) {
        return new Message(MessageType.INFO, message);
    }

    public static Message success(String message) {
        return new Message(MessageType.SUCCESS, message);
    }

    public static Message success(ResultCode resultCode) {
        return new Message(MessageType.SUCCESS, resultCode.getCodeMessage());
    }
}


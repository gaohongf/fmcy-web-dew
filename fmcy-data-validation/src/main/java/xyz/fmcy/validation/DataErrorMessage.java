package xyz.fmcy.validation;

import xyz.fmcy.server.standard.Message;
import xyz.fmcy.server.standard.MessageType;

/**
 * @author 付高宏
 * @date 2023/2/8 9:46
 */
public class DataErrorMessage extends Message {
    private Integer code;
    private String name;
    public DataErrorMessage(String message,Integer code, String name) {
        this.code = code;
        this.name = name;
        setType(MessageType.ERROR);
        setContent(message);
    }
    public String getName() {
        return name;
    }

    // @el(data: String)
    public void setName(String name) {
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}

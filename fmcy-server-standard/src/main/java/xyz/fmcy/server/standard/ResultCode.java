package xyz.fmcy.server.standard;

import java.util.Objects;

/**
 * @author 付高宏
 * @date 2023/2/6 10:48
 */
public interface ResultCode {
    /**
     * @return 结果代码
     */
    int getCode();

    /**
     * @return 这个结果的代码对应的讯息
     */
    String getCodeMessage();

    boolean equals(Object o);

    static boolean equals(ResultCode code1, ResultCode code2) {
        return (code1.getCode() == code2.getCode()) &&
                Objects.equals(code1.getCodeMessage(), code1.getCodeMessage());
    }
}

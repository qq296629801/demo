package com.sie.iidp.example.mixmodel.exception;

import com.sie.snest.sdk.exception.SdkAppException;
import org.springframework.lang.NonNull;

/**
 * 业务异常
 *
 * @author lijun10
 * @date 2022/12/5 13:54
 */
public class MbmException extends SdkAppException {

    /**
     * 自己业务错误码
     */
    protected static int errorCode = 21002;
    /**
     * 默认提示，在语言包增加语言映射即可支持多语言提示：getLocalizedMessage()，引擎已重写此方法
     */
    protected static String msg = "MBM异常";

    @Override
    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String getDefaultMsg() {
        return msg;
    }

    public MbmException() {
        super(msg);
    }

    public MbmException(String message) {
        super(message);
    }

    public MbmException(String message, Throwable cause) {
        super(message, cause);
    }

    public MbmException(Throwable cause) {
        super(msg, cause);
    }

    public MbmException(String messageFormat, Object... args) {
        super(messageFormat, args);
    }

    public MbmException(Throwable cause, String messageFormat, Object... args) {
        super(cause, null, messageFormat, args);
    }

}

package xyz.fmcy.validation;

import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import xyz.fmcy.server.standard.Message;
import xyz.fmcy.server.standard.Result;
import xyz.fmcy.server.standard.ResultCode;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用于处理错误的请求格式
 *
 * @author 付高宏
 * @date 2022/11/21 13:36
 */
@ControllerAdvice
public class ValidationExceptionEntry {

    @ResponseBody
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public Result<Void> tryValidationException(MethodArgumentNotValidException e) {
        return getBindResultBody(e.getBindingResult());
    }

    @ResponseBody
    @ExceptionHandler({BindException.class})
    public Result<Void> tryValidationException(BindException e) {
        return getBindResultBody(e.getBindingResult());
    }

    @ResponseBody
    @ExceptionHandler({ConstraintViolationException.class})
    public Result<Void> tryValidationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        List<DataErrorMessage> messageList = constraintViolations.stream()
                .map(this::getErrorMessage)
                .collect(Collectors.toList());
        return getErrorResult(messageList);
    }


    private DataErrorMessage getErrorMessage(ConstraintViolation<?> violation) {
        Annotation annotation = violation.getConstraintDescriptor().getAnnotation();
        try {
            Method codeMethod = annotation.getClass().getDeclaredMethod("code");
            ResultCode code = (ResultCode) codeMethod.invoke(annotation);
            String codeMessage = code.getCodeMessage();
            return new DataErrorMessage(violation.getMessage().replace("{code.message}", codeMessage),
                    code.getCode(), code.toString());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            return new DataErrorMessage(violation.getMessage().replace("{code.message}",
                    ValidCode.FAILED_4000.getCodeMessage()
            ), ValidCode.FAILED_4000.getCode(), ValidCode.FAILED_4000.name());
        }
    }

    private Result<Void> getBindResultBody(BindingResult bindingResult) {
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        return getErrorResult(fieldErrors.stream()
                .map(error -> {
                    Class<? extends FieldError> clazz = error.getClass();
                    try {
                        Field field = clazz.getDeclaredField("violation");
                        if (!field.canAccess(error)) {
                            field.setAccessible(true);
                        }
                        ConstraintViolation<?> violation = (ConstraintViolation<?>) field.get(error);
                        return getErrorMessage(violation);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        return new DataErrorMessage(Objects.requireNonNull(error.getDefaultMessage())
                                .replace("{code.message}", ValidCode.FAILED_4000.getCodeMessage()),
                                ValidCode.FAILED_4000.getCode(), ValidCode.FAILED_4000.name()
                        );
                    }
                })
                .collect(Collectors.toList()));
    }

    private Result<Void> getErrorResult(List<DataErrorMessage> errors) {
        return Result.error(ValidCode.FAILED_4000, errors.toArray(Message[]::new));
    }

}

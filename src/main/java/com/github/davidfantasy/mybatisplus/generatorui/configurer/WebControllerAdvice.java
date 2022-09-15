package com.github.davidfantasy.mybatisplus.generatorui.configurer;

import com.github.davidfantasy.mybatisplus.generatorui.common.Result;
import com.github.davidfantasy.mybatisplus.generatorui.common.ResultCode;
import com.github.davidfantasy.mybatisplus.generatorui.common.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * global exception handling
 */
@ControllerAdvice
@Slf4j
public class WebControllerAdvice {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result exceptionHandler(HttpServletRequest request, Exception e) {
        Result result = new Result();
        log.info("Uncaught exception：" + e.getMessage(), e);
        if (e instanceof ServiceException) {
            result.setCode(ResultCode.FAIL).setMessage(e.getMessage());
        } else if (e instanceof NoHandlerFoundException) {
            result.setCode(ResultCode.NOT_FOUND).setMessage("Interface [" + request.getRequestURI() + "] does not exist");
        } else if (e instanceof ServletException) {
            result.setCode(ResultCode.FAIL).setMessage(e.getMessage());
        } else {
            result.setCode(ResultCode.INTERNAL_SERVER_ERROR).setMessage("An internal system error occurred, please contact the administrator");
            log.error("An internal error occurred in the system, please check the console log for details", e);
        }
        return result;
    }

}

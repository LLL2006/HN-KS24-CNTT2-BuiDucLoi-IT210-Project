package com.re.it210project.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            BadRequestException.class,
            NotFoundException.class,
            UnauthorizedException.class
    })
    public String handleBusinessException(
            RuntimeException exception,
            Model model
    ) {

        model.addAttribute(
                "errorTitle",
                "Lỗi nghiệp vụ"
        );

        model.addAttribute(
                "errorMessage",
                exception.getMessage()
        );

        return "error/500";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(
            Exception exception,
            Model model
    ) {

        exception.printStackTrace();

        model.addAttribute(
                "errorTitle",
                "Lỗi hệ thống"
        );

        model.addAttribute(
                "errorMessage",
                "Đã xảy ra lỗi hệ thống!"
        );

        return "error/500";
    }
}
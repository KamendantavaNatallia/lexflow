package com.lexflow.common.exception;

import jakarta.servlet.ServletException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@ControllerAdvice(annotations = Controller.class)
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e,
                                                 RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/cases";
    }

    @ExceptionHandler(IOException.class)
    public String handleIOException(IOException e,
                                    RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "File operation failed.");
        return "redirect:/cases";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e,
                                              RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "File is too large.");
        return "redirect:/cases";
    }

    @ExceptionHandler(ServletException.class)
    public String handleServletException(ServletException e,
                                         RedirectAttributes redirectAttributes) {
        Throwable rootCause = e.getRootCause();

        if (rootCause instanceof SizeLimitExceededException) {
            redirectAttributes.addFlashAttribute("errorMessage", "File is too large.");
            return "redirect:/cases";
        }

        redirectAttributes.addFlashAttribute("errorMessage", "Request failed.");
        return "redirect:/cases";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception e,
                                         RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "Something went wrong.");
        return "redirect:/cases";
    }
}
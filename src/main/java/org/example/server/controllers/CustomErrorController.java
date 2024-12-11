package org.example.server.controllers;

import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import org.springframework.boot.web.error.ErrorAttributeOptions;

@Controller
public class CustomErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public String handleError(WebRequest webRequest, Model model) {
        Map<String, Object> attributes = errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE));
        int status = (int) attributes.get("status");
        String errorPage = "/admin/error-500"; // Шаблон по умолчанию

        // Выбор шаблона в зависимости от статуса
        if (status == 404) {
            errorPage = "/admin/error-404";
        } else if (status == 500) {
            errorPage = "/admin/error-500";
        }

        model.addAttribute("status", status);
        model.addAttribute("error", attributes.get("error"));
        model.addAttribute("message", attributes.get("message"));

        return errorPage;
    }
}


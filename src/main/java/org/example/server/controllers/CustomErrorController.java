package org.example.server.controllers;

import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
        // Получение информации об ошибке
        Map<String, Object> attributes = errorAttributes.getErrorAttributes(
                webRequest, ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE)
        );
        int status = (int) attributes.get("status");
        String errorPage = "/admin/error-500"; // Шаблон по умолчанию

        // Определение роли пользователя
        String role = getCurrentUserRole();

        // Добавление данных в модель в зависимости от роли
        if ("ADMIN".equals(role)) {
            model.addAttribute("customData", "/admin/");
        } else if ("USER".equals(role)) {
            model.addAttribute("customData", "/en/");
        } else {
            model.addAttribute("customData", "/en/");
        }

        // Выбор шаблона в зависимости от статуса ошибки
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

    // Метод для получения текущей роли пользователя
    private String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                // Возвращаем первую найденную роль (например, ROLE_ADMIN или ROLE_USER)
                return authority.getAuthority().replace("ROLE_", "");
            }
        }
        return null; // Если пользователь не аутентифицирован
    }

}


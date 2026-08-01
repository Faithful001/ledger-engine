package com.king.ledgerengine.shared.interceptor;

import com.king.ledgerengine.shared.response.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class ResponseInterceptor implements ResponseBodyAdvice<Object> {

    private final JsonMapper jsonMapper;

    @Override
    public boolean supports(@NonNull MethodParameter returnType, @NonNull Class converterType) {
        String declaringClassName = returnType.getDeclaringClass().getName();
        return !declaringClassName.startsWith("org.springdoc");
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            @NonNull MethodParameter returnType,
            @NonNull MediaType contentType,
            @NonNull Class converterType,
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response
    ) {
        HttpServletRequest req = ((ServletServerHttpRequest) request).getServletRequest();
        HttpServletResponse res = ((ServletServerHttpResponse) response).getServletResponse();
        int currentStatus = res.getStatus();

        Integer statusOverride = null;
        String message = null;
        Object data;
        Boolean successOverride = null;

        if (body instanceof Response<?> api) {
            statusOverride = api.getStatusCode();
            message = api.getMessage();
            data = api.getData();
            successOverride = api.isSuccess();
        } else {
            data = body;
        }

        int finalStatus = currentStatus;
        if (statusOverride != null && statusOverride >= 400) {
            finalStatus = statusOverride;
            res.setStatus(finalStatus);
        }

        String finalMessage = message != null
                ? message
                : resolveDefaultMessage(finalStatus, req.getMethod());

        boolean finalSuccess = successOverride != null
                ? successOverride
                : finalStatus < 400;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", finalSuccess);
        result.put("statusCode", finalStatus);
        result.put("message", finalMessage);
        result.put("data", data);

        if (StringHttpMessageConverter.class.isAssignableFrom(converterType)) {
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return jsonMapper.writeValueAsString(result);
        }

        return result;
    }

    public String resolveDefaultMessage(int status, String method) {
        if (method.equals("POST") && status == 201) {
            return "Resource created successfully";
        }
        if (status >= 200 && status < 300) {
            return "Request successful";
        }
        return "Request failed";
    }
}
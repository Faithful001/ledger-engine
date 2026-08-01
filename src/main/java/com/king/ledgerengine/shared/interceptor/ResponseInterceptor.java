package com.king.ledgerengine.shared.interceptor;

import com.king.ledgerengine.shared.response.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Map;

@RestControllerAdvice
public class ResponseInterceptor implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(@NonNull MethodParameter returnType, @NonNull Class converterType) {
       return true;
    }

    @Override
    public Map<String, Object> beforeBodyWrite(
            Object body,
            @NonNull MethodParameter returnType,
            @NonNull MediaType contentType,
            @NonNull Class converterType,
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response

    ){
        HttpServletRequest req = ((ServletServerHttpRequest) request).getServletRequest();
        HttpServletResponse res = ((ServletServerHttpResponse) response).getServletResponse();
        int currentStatus = res.getStatus();

        Integer overrideStatus = null;
        String message = null;
        Object data = null;

        if (body instanceof Response<?> api) {
            overrideStatus = api.getStatusCode();
            message = api.getMessage();
            data = api.getData();
        } else {
            data = body;
        }

        int finalStatus = currentStatus;

        if (overrideStatus != null && overrideStatus <= 400) {
            finalStatus = overrideStatus;
            res.setStatus(finalStatus);
        }

        Object finalData = data;

        String finalMessage =
                message != null ?
                message :
                resolveDefaultMessage(finalStatus, req.getMethod());

        return Map.of(
                "statusCode", finalStatus,
                "message", finalMessage,
                "data", finalData
        );
    }

    public String resolveDefaultMessage(int status, String method) {
        if (method.equals("POST") && status == 201){
            return "Resource created successfully";
        }

        if (status >= 200 && status < 300) {
            return "Request successful";
        }

        return "Request failed";
    }
}
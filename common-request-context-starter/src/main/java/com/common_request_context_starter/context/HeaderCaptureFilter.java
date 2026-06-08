package com.common_request_context_starter.context;

import java.io.IOException;
import java.util.Enumeration;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HeaderCaptureFilter extends OncePerRequestFilter{
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        try{
            Enumeration<String> headersName= request.getHeaderNames();

            while(headersName.hasMoreElements()){
                String headerName = headersName.nextElement();
                String headerValue = request.getHeader(headerName);

                RequestContext.setHeader(headerName, headerValue);
            }

            filterChain.doFilter(request, response);
        } finally {
            RequestContext.clear();
        }
    }
}

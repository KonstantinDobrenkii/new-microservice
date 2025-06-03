package com.aholddelhaize.iwmsservice;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LogVariablesFilter implements Filter {

    private static final String MDC_REQUEST_ID_VAR = "reqId";
    private static final String REQUEST_ID_HEADER = "X-RequestId";


    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain)
            throws IOException, ServletException {
        MDC.put(MDC_REQUEST_ID_VAR, ((HttpServletRequest) servletRequest).getHeader(REQUEST_ID_HEADER));
        filterChain.doFilter(servletRequest, servletResponse);
        MDC.remove(MDC_REQUEST_ID_VAR);
    }
}

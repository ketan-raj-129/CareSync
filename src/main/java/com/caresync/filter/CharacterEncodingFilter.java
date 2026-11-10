package com.caresync.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;

/**
 * Filter that enforces UTF-8 character encoding across all incoming HTTP
 * requests and outgoing HTTP responses.
 */
public class CharacterEncodingFilter implements Filter {

    private String encoding = "UTF-8";
    private boolean forceEncoding = true;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String configuredEncoding = filterConfig.getInitParameter("encoding");
        if (configuredEncoding != null && !configuredEncoding.trim().isEmpty()) {
            this.encoding = configuredEncoding.trim();
        }
        String configuredForce = filterConfig.getInitParameter("forceEncoding");
        if (configuredForce != null) {
            this.forceEncoding = Boolean.parseBoolean(configuredForce.trim());
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (this.forceEncoding || request.getCharacterEncoding() == null) {
            request.setCharacterEncoding(this.encoding);
        }
        if (this.forceEncoding || response.getCharacterEncoding() == null) {
            response.setCharacterEncoding(this.encoding);
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No cleanup required
    }
}


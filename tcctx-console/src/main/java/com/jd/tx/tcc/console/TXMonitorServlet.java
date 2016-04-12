package com.jd.tx.tcc.console;

import com.jd.tx.tcc.core.utils.Utils;
import com.jd.tx.tcc.service.TransactionQueryService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Leon Guo
 *         Creation Date: 2016/4/8
 */
public class TXMonitorServlet extends HttpServlet {

    protected final String resourcePath;

    protected final static String REST_PREFIX = "/rest/";

    protected final static String PARAM_QUERY_BEAN_NAME = "queryBeanName";

    private String queryBeanName;

    public TXMonitorServlet(){
        this.resourcePath = "console/http";
    }

    @Override
    public void init() throws ServletException {
        queryBeanName = getInitParameter(PARAM_QUERY_BEAN_NAME);
        Validate.notEmpty(queryBeanName);
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String contextPath = request.getContextPath();
        String servletPath = request.getServletPath();
        String requestURI = request.getRequestURI();

        response.setCharacterEncoding("utf-8");

        if (contextPath == null) { // root context
            contextPath = "";
        }
        String uri = contextPath + servletPath;
        String path = requestURI.substring(contextPath.length() + servletPath.length());

        if (path.startsWith(REST_PREFIX) && path.length() > REST_PREFIX.length()) {
            String resource = path.substring(REST_PREFIX.length());
            TransactionQueryService queryService = getQueryService(request);
            if ("dataSourceKey".equals(resource)) {
                response.getWriter().write(queryService.queryDataSourceKeys());
                return;
            } else if ("transactionEntity".equals(resource)) {
                response.getWriter().write(queryService.queryTransactionEntities(
                        request.getParameter("dataSourceKey"), request.getParameter("lastId")));
                return;
            }
        }
        if (path.startsWith("/index.html") || StringUtils.isBlank(path) || "/".equals(path)) {
            returnResourceFile("/index.html", uri, response);
            return;
        }
        returnResourceFile(path, uri, response);
    }

    protected String getFilePath(String fileName) {
        return resourcePath + fileName;
    }

    protected void returnResourceFile(String fileName, String uri, HttpServletResponse response)
            throws ServletException,
            IOException {

        String filePath = getFilePath(fileName);

        if (filePath.endsWith(".html")) {
            response.setContentType("text/html; charset=utf-8");
        }
        if (fileName.endsWith(".jpg")) {
            byte[] bytes = Utils.readByteArrayFromResource(filePath);
            if (bytes != null) {
                response.getOutputStream().write(bytes);
            }

            return;
        }

        String text = Utils.readFromResource(filePath);
        if (text == null) {
            response.sendRedirect(uri + "/index.html");
            return;
        }
        if (fileName.endsWith(".css")) {
            response.setContentType("text/css;charset=utf-8");
        } else if (fileName.endsWith(".js")) {
            response.setContentType("text/javascript;charset=utf-8");
        }
        response.getWriter().write(text);
    }


    protected TransactionQueryService getQueryService(HttpServletRequest request) {
        return (TransactionQueryService) getSpringContext(request).getBean(queryBeanName);
    }

    protected WebApplicationContext getSpringContext(HttpServletRequest request) {
        ServletContext context = request.getSession().getServletContext();
        WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(context);
        return wac;
    }

}

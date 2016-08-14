package org.wilson.storage.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.wilson.storage.util.CommonUtils;

public class SecurityServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(SecurityServlet.class);
    
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String command = req.getParameter("command");
        if(command == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("Failed to find command");
            logger.error("Failed to find command");
            return;
        }
        
        if("initKey".equals(command)) {
            this.initKey(req, resp);
        }
        else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("Invalid command");
            logger.error("Invalid command");
            return;
        }
    }
    
    private void initKey(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String key = req.getParameter("key");
        if(key == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("Failed to find key");
            logger.error("Failed to find key");
            return;
        }
        
        boolean ret = CommonUtils.initKey(key);
        if(!ret) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("Key already exists");
            return;
        }
        else {
            resp.getWriter().print("Init key success");
            return;
        }
    }
}

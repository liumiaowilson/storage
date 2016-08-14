package org.wilson.storage.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.wilson.storage.util.CommonUtils;

public class FileServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(FileServlet.class);
    
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
        
        String key = req.getParameter("key");
        String realKey = CommonUtils.getKey();
        if(realKey == null) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().print("Key is not set");
            return;
        }
        if(!realKey.equals(key)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().print("Key is invalid");
            return;
        }
        
        if("create".equals(command)) {
            this.create(req, resp);
        }
        else if("list".equals(command)) {
            this.list(req, resp);
        }
        else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("Invalid command");
            logger.error("Invalid command");
            return;
        }
    }
    
    private void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getParameter("path");
        if(path == null) {
            path = "";
        }
        
        File file = new File(CommonUtils.getFilesDir() + path);
        File [] files = file.listFiles();
        if(files != null) {
            for(File f : files) {
                resp.getWriter().println(f.getName());
            }
        }
    }
    
    private void create(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getParameter("path");
        if(path == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("Path is not set");
            return;
        }
        
        String url = req.getParameter("url");
        if(url == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("Url is not set");
            return;
        }
        
        path = CommonUtils.getFilesDir() + path;
        File file = new File(path);
        File parent = file.getParentFile();
        if(!parent.exists()) {
            parent.mkdirs();
        }

        URL website = new URL(url);
        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        FileOutputStream fos = new FileOutputStream(path);
        try {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
        finally {
            fos.close();
        }
        
        resp.getWriter().print("File created successfully");
    }
}

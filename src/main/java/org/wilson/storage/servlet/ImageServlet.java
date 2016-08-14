package org.wilson.storage.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.wilson.storage.util.CommonUtils;

public class ImageServlet extends HttpServlet{
    private static final long serialVersionUID = 1L;

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("image/jpeg");
        
        boolean showImage = true;
        
        String key = request.getParameter("key");
        String realKey = CommonUtils.getKey();
        if(realKey == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().print("Key is not set");
            return;
        }
        if(!realKey.equals(key)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().print("Key is invalid");
            return;
        }
        
        String path = request.getParameter("path");
        if(path == null || "".equals(path.trim())) {
            showImage = false;
        }
        else {
            path = path.trim();
        }
        
        ServletOutputStream out = response.getOutputStream();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("image_not_found.png");
        
        if(showImage) {
            File imageFile = new File(CommonUtils.getFilesDir() + path);
            if(imageFile.exists()) {
                is = new FileInputStream(imageFile);
            }
        }
          
        BufferedInputStream bin = new BufferedInputStream(is);  
        BufferedOutputStream bout = new BufferedOutputStream(out);  
        int ch =0; ;  
        while((ch=bin.read())!=-1)  
        {  
            bout.write(ch);  
        }  
          
        bin.close();  
        bout.close();  
    }
}

package org.wilson.storage.servlet;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
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
        boolean resize = false;
        
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
        
        String widthStr = request.getParameter("width");
        String heightStr = request.getParameter("height");
        String adjustStr = request.getParameter("adjust");
        int width = 0;
        int height = 0;
        boolean adjust = false;
        try {
        	if(widthStr != null && heightStr != null) {
        		width = Integer.parseInt(widthStr);
        		height = Integer.parseInt(heightStr);
        		resize = true;
        	}
        	if(adjustStr != null) {
        		adjust = Boolean.parseBoolean(adjustStr);
        	}
        }
        catch(Exception e) {
        }
        
        ServletOutputStream out = response.getOutputStream();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("image_not_found.png");
        
        if(showImage) {
            File imageFile = new File(CommonUtils.getFilesDir() + path);
            if(imageFile.exists()) {
            	if(resize) {
            		BufferedImage originalImage = ImageIO.read(imageFile);
                	int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
                	int originalWidth = originalImage.getWidth();
                	int originalHeight = originalImage.getHeight();
                	if(adjust) {
                		double ratio = originalWidth * 1.0 / originalHeight;
                		int new_width = (int) (height * ratio);
                		if(new_width > width) {
                			height = (int) (width / ratio);
                		}
                		else{
                			width = new_width;
                		}
                	}
                	BufferedImage resizedImage = this.resizeImage(originalImage, type, width, height);
                	ByteArrayOutputStream baos = new ByteArrayOutputStream();
                	ImageIO.write(resizedImage, "jpg", baos);
                	is = new ByteArrayInputStream(baos.toByteArray());
            	}
            	else {
            		is = new FileInputStream(imageFile);
            	}
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
    
    private BufferedImage resizeImage(BufferedImage originalImage, int type, int width, int height){
    	BufferedImage resizedImage = new BufferedImage(width, height, type);
    	Graphics2D g = resizedImage.createGraphics();
    	g.drawImage(originalImage, 0, 0, width, height, null);
    	g.dispose();

    	return resizedImage;
    }
}

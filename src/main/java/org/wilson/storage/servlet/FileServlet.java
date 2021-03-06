package org.wilson.storage.servlet;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
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
        String key = req.getParameter("key");
        String realKey = CommonUtils.getKey();
        if(realKey == null) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().print("[ERROR]Key is not set");
            return;
        }
        if(!realKey.equals(key)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().print("[ERROR]Key is invalid");
            return;
        }
        
        String command = req.getParameter("command");
        if(command == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("[ERROR]Failed to find command");
            logger.error("Failed to find command");
            return;
        }
        
        if("create".equals(command)) {
            this.create(req, resp);
        }
        else if("list".equals(command)) {
            this.list(req, resp);
        }
        else if("delete".equals(command)) {
            this.delete(req, resp);
        }
        else if("execute".equals(command)) {
            this.execute(req, resp);
        }
        else if("get".equals(command)) {
            this.get(req, resp);
        }
        else if("download".equals(command)) {
        	this.download(req, resp);
        }
        else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("Invalid command");
            logger.error("[ERROR]Invalid command");
            return;
        }
    }
    
    private List<File> getAllFiles(File file) {
    	List<File> files = new ArrayList<File>();
    	for(File f : file.listFiles()) {
    		if(f.isDirectory()) {
    			files.addAll(this.getAllFiles(f));
    		}
    		else {
    			files.add(f);
    		}
    	}
    	
    	return files;
    }
    
    private String getPath(File file, String root) {
    	String absPath = file.getAbsolutePath();
    	String path = absPath.substring(root.length());
    	
    	return path;
    }
    
    private void download(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	try {
            String dir = CommonUtils.getFilesDir();
            File dirFile = new File(dir);
            dir = dirFile.getAbsolutePath();
            List<File> files = this.getAllFiles(dirFile);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);
            byte [] bytes = new byte[2048];
            for(File file : files) {
            	String path = this.getPath(file, dir);
            	FileInputStream fis = new FileInputStream(file);
        		BufferedInputStream bis = new BufferedInputStream(fis);
        		
        		if(path.startsWith("/")) {
        			path = path.substring(1);
        		}
        		zos.putNextEntry(new ZipEntry(path));
        		int bytesRead;
                while ((bytesRead = bis.read(bytes)) != -1) {
                    zos.write(bytes, 0, bytesRead);
                }
                zos.closeEntry();
                
                bis.close();
                fis.close();
            }
            
            zos.flush();
            baos.flush();
            zos.close();
            baos.close();
            
            byte [] zip = baos.toByteArray();

            ServletOutputStream sos = resp.getOutputStream();
            resp.setContentType("application/zip");
            resp.setHeader("Content-Disposition", "attachment; filename=Data.zip");

            sos.write(zip);
            sos.flush();
        }
        catch (Exception e) {
            logger.error(e);
        }
    }
    
    private void get(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getParameter("path");
        if(path == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("[ERROR]Path is not set");
            return;
        }
        
        File file = new File(CommonUtils.getFilesDir() + path);
        if(file.exists()) {
            InputStream in = new FileInputStream(file);
            OutputStream out = resp.getOutputStream();
            
            byte [] buffer = new byte[1024];
            while(true) {
                int read = in.read(buffer);
                if(read < 0) {
                    break;
                }
                out.write(buffer, 0, read);
            }
            out.flush();
            
            in.close();
        }
        else {
            resp.getWriter().print("[ERROR]File does not exist");
        }
    }
    
    private void execute(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String cmd = req.getParameter("cmd");
        if(cmd == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("Cmd is not set");
            return;
        }
        
        Scanner s = null;
        try {
            InputStream is = Runtime.getRuntime().exec(cmd).getInputStream();
            s = new Scanner(is);
            s.useDelimiter("\\A");
            resp.getWriter().print(s.next());
        }
        catch(Exception e) {
            logger.error("failed to run command!", e);
            resp.getWriter().print("[ERROR]" + e.getMessage());
        }
        finally {
            if(s != null) {
                s.close();
            }
        }
    }
    
    private void delete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getParameter("path");
        if(path == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("[ERROR]Path is not set");
            return;
        }
        
        File file = new File(CommonUtils.getFilesDir() + path);
        if(file.exists()) {
            file.delete();
            resp.getWriter().print("File has been successfully deleted");
        }
        else {
            resp.getWriter().print("[ERROR]File does not exist");
        }
    }
    
    private void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getParameter("path");
        if(path == null) {
            path = "";
        }
        
        File file = new File(CommonUtils.getFilesDir() + path);
        List<File> files = new ArrayList<File>();
        this.listFiles(file, files);
        
        String prefix = file.getAbsolutePath();
        for(File f : files) {
            String name = f.getAbsolutePath();
            name = name.substring(prefix.length());
            resp.getWriter().println(name);
        }
    }
    
    private void listFiles(File root, List<File> files) {
        if(root.isFile()) {
            files.add(root);
        }
        else {
            for(File file : root.listFiles()) {
                listFiles(file, files);
            }
        }
    }
    
    private void create(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getParameter("path");
        if(path == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("[ERROR]Path is not set");
            return;
        }
        
        String url = req.getParameter("url");
        if(url == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("[ERROR]Url is not set");
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

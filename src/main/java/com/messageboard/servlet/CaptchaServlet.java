package com.messageboard.servlet;

import com.messageboard.util.CaptchaUtil;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

public class CaptchaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("image/jpeg");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        // Generate captcha text
        String captchaText = CaptchaUtil.generateCaptchaText(4);
        
        // Store in session
        HttpSession session = request.getSession();
        session.setAttribute("captcha", captchaText);

        // Generate image
        BufferedImage image = CaptchaUtil.generateCaptchaImage(captchaText);

        // Write image to response
        OutputStream os = response.getOutputStream();
        ImageIO.write(image, "JPEG", os);
        os.flush();
        os.close();
    }
}

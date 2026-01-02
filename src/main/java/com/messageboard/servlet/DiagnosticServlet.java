package com.messageboard.servlet;

import com.messageboard.dao.MessageDAO;
import com.messageboard.dao.UserDAO;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class DiagnosticServlet extends HttpServlet {
    private Gson gson = new Gson();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            UserDAO userDAO = new UserDAO();
            MessageDAO messageDAO = new MessageDAO();
            
            int userCount = userDAO.getTotalCount();
            int messageCount = messageDAO.getTotalCount();
            
            result.put("success", true);
            result.put("userCount", userCount);
            result.put("messageCount", messageCount);
            result.put("message", "数据库连接正常");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("stackTrace", e.getClass().getName());
        }
        
        out.print(gson.toJson(result));
        out.flush();
        out.close();
    }
}

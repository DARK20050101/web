package com.messageboard.web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * LogoutServlet - Handles user logout
 */
@WebServlet("/api/logout")
public class LogoutServlet extends HttpServlet {
    private Gson gson;

    @Override
    public void init() throws ServletException {
        gson = new Gson();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        JsonObject result = new JsonObject();
        result.addProperty("success", true);
        result.addProperty("message", "Logout successful");
        response.getWriter().write(gson.toJson(result));
    }
}

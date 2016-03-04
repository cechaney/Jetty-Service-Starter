package com.cec.jettyweb.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HelloServlet extends javax.servlet.http.HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "HelloServlet";
	public static final String PATH = "/hello";
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	
		resp.getWriter().println("Hello!");
	}

}

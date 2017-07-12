package fhirtb;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AuthorizationFilter implements Filter {
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpSession session = req.getSession();

		if (session.getAttribute("logged") != null || req.getRequestURI().endsWith("login.xhtml")) {
			chain.doFilter(request, response);
			System.out.println("Filter passed if clause");
		} else {
			System.out.println("filter in else clause");
			HttpServletResponse res = (HttpServletResponse) response;
			String contextPath = ((HttpServletRequest) request).getContextPath();
			res.sendRedirect(contextPath + "/login.jsf");
			return;
		}

	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void destroy() {
	}

}
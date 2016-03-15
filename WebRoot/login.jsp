<%@ page language="java" errorPage="/error.jsp"
	contentType="text/html; charset=UTF-8"%>
<%@page import="org.androidpn.server.BasicLoginManager"%>
<%@ include file="/includes/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title>Login</title>
</head>
<body>
<%
	response.setContentType("text/html;charset=UTF-8");
	String username = (String) session.getAttribute("username");
	String password = (String) session.getAttribute("password");
	String path=request.getContextPath();  
	if (username != null && password != null) {
		//表示已经登录
	    response.sendRedirect(path);
	} else {
		String authorization = request.getHeader("Authorization");
		String validateCode = BasicLoginManager.validate(authorization);

		if (validateCode != null) {
			username = validateCode.split(":")[0];
			password = validateCode.split(":")[1];
			session.setAttribute("username", username);
			session.setAttribute("password", password);
		    response.sendRedirect(path);
		} else {
			//到这里，就要登录了，修改头文件,并且让用户登录
			String authHeadKey = "WWW-Authenticate";
			String chineseCode = "ANDROID NOTIFICATION PUSH";
			String authHeadVal = "Basic realm=\""
					+ new String(chineseCode.getBytes(), "UTF-8")
					+ "\"";

			response.setHeader(authHeadKey, authHeadVal);
			response.sendError(401,
					"you must login with username and password");
		}

	}
%>

</body>
</html>

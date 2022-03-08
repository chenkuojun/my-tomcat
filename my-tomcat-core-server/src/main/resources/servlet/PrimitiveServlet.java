import javax.servlet.*;
import java.io.IOException;
import java.io.PrintWriter;

public class PrimitiveServlet implements Servlet {

  public void init(ServletConfig config) throws ServletException {
    System.out.println("init");
  }

  public void service(ServletRequest request, ServletResponse response)
    throws ServletException, IOException {
    String result = "Hello. Roses are red.";
//    PrintWriter out = response.getWriter();
//    out.println("Hello. Roses are red.");
//    out.print("Violets are blue.");
//    System.out.println("from service");
    PrintWriter out = response.getWriter();
    out.println("HTTP/1.1 200 OK");// 返回应答消息,并结束应答
//    out.println("Content-Type:text/plain ");
    out.println("Content-Length:" + result.length());
    out.println();// 根据 HTTP 协议, 空行将结束头信息
    out.println(result);
    out.println("Violets are blue.");
    out.close();
  }

  public void destroy() {
    System.out.println("destroy");
  }

  public String getServletInfo() {
    return null;
  }
  public ServletConfig getServletConfig() {
    return null;
  }

}

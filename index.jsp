<%
// This little sequence allows index.jsp to turn into index.html
// this is necessary because index.html doesn't really exist, and
// the server needs to have a welcome file or it'll do a directory
// listing
String q = request.getQueryString();
if( q != null ){
    q = "?" + q;
}
else{
    q = "";
}
String c = request.getContextPath();
response.sendRedirect( c + "/service/DailySummary.html" + q );
//response.sendRedirect( "/" );
%>

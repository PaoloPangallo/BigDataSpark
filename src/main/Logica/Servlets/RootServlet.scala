package Servlets

import jakarta.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

class RootServlet extends HttpServlet {
  override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    resp.setContentType("text/html")
    resp.setStatus(HttpServletResponse.SC_OK)
    resp.getWriter.write(
      """
        |<html>
        |<head>
        |  <title>Chess Analysis Server</title>
        |  <style>
        |    body {
        |      font-family: Arial, sans-serif;
        |      background-color: #f4f4f9;
        |      color: #333;
        |      margin: 0;
        |      padding: 0;
        |    }
        |    header {
        |      background-color: #6200ea;
        |      color: white;
        |      padding: 20px;
        |      text-align: center;
        |    }
        |    main {
        |      padding: 20px;
        |      max-width: 800px;
        |      margin: auto;
        |      background: white;
        |      border-radius: 8px;
        |      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
        |    }
        |    ul {
        |      list-style: none;
        |      padding: 0;
        |    }
        |    ul li {
        |      margin: 10px 0;
        |    }
        |    ul li a {
        |      text-decoration: none;
        |      color: #6200ea;
        |      font-weight: bold;
        |    }
        |    ul li a:hover {
        |      text-decoration: underline;
        |    }
        |  </style>
        |</head>
        |<body>
        |  <header>
        |    <h1>Benvenuto nel server di analisi scacchi</h1>
        |  </header>
        |  <main>
        |    <p>Usa uno dei seguenti endpoint per accedere ai dati:</p>
        |    <ul>
        |      <li><a href="/resultCounts">Vittorie per colore</a></li>
        |      <li><a href="/popularOpenings">Aperture più popolari</a></li>
        |      <li><a href="/avgRatingDiff">Differenze medie di rating</a></li>
        |    </ul>
        |  </main>
        |</body>
        |</html>
      """.stripMargin
    )
  }
}

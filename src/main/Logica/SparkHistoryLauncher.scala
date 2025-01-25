import java.io.File

object SparkHistoryLauncher {
  def main(args: Array[String]): Unit = {
    val sparkHome = "C:\\Spark\\spark-3.4.4-bin-hadoop3"

    val command = s"$sparkHome\\sbin\\start-history-server.sh"

    try {
      println(s"Eseguendo il comando: $command")
      val process = new ProcessBuilder("cmd.exe", "/c", command)
        .directory(new File(sparkHome))
        .inheritIO()
        .start()

      process.waitFor()
      println("Spark History Server avviato con successo.")
    } catch {
      case e: Exception =>
        e.printStackTrace()
        println("Errore durante l'avvio dello Spark History Server.")
    }
  }
}

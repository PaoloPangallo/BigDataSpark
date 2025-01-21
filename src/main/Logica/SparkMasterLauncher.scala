import java.io.File

object SparkMasterLauncher {
  def main(args: Array[String]): Unit = {
    // Percorso della directory di Spark
    val sparkHome = "C:\\Spark\\spark-3.4.4-bin-hadoop3"

    // Comando per avviare il Master
    val command = s"$sparkHome\\bin\\spark-class org.apache.spark.deploy.master.Master --host 127.0.0.1 --port 7077 --webui-port 8080"

    try {
      // Avvia il processo
      println(s"Eseguendo il comando: $command")
      val process = new ProcessBuilder("cmd.exe", "/c", command)
        .directory(new File(sparkHome))
        .inheritIO()
        .start()

      // Attendi la fine del processo
      process.waitFor()
      println("Master Spark terminato.")
    } catch {
      case e: Exception =>
        e.printStackTrace()
        println("Errore durante l'avvio del Master Spark.")
    }
  }
}

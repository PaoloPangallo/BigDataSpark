package Logica

import java.io.IOException
import scala.sys.process._

object SparkWorkerLauncher {
  def main(args: Array[String]): Unit = {
    // Configurazione del percorso di Spark e parametri del Worker
    val sparkHome = "C:/Spark/spark-3.4.4-bin-hadoop3" // Percorso di Spark
    val masterUrl = "spark://127.0.0.1:7077" // URL del Master
    val workerHost = "127.0.0.1" // Indirizzo del Worker
    val workerPort = "8881" // Porta per il Worker
    val webUiPort = "8081" // Porta per la Web UI del Worker

    // Costruzione del comando per avviare il Worker
    val command = Seq(
      s"$sparkHome/bin/spark-class.cmd",
      "org.apache.spark.deploy.worker.Worker",
      "--host", workerHost,
      "--port", workerPort,
      "--webui-port", webUiPort,
      masterUrl
    )

    println(s"Eseguendo il Worker con comando: ${command.mkString(" ")}")

    try {
      // Avvia il Worker come processo esterno
      val processBuilder = Process(command)
      val process = processBuilder.run()

      // Stampa messaggi utili
      println("Worker avviato con successo!")
      println("Premi Ctrl+C per terminare...")

      // Usa waitFor() per mantenere il programma in esecuzione fino a che il processo è attivo
      val exitCode = process.exitValue() // Ora funziona perché il processo è già stato eseguito
      println(s"Il processo Worker si è chiuso con codice di uscita: $exitCode")
    } catch {
      case ex: IOException =>
        println(s"Errore durante l'avvio del Worker: ${ex.getMessage}")
      case ex: Exception =>
        println(s"Errore imprevisto: ${ex.getMessage}")
    }
  }
}

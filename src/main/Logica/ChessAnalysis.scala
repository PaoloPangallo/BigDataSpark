import Services._
import Utils.DataLoader
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{Dataset, Row, SparkSession}

import java.nio.file.{Files, Paths}
import java.util.concurrent.ForkJoinPool
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import org.apache.commons.io.FileUtils

import java.io.File
import scala.language.reflectiveCalls

object ChessAnalysis {

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(new ForkJoinPool(4))

  def main(args: Array[String]): Unit = {
    configureLogging()
    val spark = createSparkSession()

    val filePath = "C:/Users/paolo/Desktop/Big Data/chess_games.csv/chess_games.csv"
    val outputDir = "C:/Users/paolo/Desktop/ProgettoSparkScacchi/Output"
    ensureOutputDirectory(outputDir)
    println(s"Spark Version: ${spark.version}")

    try {
      val chessData = DataLoader.loadDataset(spark, filePath)
      println(s"Numero di record caricati: ${chessData.count()}")

      val performanceReport = new StringBuilder()
      val (_, totalTime) = measureExecutionTime("Intera analisi") {

        val tasks = Seq(
          runAnalysis("ResultAnalyzer", outputDir, chessData, new ResultAnalyzer()),
          runAnalysis("OpeningAnalyzer", outputDir, chessData, new OpeningAnalyzer()),
          runAnalysis("RatingAnalyzer", outputDir, chessData, new RatingAnalyzer()),
          runAnalysis("SpecificQueryGenerator", outputDir, chessData, new SpecificQueryGenerator()),
          runAnalysis("MVPStatistics", outputDir, chessData, new MVPStatistics()),
          runAnalysis("DateStatistics", outputDir, chessData, new DateStatistics()),
          runAnalysis("StatisticsGenerator", outputDir, chessData, new StatisticsGenerator()),
          runAnalysis("TerminationQueryGenerator", outputDir, chessData, new TerminationQueryGenerator()) // <-- Aggiunto qui




        )

        // Aspetta il completamento di tutte le analisi parallele
        Await.result(Future.sequence(tasks), Duration.Inf)
      }

      savePerformanceReport(performanceReport, totalTime, outputDir)

    } finally {
      spark.stop()
    }
  }

  /** Esegue un'analisi in parallelo con gestione sicura della directory di output */
  private def runAnalysis(name: String, baseOutputDir: String, data: Dataset[Row], analyzer: { def analyze(data: Dataset[Row], outputDir: String): Unit }): Future[Unit] = {
    Future {
      val outputDir = s"$baseOutputDir/$name"
      ensureOutputDirectory(outputDir)

      Try(measureExecutionTime(name) {
        analyzer.analyze(data, outputDir)
      }) match {
        case Success(_) => println(s"$name completato con successo.")
        case Failure(e) => println(s"Errore in $name: ${e.getMessage}")
      }
    }
  }

  /** Misura il tempo di esecuzione di un task */

  private def measureExecutionTime[T](taskName: String)(block: => T): (T, Double) = {
    val startTime = System.nanoTime()
    val result = block
    val duration = (System.nanoTime() - startTime) / 1e9
    println(s"Tempo di esecuzione per $taskName: $duration secondi")
    (result, duration)
  }

  /** Salva il report delle performance */
  private def savePerformanceReport(report: StringBuilder, totalTime: Double, outputDir: String): Unit = {
    report.insert(0, s"Analisi completata in $totalTime secondi\n")
    val reportPath = Paths.get(outputDir, "performance_report.txt")
    Files.write(reportPath, report.toString().getBytes)
    println(s"Report delle performance salvato in: $reportPath")
  }

  /** Configura il logging */
  private def configureLogging(): Unit = {
    Logger.getLogger("org.apache").setLevel(Level.WARN)
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
  }

  /** Crea la sessione Spark con ottimizzazioni */
  private def createSparkSession(): SparkSession = {
    SparkSession.builder()
      .appName("Chess Analysis")
      .master("local[*]")
      .config("spark.driver.host", "127.0.0.1")
      .config("spark.executor.memory", "6g")
      .config("spark.executor.cores", "4")
      .config("spark.driver.memory", "6g")
      .config("spark.eventLog.enabled", "true")
      .config("spark.eventLog.dir", "/tmp/spark-events")
      .config("spark.sql.shuffle.partitions", "4") // Numero ridotto per meno overhead
      .config("spark.sql.autoBroadcastJoinThreshold", -1) // Evita problemi di broadcast join
      .getOrCreate()
  }

  /** Assicura che la directory di output sia pronta e svuota il contenuto */
  private def ensureOutputDirectory(outputDir: String): Unit = {
    val dir = new File(outputDir)

    if (dir.exists()) {
      Try(FileUtils.cleanDirectory(dir)) match {
        case Failure(e) => println(s"Errore durante la pulizia della directory $outputDir: ${e.getMessage}")
        case _ => ()
      }
    } else {
      dir.mkdirs()
    }
  }
}

import Services._
import Utils.FileUtils
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

import java.io.{File, IOException}
import java.nio.file.{Files, Paths}

object ChessAnalysis {

  def main(args: Array[String]): Unit = {
    configureLogging()
    val spark = createSparkSession()

    val filePath = "C:/Users/paolo/Desktop/Big Data/chess_games.csv/chess_games.csv"
    val outputDir = "C:/Users/paolo/Desktop/ProgettoSparkScacchi/Output"
    ensureOutputDirectory(outputDir)
    println(s"Spark Version: ${spark.version}")

    try {
      val chessData = DataLoader.loadDataset(spark, filePath)

      chessData.printSchema()

      val performanceReport = new StringBuilder()
      val (_, totalTime) = measureExecutionTime("Intera analisi", performanceReport) {
        // ResultAnalyzer
        measureExecutionTime("ResultAnalyzer", performanceReport) {
          val resultAnalyzer = new ResultAnalyzer()
          resultAnalyzer.analyze(chessData, outputDir)
        }

        // OpeningAnalyzer
        measureExecutionTime("OpeningAnalyzer", performanceReport) {
          val openingAnalyzer = new OpeningAnalyzer()
          openingAnalyzer.analyze(chessData, outputDir)
        }

        // RatingAnalyzer
        measureExecutionTime("RatingAnalyzer", performanceReport) {
          val ratingAnalyzer = new RatingAnalyzer()
          ratingAnalyzer.analyze(chessData, outputDir)
        }

        // StatisticsGenerator
        measureExecutionTime("StatisticsGenerator", performanceReport) {
          val statisticsGenerator = new StatisticsGenerator()
          statisticsGenerator.generate(chessData, outputDir)
        }

        // MVPStatistics
        measureExecutionTime("MVPStatistics", performanceReport) {
          val mvpStatistics = new MVPStatistics()
          mvpStatistics.analyze(chessData, outputDir)
        }

        // DateStatistics
        measureExecutionTime("DateStatistics", performanceReport) {
          val dateStatistics = new DateStatistics()
          dateStatistics.analyze(chessData, outputDir)
        }
      }

      // Salva il report delle performance
      savePerformanceReport(performanceReport, totalTime, outputDir)

      println(s"Risultati salvati in CSV nella directory: $outputDir")
      Thread.sleep(5 * 60 * 1000)

    } finally {
      spark.stop()
    }
  }

  private def measureExecutionTime[T](taskName: String, report: StringBuilder = null)(block: => T): (T, Double) = {
    val startTime = System.nanoTime()
    val result = block
    val endTime = System.nanoTime()
    val duration = (endTime - startTime) / 1e9
    println(s"Tempo di esecuzione per $taskName: $duration secondi")
    if (report != null) {
      report.append(s" - $taskName: $duration secondi\n")
    }
    (result, duration)
  }

  private def savePerformanceReport(report: StringBuilder, totalTime: Double, outputDir: String): Unit = {
    report.insert(0, s"Analisi completata in $totalTime secondi\n")
    val reportPath = Paths.get(outputDir, "performance_report.txt")
    Files.write(reportPath, report.toString().getBytes)
    println(s"Report delle performance salvato in: $reportPath")
  }

  private def configureLogging(): Unit = {
    Logger.getLogger("org.apache").setLevel(Level.WARN)
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
  }

  private def createSparkSession(): SparkSession = {
    SparkSession.builder()
      .appName("Chess Analysis")
      .master("local[*]")
      .config("spark.driver.host", "127.0.0.1")
      .config("spark.executor.memory", "2g")
      .config("spark.executor.cores", "2")
      .config("spark.driver.memory", "2g")
      .config("spark.eventLog.enabled", "true")
      .config("spark.eventLog.dir", "/tmp/spark-events")
      .getOrCreate()
  }

  private def ensureOutputDirectory(outputDir: String): Unit = {
    val dir = new File(outputDir)
    if (dir.exists()) {
      FileUtils.deleteDirectory(dir)
    }
    if (!dir.mkdirs()) {
      throw new IOException(s"Errore nella creazione della directory: $outputDir")
    }
  }
}

import Services._
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

import java.nio.file.{Files, Paths}
import java.util.UUID
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

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
      println(s"Numero di record caricati: ${chessData.count()}")

      val performanceReport = new StringBuilder()
      val (_, totalTime) = measureExecutionTime("Intera analisi") {
        // Future per analisi parallele
        val resultFuture = Future {
          val resultOutputDir = s"$outputDir/ResultAnalyzer_${UUID.randomUUID()}"
          ensureOutputDirectory(resultOutputDir)
          measureExecutionTime("ResultAnalyzer") {
            val resultAnalyzer = new ResultAnalyzer()
            resultAnalyzer.analyze(chessData, resultOutputDir)
          }
        }

        val openingFuture = Future {
          val openingOutputDir = s"$outputDir/OpeningAnalyzer_${UUID.randomUUID()}"
          ensureOutputDirectory(openingOutputDir)
          measureExecutionTime("OpeningAnalyzer") {
            val openingAnalyzer = new OpeningAnalyzer()
            openingAnalyzer.analyze(chessData, openingOutputDir)
          }
        }

        val ratingFuture = Future {
          val ratingOutputDir = s"$outputDir/RatingAnalyzer_${UUID.randomUUID()}"
          ensureOutputDirectory(ratingOutputDir)
          measureExecutionTime("RatingAnalyzer") {
            val ratingAnalyzer = new RatingAnalyzer()
            ratingAnalyzer.analyze(chessData, ratingOutputDir)
          }
        }

        // **SpecificQueryGenerator Integration**
        val specificQueryFuture = Future {
          val specificQueryOutputDir = s"$outputDir/SpecificQueryGenerator_${UUID.randomUUID()}"
          ensureOutputDirectory(specificQueryOutputDir)
          measureExecutionTime("SpecificQueryGenerator") {
            val specificQueryGenerator = new SpecificQueryGenerator()
            specificQueryGenerator.generate(chessData, specificQueryOutputDir)
          }
        }

        // Analisi sequenziali
        measureExecutionTime("StatisticsGenerator") {
          val statisticsGenerator = new StatisticsGenerator()
          statisticsGenerator.generate(chessData, outputDir)
        }

        measureExecutionTime("MVPStatistics") {
          val mvpStatistics = new MVPStatistics()
          mvpStatistics.analyze(chessData, outputDir)
        }

        measureExecutionTime("DateStatistics") {
          val dateStatistics = new DateStatistics()
          dateStatistics.analyze(chessData, outputDir)
        }

        // Aspetta il completamento delle Future parallele
        Await.result(Future.sequence(Seq(resultFuture, openingFuture, ratingFuture, specificQueryFuture)), Duration.Inf)
      }

      // Salva il report delle performance
      savePerformanceReport(performanceReport, totalTime, outputDir)
    } finally {
      spark.stop()
    }
  }

  // Misura il tempo di esecuzione di un task
  private def measureExecutionTime[T](taskName: String)(block: => T): (T, Double) = {
    val startTime = System.nanoTime()
    val result = block
    val duration = (System.nanoTime() - startTime) / 1e9
    println(s"Tempo di esecuzione per $taskName: $duration secondi")
    (result, duration)
  }

  // Salva il report delle performance
  private def savePerformanceReport(report: StringBuilder, totalTime: Double, outputDir: String): Unit = {
    report.insert(0, s"Analisi completata in $totalTime secondi\n")
    val reportPath = Paths.get(outputDir, "performance_report.txt")
    Files.write(reportPath, report.toString().getBytes)
    println(s"Report delle performance salvato in: $reportPath")
  }

  // Configura il logging
  private def configureLogging(): Unit = {
    Logger.getLogger("org.apache").setLevel(Level.WARN)
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
  }

  // Crea la sessione Spark
  private def createSparkSession(): SparkSession = {
    SparkSession.builder()
      .appName("Chess Analysis")
      .master("local[*]")
      .config("spark.executor.memory", "4g")
      .config("spark.sql.shuffle.partitions", "8") // Ottimizzazione delle partizioni shuffle
      .getOrCreate()
  }

  // Assicura che la directory di output sia pronta
  private def ensureOutputDirectory(outputDir: String): Unit = {
    val dir = Paths.get(outputDir)
    if (Files.exists(dir)) {
      Files.list(dir).forEach(file => Files.delete(file))
    } else {
      Files.createDirectories(dir)
    }
  }
}

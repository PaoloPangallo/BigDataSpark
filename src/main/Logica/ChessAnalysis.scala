import Services.{DataLoader, OpeningAnalyzer, RatingAnalyzer, ResultAnalyzer, StatisticsGenerator}
import Utils.FileUtils
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{Dataset, Row, SparkSession}

import java.io.{File, IOException}

object ChessAnalysis {

  def main(args: Array[String]): Unit = {
    configureLogging()
    val spark = createSparkSession()

    val filePath = "C:/Users/paolo/Desktop/Big Data/chess_games.csv/chess_games.csv"
    val outputDir = "C:/Users/paolo/Desktop/ProgettoSparkScacchi/Output"
    ensureOutputDirectory(outputDir)

    try {
      val chessData = DataLoader.loadDataset(spark, filePath)

      chessData.printSchema()


      val resultAnalyzer = new ResultAnalyzer()
      resultAnalyzer.analyze(chessData, outputDir)

      val openingAnalyzer = new OpeningAnalyzer()
      openingAnalyzer.analyze(chessData, outputDir)

      val ratingAnalyzer = new RatingAnalyzer()
      ratingAnalyzer.analyze(chessData, outputDir)

      val statisticsGenerator = new StatisticsGenerator()
      statisticsGenerator.generate(chessData, outputDir)


      println(s"Risultati salvati in CSV nella directory: $outputDir")
    } finally {
      spark.stop()
    }
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

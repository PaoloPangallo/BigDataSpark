import Logica.FileUtils
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.apache.log4j.{Level, Logger}

import java.io.File

object ChessAnalysis {
  def main(args: Array[String]): Unit = {
    // Configura i log per ridurre i messaggi superflui
    configureLogging()

    // Inizializza la SparkSession
    val spark = createSparkSession()

    // Percorso del file di input
    val filePath = "C:\\Users\\paolo\\Desktop\\Big Data\\chess_games.csv\\chess_games.csv"

    // Carica il dataset
    val chessData = loadDataset(spark, filePath)

    // Configura la directory di output
    val outputDir = "C:\\Users\\paolo\\Desktop\\ProgettoSparkScacchi\\Output"
    ensureOutputDirectory(outputDir)

    // Esegue le analisi e salva i risultati
    performAnalysis(chessData, outputDir)

    // Arresta la SparkSession
    spark.stop()
  }

  // **Metodo per configurare i log**
  private def configureLogging(): Unit = {
    Logger.getLogger("org.apache").setLevel(Level.WARN)
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
  }

  // **Metodo per creare la SparkSession**
  private def createSparkSession(): SparkSession = {
    SparkSession.builder()
      .appName("Chess Analysis")
      .master("local[*]") // Usa tutte le CPU disponibili
      .config("spark.ui.enabled", "false") // Disabilita l'interfaccia utente
      .getOrCreate()
  }

  // **Metodo per caricare il dataset**
  private def loadDataset(spark: SparkSession, filePath: String): Dataset[Row] = {
    val file = new File(filePath)
    if (!file.exists()) {
      println(s"Errore: Il file non esiste nel percorso: $filePath")
      System.exit(1)
    }

    spark.read
      .option("header", "true") // Il dataset ha un'intestazione
      .option("inferSchema", "true") // Inferisce automaticamente lo schema
      .csv(filePath)
  }

  // **Metodo per assicurarsi che la directory di output esista**
  private def ensureOutputDirectory(outputDir: String): Unit = {
    val dir = new File(outputDir)
    if (!dir.exists() && dir.mkdirs()) {
      println(s"Creata la directory di output: $outputDir")
    }
  }


  private def performAnalysis(chessData: Dataset[Row], outputDir: String): Unit = {
    import chessData.sparkSession.implicits._

    // Conteggio delle vittorie per colore
    val resultCounts = chessData.groupBy("Result").count()
    println("Conteggio delle vittorie per colore:")
    resultCounts.show()
    FileUtils.saveAsSingleJson(resultCounts, s"$outputDir\\resultCounts", "resultCounts.json")

    // Aperture più popolari
    val popularOpenings = chessData.groupBy("Opening").count().orderBy($"count".desc)
    println("Aperture più popolari:")
    popularOpenings.show(10, truncate = false)
    FileUtils.saveAsSingleJson(popularOpenings, s"$outputDir\\popularOpenings", "popularOpenings.json")

    // Differenza media del rating
    val avgRatingDiff = chessData
      .selectExpr("avg(WhiteRatingDiff) as AvgWhiteRatingDiff", "avg(BlackRatingDiff) as AvgBlackRatingDiff")
    println("Differenza media del rating per colore:")
    avgRatingDiff.show()
    FileUtils.saveAsSingleJson(avgRatingDiff, s"$outputDir\\avgRatingDiff", "avgRatingDiff.json")

    println(s"Risultati salvati in: $outputDir")
  }

}




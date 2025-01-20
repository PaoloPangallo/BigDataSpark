import org.apache.spark.sql.{SparkSession, Dataset, Row}
import java.io.File
import org.apache.log4j.{Level, Logger}

object ChessAnalysis {
  def main(args: Array[String]): Unit = {
    // Configura i log per ridurre i messaggi superflui
    Logger.getLogger("org.apache").setLevel(Level.WARN)
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

    val spark = SparkSession.builder()
      .appName("Test Spark")
      .master("local[*]") // Usa tutte le CPU disponibili
      .config("spark.ui.enabled", "false") // Disabilita l'interfaccia utente
      .getOrCreate()


    import spark.implicits._

    // Percorso del file di input
    val filePath = "C:\\Users\\paolo\\Desktop\\Big Data\\chess_games.csv\\chess_games.csv"

    // Controlla se il file esiste
    val file = new File(filePath)
    if (!file.exists()) {
      println(s"Errore: Il file non esiste nel percorso: $filePath")
      System.exit(1)
    }

    // Leggi il dataset di scacchi
    val chessData = spark.read
      .option("header", "true") // Il dataset ha un'intestazione
      .option("inferSchema", "true") // Inferisce automaticamente lo schema
      .csv(filePath)

    // Mostra le prime righe e lo schema del dataset
    println("Prime righe del dataset:")
    chessData.show(10, truncate = false) // Evita troncamenti per una migliore leggibilità
    println("Schema del dataset:")
    chessData.printSchema()

    // Controllo colonne
    println("Colonne trovate nel dataset:")
    chessData.columns.foreach(println)

    // Crea la directory di output se non esiste
    val outputDir = new File("C:\\Users\\paolo\\Desktop\\ProgettoSparkScalaScacchi\\Output")
    if (!outputDir.exists() && outputDir.mkdirs()) {
      println(s"Creata la directory di output: ${outputDir.getAbsolutePath}")
    }

    // Percorsi dei file di output
    val resultCountsPath = "C:\\Users\\paolo\\Desktop\\ProgettoSparkScalaScacchi\\Output\\resultCounts.json"
    val popularOpeningsPath = "C:\\Users\\paolo\\Desktop\\ProgettoSparkScalaScacchi\\Output\\popularOpenings.json"
    val avgRatingDiffPath = "C:\\Users\\paolo\\Desktop\\ProgettoSparkScalaScacchi\\Output\\avgRatingDiff.json"

    // **1. Conteggio delle vittorie per colore**
    val resultCounts = chessData.groupBy("Result").count()
    println("Conteggio delle vittorie per colore:")
    resultCounts.show()
    resultCounts.write.mode("overwrite").json(resultCountsPath)

    // **2. Aperture più popolari**
    val popularOpenings = chessData.groupBy("Opening").count().orderBy($"count".desc)
    println("Aperture più popolari:")
    popularOpenings.show(10, truncate = false) // Mostra i primi 10 senza troncare
    popularOpenings.write.mode("overwrite").json(popularOpeningsPath)

    // **3. Differenza media del rating**
    val avgRatingDiff = chessData
      .selectExpr("avg(WhiteRatingDiff) as AvgWhiteRatingDiff", "avg(BlackRatingDiff) as AvgBlackRatingDiff")
    println("Differenza media del rating per colore:")
    avgRatingDiff.show()
    avgRatingDiff.write.mode("overwrite").json(avgRatingDiffPath)

    // Messaggio di completamento
    println(s"I risultati sono stati salvati nei seguenti file JSON:")
    println(s"- Vittorie per colore: $resultCountsPath")
    println(s"- Aperture popolari: $popularOpeningsPath")
    println(s"- Differenze medie di rating: $avgRatingDiffPath")

    // Arresta la SparkSession
    spark.stop()
  }
}

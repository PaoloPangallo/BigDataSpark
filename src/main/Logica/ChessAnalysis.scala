import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{Dataset, Row, SparkSession}
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

    // Salvataggio in formato CSV in un solo file con nome specificato
    val resultCountsDir = s"$outputDir/resultCounts"
    resultCounts.coalesce(1)  // Assicura che venga scritto un solo file
      .write
      .mode("overwrite")
      .option("header", "true")
      .csv(resultCountsDir)

    renameCSVFile(resultCountsDir, s"$outputDir/resultCounts.csv")

    // Aperture più popolari
    val popularOpenings = chessData.groupBy("Opening").count().orderBy($"count".desc)
    println("Aperture più popolari:")
    popularOpenings.show(10, truncate = false)

    // Salvataggio in formato CSV in un solo file con nome specificato
    val popularOpeningsDir = s"$outputDir/popularOpenings"
    popularOpenings.coalesce(1)
      .write
      .mode("overwrite")
      .option("header", "true")
      .csv(popularOpeningsDir)

    renameCSVFile(popularOpeningsDir, s"$outputDir/popularOpenings.csv")

    // Differenza media del rating
    val avgRatingDiff = chessData
      .selectExpr("avg(WhiteRatingDiff) as AvgWhiteRatingDiff", "avg(BlackRatingDiff) as AvgBlackRatingDiff")
    println("Differenza media del rating per colore:")
    avgRatingDiff.show()

    // Salvataggio in formato CSV in un solo file con nome specificato
    val avgRatingDiffDir = s"$outputDir/avgRatingDiff"
    avgRatingDiff.coalesce(1)
      .write
      .mode("overwrite")
      .option("header", "true")
      .csv(avgRatingDiffDir)

    renameCSVFile(avgRatingDiffDir, s"$outputDir/avgRatingDiff.csv")

    println(s"Risultati salvati in CSV in: $outputDir")
  }

  // Funzione per rinominare il file CSV generato
  private def renameCSVFile(sourceDir: String, targetFileName: String): Unit = {
    val sourceDirFile = new File(sourceDir)
    if (sourceDirFile.exists() && sourceDirFile.isDirectory) {
      val partFile = sourceDirFile.listFiles().find(_.getName.startsWith("part-"))
      partFile match {
        case Some(file) =>
          val renamedFile = new File(targetFileName)
          if (file.renameTo(renamedFile)) {
            println(s"File rinominato con successo in: $targetFileName")
          } else {
            println(s"Errore nel rinominare il file a: $targetFileName")
          }
        case None => println(s"Nessun file part trovato nella directory $sourceDir")
      }
    } else {
      println(s"La directory $sourceDir non esiste.")
    }
  }

}

import org.apache.spark.sql.{SparkSession, Dataset, Row}
import java.io.File
import java.nio.file.{Files, Paths, StandardCopyOption}
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

    // Directory di output
    val outputDirPath = "C:\\Users\\paolo\\Desktop\\ProgettoSparkScacchi\\Output"
    val outputDir = new File(outputDirPath)
    if (!outputDir.exists() && outputDir.mkdirs()) {
      println(s"Creata la directory di output: $outputDirPath")
    }

    // Percorsi dei file di output
    val resultCountsPath = s"$outputDirPath\\resultCounts"
    val popularOpeningsPath = s"$outputDirPath\\popularOpenings"
    val avgRatingDiffPath = s"$outputDirPath\\avgRatingDiff"

    // **1. Conteggio delle vittorie per colore**
    val resultCounts = chessData.groupBy("Result").count()
    println("Conteggio delle vittorie per colore:")
    resultCounts.show()
    saveAsSingleJson(resultCounts, resultCountsPath, "resultCounts.json")

    // **2. Aperture più popolari**
    val popularOpenings = chessData.groupBy("Opening").count().orderBy($"count".desc)
    println("Aperture più popolari:")
    popularOpenings.show(10, truncate = false) // Mostra i primi 10 senza troncare
    saveAsSingleJson(popularOpenings, popularOpeningsPath, "popularOpenings.json")

    // **3. Differenza media del rating**
    val avgRatingDiff = chessData
      .selectExpr("avg(WhiteRatingDiff) as AvgWhiteRatingDiff", "avg(BlackRatingDiff) as AvgBlackRatingDiff")
    println("Differenza media del rating per colore:")
    avgRatingDiff.show()
    saveAsSingleJson(avgRatingDiff, avgRatingDiffPath, "avgRatingDiff.json")

    // Messaggio di completamento
    println(s"I risultati sono stati salvati nei seguenti file JSON:")
    println(s"- Vittorie per colore: $outputDirPath\\resultCounts.json")
    println(s"- Aperture popolari: $outputDirPath\\popularOpenings.json")
    println(s"- Differenze medie di rating: $outputDirPath\\avgRatingDiff.json")

    // Arresta la SparkSession
    spark.stop()
  }

  // Metodo per salvare il dataset come singolo file JSON

  import java.io.{File, FileWriter}
  import scala.io.Source

  private def saveAsSingleJson(df: Dataset[Row], outputPath: String, fileName: String): Unit = {
    val tempDir = s"$outputPath-temp" // Directory temporanea per il salvataggio
    val targetDir = new File(outputPath) // Directory di destinazione
    val targetFilePath = s"$outputPath\\$fileName" // Percorso del file di destinazione

    // Crea la directory di destinazione se non esiste
    if (!targetDir.exists() && !targetDir.mkdirs()) {
      throw new IllegalStateException(s"Impossibile creare la directory: ${targetDir.getAbsolutePath}")
    }

    // Salva il dataset come partizione singola
    df.coalesce(1)
      .write
      .mode("overwrite")
      .json(tempDir)

    // Trova il file JSON generato
    val tempDirFile = new File(tempDir)
    if (!tempDirFile.exists() || !tempDirFile.isDirectory) {
      throw new IllegalStateException(s"La directory temporanea $tempDir non esiste o non è una directory valida.")
    }

    val partFile = waitForPartFile(tempDirFile)
    if (partFile.isEmpty) {
      throw new IllegalStateException(s"File part-00000 non trovato nella directory $tempDir.")
    }

    // Leggi il contenuto del file generato
    val source = Source.fromFile(partFile.get)
    val content = source.getLines().mkString("\n")
    source.close()

    // Scrivi il contenuto nel file di destinazione
    val writer = new FileWriter(targetFilePath)
    writer.write(content)
    writer.close()

    // Rimuove la directory temporanea
    tempDirFile.listFiles().foreach(_.delete())
    tempDirFile.delete()
  }

  // Metodo per attendere che il file part-00000 venga generato
  private def waitForPartFile(tempDirFile: File): Option[File] = {
    var retries = 10 // Numero massimo di tentativi
    var partFile: Option[File] = None

    while (retries > 0 && partFile.isEmpty) {
      partFile = tempDirFile.listFiles().find(_.getName.startsWith("part-"))
      if (partFile.isEmpty) {
        Thread.sleep(500) // Attendi 500ms prima di riprovare
        retries -= 1
      }
    }

    partFile
  }
}


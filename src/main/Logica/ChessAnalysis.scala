import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import java.io.File
import java.io.IOException

object ChessAnalysis {

  def main(args: Array[String]): Unit = {
    configureLogging()
    val spark = createSparkSession()
    val filePath = "C:\\Users\\paolo\\Desktop\\Big Data\\chess_games.csv\\chess_games.csv"
    val outputDir = "C:\\Users\\paolo\\Desktop\\ProgettoSparkScacchi\\Output"

    ensureOutputDirectory(outputDir)

    val chessData = loadDataset(spark, filePath)
    performAnalysis(chessData, outputDir)

    spark.stop()
  }

  private def configureLogging(): Unit = {
    Logger.getLogger("org.apache").setLevel(Level.WARN)
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
  }

  private def createSparkSession(): SparkSession = {
    SparkSession.builder()
      .appName("Chess Analysis")
      .master("local[*]")
      .config("spark.ui.enabled", "false")
      .getOrCreate()
  }

  private def loadDataset(spark: SparkSession, filePath: String): Dataset[Row] = {
    val file = new File(filePath)
    if (!file.exists()) {
      println(s"Errore: Il file non esiste nel percorso: $filePath")
      System.exit(1)
    }

    spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(filePath)
  }

  private def ensureOutputDirectory(outputDir: String): Unit = {
    val dir = new File(outputDir)
    if (dir.exists()) {
      deleteDirectory(dir) // Elimina la directory e il suo contenuto
    }
    if (dir.mkdirs()) {
      println(s"Creata la directory di output: $outputDir")
    }
  }

  private def deleteDirectory(directory: File): Boolean = {
    if (directory.isDirectory) {
      directory.listFiles().foreach(deleteDirectory)
    }
    directory.delete()
  }

  private def performAnalysis(chessData: Dataset[Row], outputDir: String): Unit = {
    import chessData.sparkSession.implicits._

    // Conteggio delle vittorie per colore
    val resultCounts = chessData.groupBy("Result").count()
    saveAndRename(resultCounts, s"$outputDir/resultCounts", s"$outputDir/resultCounts.csv")

    // Aperture più popolari
    val popularOpenings = chessData.groupBy("Opening").count().orderBy($"count".desc)
    saveAndRename(popularOpenings, s"$outputDir/popularOpenings", s"$outputDir/popularOpenings.csv")

    // Differenza media del rating
    val avgRatingDiff = chessData
      .selectExpr("avg(WhiteRatingDiff) as AvgWhiteRatingDiff", "avg(BlackRatingDiff) as AvgBlackRatingDiff")
    saveAndRename(avgRatingDiff, s"$outputDir/avgRatingDiff", s"$outputDir/avgRatingDiff.csv")

    println(s"Risultati salvati in CSV nella directory: $outputDir")
  }

  private def saveAndRename(data: Dataset[Row], outputDir: String, targetFileName: String): Unit = {
    try {
      println(s"Scrittura dei dati in: $outputDir")
      data.coalesce(1)
        .write
        .mode("overwrite")
        .option("header", "true")
        .csv(outputDir)

      renameCSVFile(outputDir, targetFileName)
    } catch {
      case e: IOException =>
        println(s"Errore durante la scrittura in $outputDir: ${e.getMessage}")
        throw e
    }
  }

  private def renameCSVFile(sourceDir: String, targetFileName: String): Unit = {
    val sourceDirFile = new File(sourceDir)
    if (sourceDirFile.exists() && sourceDirFile.isDirectory) {
      val partFile = sourceDirFile.listFiles().find(_.getName.startsWith("part-"))
      partFile match {
        case Some(file) =>
          val renamedFile = new File(targetFileName)
          if (renamedFile.exists()) {
            println(s"Il file di destinazione esiste già: $targetFileName. Eliminandolo...")
            renamedFile.delete()
          }
          if (file.renameTo(renamedFile)) {
            println(s"File rinominato con successo in: $targetFileName")
          } else {
            println(s"Errore nel rinominare il file: ${file.getName} -> $targetFileName")
          }
        case None =>
          println(s"Nessun file part trovato nella directory $sourceDir")
      }
    } else {
      println(s"La directory $sourceDir non esiste o non è una directory.")
    }
  }
}

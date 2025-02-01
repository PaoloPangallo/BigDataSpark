package Services

import Utils.FileUtils
import org.apache.spark.sql.functions.regexp_replace
import org.apache.spark.sql.{Dataset, Row}

class ResultAnalyzer {
  def analyze(chessData: Dataset[Row], outputDir: String): Unit = {
    val resultCounts = chessData.groupBy("Result").count()
    FileUtils.saveAndRenameParquet(resultCounts, outputDir, "resultCounts.parquet")

  }
}

class OpeningAnalyzer {
  def analyze(chessData: Dataset[Row], outputDir: String): Unit = {
    import chessData.sparkSession.implicits._

    // Pulizia della colonna Opening
    val cleanedChessData = chessData.withColumn(
      "Opening",
      regexp_replace($"Opening", "\"", "") // Rimuove le virgolette dalla colonna Opening
    )

    // Conteggio delle aperture
    val popularOpenings = cleanedChessData
      .groupBy("Opening")
      .count()
      .withColumnRenamed("count", "OpeningCount")

    // Ordina per la colonna OpeningCount in ordine decrescente
    val sortedOpenings = popularOpenings.orderBy($"OpeningCount".desc)


    // Debug: controlla i dati prima di salvare

    // Salva i risultati
    FileUtils.saveAndRenameParquet(sortedOpenings, outputDir, "popularOpenings.parquet")

  }
}


class RatingAnalyzer {
  def analyze(chessData: Dataset[Row], outputDir: String): Unit = {
    val avgRatingDiff = chessData
      .selectExpr("avg(WhiteRatingDiff) as AvgWhiteRatingDiff", "avg(BlackRatingDiff) as AvgBlackRatingDiff")
    try {
      FileUtils.saveAndRenameParquet(avgRatingDiff, outputDir, "avgRatingDiff.parquet")

    } catch {
      case e: Exception =>
        println(s"Errore durante il salvataggio: ${e.getMessage}")
    }

  }}
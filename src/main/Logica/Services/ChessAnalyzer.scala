package Services

import Utils.FileUtils
import org.apache.spark.sql.{Dataset, Row}

class ResultAnalyzer {
  def analyze(chessData: Dataset[Row], outputDir: String): Unit = {
    val resultCounts = chessData.groupBy("Result").count()
    FileUtils.saveAndRename(resultCounts, outputDir, "resultCounts.csv")
  }
}

class OpeningAnalyzer {
  def analyze(chessData: Dataset[Row], outputDir: String): Unit = {
    import chessData.sparkSession.implicits._

    // Conteggio delle aperture
    val popularOpenings = chessData
      .groupBy("Opening")
      .count()
      .withColumnRenamed("count", "OpeningCount") // Rinomina la colonna count in OpeningCount

    // Ordina per la colonna OpeningCount in ordine decrescente
    val sortedOpenings = popularOpenings.orderBy($"OpeningCount".desc)

    // Salva i risultati
    FileUtils.saveAndRename(sortedOpenings, outputDir, "popularOpenings.csv")
  }
}


class RatingAnalyzer {
  def analyze(chessData: Dataset[Row], outputDir: String): Unit = {
    val avgRatingDiff = chessData
      .selectExpr("avg(WhiteRatingDiff) as AvgWhiteRatingDiff", "avg(BlackRatingDiff) as AvgBlackRatingDiff")
    FileUtils.saveAndRename(avgRatingDiff, outputDir, "avgRatingDiff.csv")
  }
}

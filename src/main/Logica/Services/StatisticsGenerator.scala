package Services

import Utils.FileUtils
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Dataset, Row}

class StatisticsGenerator {
  def generate(chessData: Dataset[Row], outputDir: String): Unit = {
    import chessData.sparkSession.implicits._

    // Calcolo delle statistiche
    val maxWhiteRatingDiff = chessData.agg(max($"WhiteRatingDiff")).as[Double].head()
    val maxBlackRatingDiff = chessData.agg(max($"BlackRatingDiff")).as[Double].head()
    val avgWhiteElo = chessData.agg(avg($"WhiteElo")).as[Double].head()
    val avgBlackElo = chessData.agg(avg($"BlackElo")).as[Double].head()

    // Conteggio dei risultati
    val resultCounts = chessData.groupBy("Result").count()
    val totalGames = chessData.count()
    val winPercentage = resultCounts.filter($"Result" === "1-0").select($"count").as[Long].head() * 100.0 / totalGames
    val drawPercentage = resultCounts.filter($"Result" === "1/2-1/2").select($"count").as[Long].head() * 100.0 / totalGames
    val lossPercentage = resultCounts.filter($"Result" === "0-1").select($"count").as[Long].head() * 100.0 / totalGames

    // Organizza le statistiche in un dataset
    val stats = Seq(
      ("Massima differenza di rating (White)", maxWhiteRatingDiff),
      ("Massima differenza di rating (Black)", maxBlackRatingDiff),
      ("Media punteggio ELO (White)", avgWhiteElo),
      ("Media punteggio ELO (Black)", avgBlackElo),
      ("Percentuale vittorie (White)", winPercentage),
      ("Percentuale pareggi", drawPercentage),
      ("Percentuale vittorie (Black)", lossPercentage)
    ).toDF("Statistic", "Value")

    // Salva i risultati in un file CSV
    FileUtils.saveAndRename(stats, outputDir, "chessStatistics.csv")
  }
}

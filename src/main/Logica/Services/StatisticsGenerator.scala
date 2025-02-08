package Services

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Dataset, Row}
import org.apache.spark.storage.StorageLevel
import org.apache.log4j.Logger

class StatisticsGenerator {
  private val logger = Logger.getLogger(getClass.getName)

  def analyze(chessData: Dataset[Row], outputDir: String): Unit = {
    import chessData.sparkSession.implicits._

    logger.info("Inizio generazione statistiche...")

    // Persistenza ottimizzata per evitare rielaborazioni
    val filteredChessData = chessData
      .select("WhiteRatingDiff", "BlackRatingDiff", "WhiteElo", "BlackElo", "Result")
      .persist(StorageLevel.MEMORY_AND_DISK)

    val maxWhiteRatingDiff = filteredChessData.agg(max($"WhiteRatingDiff")).first().getDouble(0)
    val maxBlackRatingDiff = filteredChessData.agg(max($"BlackRatingDiff")).first().getDouble(0)
    val avgWhiteElo = filteredChessData.agg(avg($"WhiteElo")).first().getDouble(0)
    val avgBlackElo = filteredChessData.agg(avg($"BlackElo")).first().getDouble(0)

    logger.info(s"Massima differenza di rating (White): $maxWhiteRatingDiff")
    logger.info(s"Massima differenza di rating (Black): $maxBlackRatingDiff")
    logger.info(s"Media punteggio ELO (White): $avgWhiteElo")
    logger.info(s"Media punteggio ELO (Black): $avgBlackElo")

    // Aggregazione in un'unica query per ridurre il numero di operazioni
    val resultCountsAgg = filteredChessData.groupBy().agg(
      sum(when($"Result" === "1-0", 1)).alias("winCount"),
      sum(when($"Result" === "1/2-1/2", 1)).alias("drawCount"),
      sum(when($"Result" === "0-1", 1)).alias("lossCount"),
      count("*").alias("totalGames")
    ).collect().head

    val winPercentage = resultCountsAgg.getLong(0) * 100.0 / resultCountsAgg.getLong(3)
    val drawPercentage = resultCountsAgg.getLong(1) * 100.0 / resultCountsAgg.getLong(3)
    val lossPercentage = resultCountsAgg.getLong(2) * 100.0 / resultCountsAgg.getLong(3)

    // Creazione del dataset delle statistiche
    val stats = Seq(
      ("Massima differenza di rating (White)", maxWhiteRatingDiff),
      ("Massima differenza di rating (Black)", maxBlackRatingDiff),
      ("Media punteggio ELO (White)", avgWhiteElo),
      ("Media punteggio ELO (Black)", avgBlackElo),
      ("Percentuale vittorie (White)", winPercentage),
      ("Percentuale pareggi", drawPercentage),
      ("Percentuale vittorie (Black)", lossPercentage)
    ).toDF("Statistic", "Value")

    // Salvataggio con sovrascrittura
    stats.write.mode("overwrite").parquet(s"$outputDir/chessStatistics.parquet")

    logger.info("Statistiche salvate con successo.")

    // Libera il cache
    filteredChessData.unpersist()
    logger.info("Memoria liberata.")
  }
}

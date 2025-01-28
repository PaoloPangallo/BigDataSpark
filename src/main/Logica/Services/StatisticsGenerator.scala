package Services

import Utils.FileUtils
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Dataset, Row}

import org.apache.log4j.Logger

class StatisticsGenerator {
  private val logger = Logger.getLogger(getClass.getName)

  def generate(chessData: Dataset[Row], outputDir: String): Unit = {
    import chessData.sparkSession.implicits._

    logger.info("Inizio generazione statistiche...")

    // **1. Pre-selezione delle colonne necessarie**
    val filteredChessData = chessData
      .select("WhiteRatingDiff", "BlackRatingDiff", "WhiteElo", "BlackElo", "Result")
      .cache() // Caching per evitare rielaborazioni

    // **2. Calcolo delle statistiche principali**
    val maxWhiteRatingDiff = filteredChessData.agg(max($"WhiteRatingDiff")).as[Double].head()
    logger.info(s"Massima differenza di rating (White): $maxWhiteRatingDiff")

    val maxBlackRatingDiff = filteredChessData.agg(max($"BlackRatingDiff")).as[Double].head()
    logger.info(s"Massima differenza di rating (Black): $maxBlackRatingDiff")

    val avgWhiteElo = filteredChessData.agg(avg($"WhiteElo")).as[Double].head()
    logger.info(s"Media punteggio ELO (White): $avgWhiteElo")

    val avgBlackElo = filteredChessData.agg(avg($"BlackElo")).as[Double].head()
    logger.info(s"Media punteggio ELO (Black): $avgBlackElo")

    // **3. Conteggio e percentuali sui risultati**
    val resultCounts = filteredChessData.groupBy("Result").count()
    logger.info("Conteggio dei risultati completato.")

    val totalGames = filteredChessData.count()
    val winPercentage = {
      val winCount = resultCounts
        .filter($"Result" === "1-0")
        .select($"count").as[Long].collect()
        .headOption.getOrElse(0L)

      winCount * 100.0 / totalGames
    }

    val drawPercentage = {
      val drawCount = resultCounts
        .filter($"Result" === "1/2-1/2")
        .select($"count").as[Long].collect()
        .headOption.getOrElse(0L)

      drawCount * 100.0 / totalGames
    }

    val lossPercentage = {
      val lossCount = resultCounts
        .filter($"Result" === "0-1")
        .select($"count").as[Long].collect()
        .headOption.getOrElse(0L)

      lossCount * 100.0 / totalGames
    }


    // **4. Creazione del dataset delle statistiche**
    val stats = Seq(
      ("Massima differenza di rating (White)", maxWhiteRatingDiff),
      ("Massima differenza di rating (Black)", maxBlackRatingDiff),
      ("Media punteggio ELO (White)", avgWhiteElo),
      ("Media punteggio ELO (Black)", avgBlackElo),
      ("Percentuale vittorie (White)", winPercentage),
      ("Percentuale pareggi", drawPercentage),
      ("Percentuale vittorie (Black)", lossPercentage)
    ).toDF("Statistic", "Value")

    // **5. Salvataggio dei risultati**
    FileUtils.saveAndRename(stats, outputDir, "chessStatistics.csv")
    logger.info("Statistiche salvate con successo.")

    // **6. Libera il cache**
    filteredChessData.unpersist()
    logger.info("Memoria liberata.")
  }
}

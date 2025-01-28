package Services


import Utils.FileUtils
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Dataset, Row}
import org.apache.log4j.Logger
import org.apache.spark.sql.expressions.Window.partitionBy

class SpecificQueryGenerator {
  private val logger = Logger.getLogger(getClass.getName)

  def generate(chessData: Dataset[Row], outputDir: String): Unit = {
    import chessData.sparkSession.implicits._

    logger.info("Inizio generazione di query specifiche...")

    // **1. Aperture con il maggior numero di vittorie per il bianco e per il nero**
    val openingWinStats = chessData
      .filter($"Result" === "1-0" || $"Result" === "0-1")
      .withColumn("Winner", when($"Result" === "1-0", "White").otherwise("Black"))
      .groupBy("Opening", "Winner")
      .agg(count("*").as("WinCount"))
      .orderBy(desc("WinCount"))
      .limit(6)
    FileUtils.saveAndRename(openingWinStats, outputDir, "openingWinStats.csv")

    logger.info("Aperture con il maggior numero di vittorie calcolate.")

    // **2. Distribuzione delle partite per fascia di rating (ELO Bianco e Nero)**
    val ratingDistribution = chessData
      .withColumn("RatingRange", when($"WhiteElo" >= 2000 && $"BlackElo" >= 2000, "2000+")
        .when($"WhiteElo" >= 1500 && $"BlackElo" >= 1500, "1500-1999")
        .otherwise("Under 1500"))
      .groupBy("RatingRange")
      .agg(count("*").as("GameCount"))
      .orderBy(desc("GameCount"))
    FileUtils.saveAndRename(ratingDistribution, outputDir, "ratingDistribution.csv")

    logger.info("Distribuzione delle partite per fascia di rating calcolata.")

    // **3. Analisi delle aperture più utilizzate in partite terminate per "Timeout"**
    val timeoutOpenings = chessData
      .filter($"Termination" === "Time forfeit")
      .groupBy("Opening")
      .agg(count("*").as("TimeoutGames"))
      .orderBy(desc("TimeoutGames"))
      .limit(6)
    FileUtils.saveAndRename(timeoutOpenings, outputDir, "timeoutOpenings.csv")

    logger.info("Aperture in partite terminate per Timeout calcolate.")

    // **4. Differenze di rating medie per apertura**
    import org.apache.spark.sql.functions._

    val ratingDiffByOpening = chessData
      .withColumn("Opening", regexp_replace($"Opening", "\"", "")) // Rimuove le virgolette doppie
      .groupBy("Opening")
      .agg(
        avg($"WhiteRatingDiff").as("AvgWhiteRatingDiff"),
        avg($"BlackRatingDiff").as("AvgBlackRatingDiff")
      )
      .orderBy(desc("AvgWhiteRatingDiff"))
      .limit(5) // Prendi solo i primi 5 risultati

    FileUtils.saveAndRename(ratingDiffByOpening, outputDir, "ratingDiffByOpening.csv")


    logger.info("Differenze di rating medie per apertura calcolate.")

    // **5. Percentuale di pareggi per controllo del tempo**
    val drawByTimeControl = chessData
      .filter($"Result" === "1/2-1/2")
      .groupBy("TimeControl")
      .agg(count("*").as("DrawGames"))
      .withColumn("DrawPercentage", ($"DrawGames" / sum("DrawGames").over()) * 100)
      .orderBy(desc("DrawPercentage"))
    FileUtils.saveAndRename(drawByTimeControl, outputDir, "drawByTimeControl.csv")

    logger.info("Percentuali di pareggi per controllo del tempo calcolate.")



    logger.info("Giocatori più attivi calcolati.")

    // **7. Percentuale di vittorie in base alla differenza di rating**
    val winByRatingDiff = chessData
      .filter($"Result" === "1-0" || $"Result" === "0-1")
      .withColumn("RatingDiffCategory", when(abs($"WhiteRatingDiff") >= 100, "High Difference")
        .otherwise("Low Difference"))
      .groupBy("RatingDiffCategory", "Result")
      .agg(count("*").as("WinCount"))
      .withColumn("WinPercentage", ($"WinCount" / sum("WinCount").over(partitionBy("RatingDiffCategory"))) * 100)
      .orderBy(col("RatingDiffCategory"), desc("WinPercentage"))
    FileUtils.saveAndRename(winByRatingDiff, outputDir, "winByRatingDiff.csv")

    logger.info("Percentuale di vittorie per differenza di rating calcolata.")

    logger.info("Tutte le query specifiche sono state generate e salvate.")
  }
}


package Services

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Dataset, Row}
import org.apache.log4j.Logger
import Utils.FileUtils

class TerminationQueryGenerator {
  private val logger = Logger.getLogger(getClass.getName)

  def analyze(chessData: Dataset[Row], outputDir: String): Unit = {
    import chessData.sparkSession.implicits._

    logger.info("Inizio analisi della colonna Termination...")

    // **1. Conteggio degli esiti delle partite**
    val terminationCounts = chessData
      .groupBy("Termination")
      .agg(count("*").as("Count"))
      .orderBy(desc("Count"))

    FileUtils.saveAndRenameParquet(terminationCounts, outputDir, "terminationCounts.parquet")

    // **2. Percentuale degli esiti rispetto al totale**
    val totalGames = chessData.count().toDouble
    val terminationPercentages = terminationCounts
      .withColumn("Percentage", ($"Count" / totalGames) * 100)

    FileUtils.saveAndRenameParquet(terminationPercentages, outputDir, "terminationPercentages.parquet")

    // **3. Analisi degli abbandoni rispetto ai timeout**
    val abandonVsTimeout = chessData
      .filter($"Termination".isin("Abandoned", "Time forfeit"))
      .groupBy("Termination")
      .agg(count("*").as("Count"))
      .orderBy(desc("Count"))

    FileUtils.saveAndRenameParquet(abandonVsTimeout, outputDir, "abandonVsTimeout.parquet")

    // **4. Identificare i giocatori coinvolti in partite "Unterminated"**
    val unterminatedPlayers = chessData
      .filter($"Termination" === "Unterminated")
      .select("White", "Black", "Termination")

    FileUtils.saveAndRenameParquet(unterminatedPlayers, outputDir, "unterminatedPlayers.parquet")

    logger.info("Analisi della colonna Termination completata e salvata.")
  }
}


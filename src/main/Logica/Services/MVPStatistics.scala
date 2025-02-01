package Services

import Utils.FileUtils
import org.apache.spark.sql.{Dataset, Row}
import org.apache.spark.sql.functions._

class MVPStatistics {

  def analyze(chessData: Dataset[Row], outputDir: String): Unit = {

    import chessData.sparkSession.implicits._

    // **1. Pre-selezione delle colonne necessarie**
    val filteredChessData = chessData
      .select("Result", "White", "Black")
      .filter($"Result" === "1-0" || $"Result" === "0-1")
      .cache()

    // **2. Giocatori più vincenti (assoluti)**
    val topPlayersAbsolute = filteredChessData
      .withColumn("Winner", when($"Result" === "1-0", $"White").otherwise($"Black")) // Determina il vincitore
      .groupBy("Winner")
      .agg(count("*").as("WinsAbsolute")) // Conta le vittorie assolute
      .orderBy(desc("WinsAbsolute")) // Ordina in ordine decrescente
      .limit(3) // Prendi i primi 3

    FileUtils.saveAndRename(topPlayersAbsolute, outputDir, "topPlayersAbsolute.csv")

    //**3. Giocatori con la percentuale di vittorie più alta (con almeno 20 partite)**
    val totalGamesPerPlayer = filteredChessData
      .withColumn("Player", when($"Result" === "1-0", $"White").otherwise($"Black")) // Identifica il giocatore
      .groupBy("Player")
      .agg(
        count("*").as("TotalGames"), // Conta il numero totale di partite
        sum(when($"Result" === "1-0" && $"White" === $"Player", 1).otherwise(0) + // Vittorie come bianco
          when($"Result" === "0-1" && $"Black" === $"Player", 1).otherwise(0)).as("Wins") // Vittorie come nero
      )
      .filter($"TotalGames" >= 20)
      .withColumn("WinPercentage", ($"Wins" / $"TotalGames") * 100)
      .orderBy(desc("WinPercentage")) // Ordina per percentuale di vittorie
      .limit(3) // Prendi i primi 3

    FileUtils.saveAndRename(totalGamesPerPlayer, outputDir, "topPlayersPercentage.csv")
  }
}

package Services

import Utils.FileUtils
import org.apache.spark.sql.{Dataset, Row}
import org.apache.spark.sql.functions._

class MVPStatistics {

  def analyze(chessData: Dataset[Row], outputDir: String): Unit = {

    import chessData.sparkSession.implicits._

    // **1. Giocatori più vincenti (assoluti)**
    val topPlayersAbsolute = chessData
      .filter($"Result" === "1-0" || $"Result" === "0-1") // Filtra solo le partite vinte
      .withColumn("Winner", when($"Result" === "1-0", $"White").otherwise($"Black")) // Determina il vincitore
      .groupBy("Winner") // Raggruppa per vincitore
      .count() // Conta il numero di vittorie
      .orderBy(desc("count")) // Ordina in ordine decrescente
      .limit(3) // Prendi i primi 3
      .withColumnRenamed("Winner", "Player")
      .withColumnRenamed("count", "WinsAbsolute")

    FileUtils.saveAndRename(topPlayersAbsolute, outputDir, "topPlayersAbsolute.csv")

    // **2. Giocatori con la percentuale di vittorie più alta (con almeno 20 partite)**
    val totalGamesPerPlayer = chessData
      .filter($"Result" === "1-0" || $"Result" === "0-1") // Filtra solo le partite valide
      .withColumn("Player", when($"Result" === "1-0", $"White").otherwise($"Black")) // Identifica il giocatore
      .groupBy("Player") // Raggruppa per giocatore
      .agg(
        count("*").as("TotalGames"), // Conta il numero totale di partite
        sum(when($"Result" === "1-0" && $"White" === $"Player", 1).otherwise(0) + // Vittorie come bianco
          when($"Result" === "0-1" && $"Black" === $"Player", 1).otherwise(0)).as("Wins") // Vittorie come nero
      )
      .filter($"TotalGames" >= 20) // Considera solo giocatori con almeno 20 partite
      .withColumn("WinPercentage", ($"Wins" / $"TotalGames") * 100) // Calcola la percentuale di vittorie
      .orderBy(desc("WinPercentage")) // Ordina per percentuale di vittorie
      .limit(3) // Prendi i primi 3

    FileUtils.saveAndRename(totalGamesPerPlayer, outputDir, "topPlayersPercentage.csv")
  }
}

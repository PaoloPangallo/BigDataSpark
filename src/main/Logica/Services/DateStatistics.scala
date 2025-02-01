package Services

import Utils.FileUtils
import org.apache.spark.sql.{Dataset, Row}
import org.apache.spark.sql.functions._

class DateStatistics {

  def analyze(chessData: Dataset[Row], outputDir: String): Unit = {

    import chessData.sparkSession.implicits._

    // **1. Pre-selezione delle colonne necessarie**
    val filteredChessData = chessData
      .select("UTCDate", "Opening") // Seleziona solo le colonne necessarie
      .cache() // Cache per evitare di rielaborare i dati

    // **2. Giorno con il maggior numero di partite**
    val mostGamesDay = filteredChessData
      .groupBy("UTCDate") // Raggruppa per giorno
      .count() // Conta il numero di partite per giorno
      .orderBy(desc("count")) // Ordina in ordine decrescente
      .limit(3) // Prendi i primi 3
      .withColumnRenamed("UTCDate", "Day")
      .withColumnRenamed("count", "TotalGames")


    FileUtils.saveAndRenameParquet(mostGamesDay, outputDir, "mostGamesDay.parquet")


    // **3. Giorno con più partite per una specifica apertura**
    val mostGamesForOpeningDay = filteredChessData
      .filter($"Opening" === "Sicilian Defense") // Filtra per apertura specifica
      .groupBy("UTCDate") // Raggruppa per giorno
      .count() // Conta il numero di partite per giorno
      .orderBy(desc("count")) // Ordina in ordine decrescente
      .limit(3) // Prendi i primi 3 giorni
      .withColumnRenamed("UTCDate", "Day")
      .withColumnRenamed("count", "SicilianDefenseGames")

    FileUtils.saveAndRenameParquet(mostGamesForOpeningDay, outputDir, "mostGamesDay.parquet")


    // **4. Giorno con il maggior numero di aperture uniche**
    val dayWithMostUniqueOpenings = filteredChessData
      .groupBy("UTCDate") // Raggruppa per giorno
      .agg(countDistinct("Opening").as("UniqueOpenings")) // Conta le aperture uniche
      .orderBy(desc("UniqueOpenings")) // Ordina in ordine decrescente
      .limit(3) // Prendi i primi 3 giorni
      .withColumnRenamed("UTCDate", "Day")

    FileUtils.saveAndRenameParquet(dayWithMostUniqueOpenings, outputDir, "dayWithMostUniqueOpenings.parquet")


    // **5. Liberare il cache dopo l'uso**
    filteredChessData.unpersist()
  }









}

package Services

import Utils.FileUtils
import org.apache.spark.sql.{Dataset, Row}
import org.apache.spark.sql.functions._

class DateStatistics {

  def analyze(chessData: Dataset[Row], outputDir: String): Unit = {

    import chessData.sparkSession.implicits._

    // **1. Giorno con il maggior numero di partite**
    val mostGamesDay = chessData
      .groupBy("UTCDate") // Raggruppa per giorno
      .count() // Conta il numero di partite per giorno
      .orderBy(desc("count")) // Ordina in ordine decrescente
      .limit(3)
      .withColumnRenamed("UTCDate", "Day")
      .withColumnRenamed("count", "TotalGames")

    FileUtils.saveAndRename(mostGamesDay, outputDir, "mostGamesDay.csv")

    // **2. Giorno con più partite per una specifica apertura**
    val mostGamesForOpeningDay = chessData
      .filter($"Opening" === "Sicilian Defense") // Filtra per apertura specifica
      .groupBy("UTCDate") // Raggruppa per giorno
      .count() // Conta il numero di partite per giorno
      .orderBy(desc("count")) // Ordina in ordine decrescente
      .limit(3) // Prendi il giorno con più partite per l'apertura specificata
      .withColumnRenamed("UTCDate", "Day")
      .withColumnRenamed("count", "Apertura siciliana")

    FileUtils.saveAndRename(mostGamesForOpeningDay, outputDir, "mostGamesForOpeningDay.csv")

    // **3. Giorno con il maggior numero di aperture uniche**
    val dayWithMostUniqueOpenings = chessData
      .groupBy("UTCDate") // Raggruppa per giorno
      .agg(countDistinct("Opening").alias("UniqueOpenings")) // Conta le aperture uniche
      .orderBy(desc("UniqueOpenings")) // Ordina in ordine decrescente
      .limit(3)
      .withColumnRenamed("UTCDate", "Day")

    FileUtils.saveAndRename(dayWithMostUniqueOpenings, outputDir, "dayWithMostUniqueOpenings.csv")
  }
}


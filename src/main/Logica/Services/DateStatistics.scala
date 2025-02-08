package Services

import Utils.FileUtils
import org.apache.spark.sql.{Dataset, Row}
import org.apache.spark.sql.functions._

class DateStatistics {

  def analyze(chessData: Dataset[Row], outputDir: String): Unit = {

    import chessData.sparkSession.implicits._


    val filteredChessData = chessData
      .select("UTCDate", "Opening")
      .cache()


    val mostGamesDay = filteredChessData
      .groupBy("UTCDate")
      .count()
      .orderBy(desc("count"))
      .limit(3)
      .withColumnRenamed("UTCDate", "Day")
      .withColumnRenamed("count", "TotalGames")


    FileUtils.saveAndRenameParquet(mostGamesDay, outputDir, "mostGamesDay.parquet")



    val mostGamesForOpeningDay = filteredChessData
      .filter($"Opening" === "Sicilian Defense") // Filtra per apertura specifica
      .groupBy("UTCDate")
      .count()
      .orderBy(desc("count"))
      .limit(3)
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


    //  Liberare il cache dopo l'uso**
    filteredChessData.unpersist()
  }









}

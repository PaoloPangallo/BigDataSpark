import org.apache.spark.sql.{SparkSession}
import org.apache.spark.sql.functions._
import org.apache.log4j.{Level, Logger}

object IspezioneDataset {
  def main(args: Array[String]): Unit = {
    // Disabilita log verbosi di Spark
    Logger.getLogger("org").setLevel(Level.WARN)

    val spark = SparkSession.builder()
      .appName("Chess Data Exploration")
      .master("local[*]")
      .config("spark.driver.memory", "8g")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer") // Preferibile rispetto a JavaSerializer
      .getOrCreate()

    val filePath = "C:/Users/paolo/Desktop/Big Data/chess_games.csv/chess_games.csv"


    val chessData = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(filePath)

    if (chessData.isEmpty) {
      println(" Il dataset è vuoto. Controlla il percorso del file.")
      spark.stop()
      System.exit(1)
    }

    println("Schema del dataset:")
    chessData.printSchema()

    // Anteprima delle righe
    println("\n Prime 10 righe del dataset:")
    chessData.show(10, truncate = false)

    // Contare i valori nulli per colonna in modo più efficiente
    println("\n Numero di valori nulli per colonna:")
    val nullCounts = chessData.select(chessData.columns.map(c => count(when(col(c).isNull || col(c) === "", c)).alias(c)): _*)
    nullCounts.show()

    val duplicateCount = chessData.count() - chessData.dropDuplicates().count()
    println(s"\n Numero di righe duplicate: $duplicateCount")

    if (duplicateCount > 0) {
      println(" Analisi delle righe duplicate (prime 5 righe duplicate):")
      chessData.groupBy(chessData.columns.map(col): _*)
        .count()
        .filter("count > 1")
        .orderBy(desc("count"))
        .show(5, truncate = false)
    }

    // Analizzare eventuali colonne problematiche
    println("\n Controllo di colonne problematiche:")
    chessData.dtypes.foreach { case (colName, dtype) =>
      if (dtype.contains("Array") || dtype.contains("Binary") || dtype.contains("Byte")) {
        println(s"️ POSSIBILE PROBLEMA: La colonna '$colName' ha un tipo sospetto -> $dtype")
      }
    }

    // Mostrare statistiche sulle colonne numeriche
    println("\n Statistiche descrittive delle colonne numeriche:")
    val numericCols = chessData.schema.fields.filter(_.dataType.simpleString.matches("int|double|float|long")).map(_.name)
    if (numericCols.nonEmpty) {
      chessData.select(numericCols.map(col): _*).describe().show()
    } else {
      println(" Nessuna colonna numerica trovata.")
    }

    spark.stop()
  }
}

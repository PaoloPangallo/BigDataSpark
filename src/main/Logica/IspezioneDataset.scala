import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.apache.spark.ml.classification.{RandomForestClassificationModel, RandomForestClassifier}
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator

import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._

object IspezioneDataset {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Chess Data Exploration")
      .master("local[*]") // Usa tutti i core disponibili
      .config("spark.driver.memory", "8g") // Assegna più memoria
      .config("spark.serializer", "org.apache.spark.serializer.JavaSerializer") // Forza JavaSerializer
      .getOrCreate()

    val filePath = "C:/Users/paolo/Desktop/Big Data/chess_games.csv/chess_games.csv"

    val chessData = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(filePath)

    println(" Schema del dataset:")
    chessData.printSchema()

    // 3️⃣ Mostra un'anteprima delle prime righe
    println(" Prime 10 righe del dataset:")
    chessData.show(10, truncate = false)

    println(" Numero di valori nulli per colonna:")
    chessData.columns.foreach { colName =>
      val nullCount = chessData.filter(col(colName).isNull || col(colName) === "").count()
      println(s"$colName: $nullCount valori nulli")
    }

    val duplicateCount = chessData.count() - chessData.dropDuplicates().count()
    println(s" Numero di righe duplicate: $duplicateCount")

    println(" Controllo se ci sono colonne problematiche:")
    chessData.dtypes.foreach { case (colName, dtype) =>
      if (dtype.contains("Array") || dtype.contains("Binary") || dtype.contains("Byte")) {
        println(s" POSSIBILE PROBLEMA: La colonna '$colName' ha tipo sospetto -> $dtype")
      }
    }

    spark.stop()
  }
}

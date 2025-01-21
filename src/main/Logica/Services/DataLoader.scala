package Services

import org.apache.spark.sql.{Dataset, Row, SparkSession}
import java.io.File

object DataLoader {

  def loadDataset(spark: SparkSession, filePath: String): Dataset[Row] = {
    val file = new File(filePath)
    if (!file.exists()) {
      throw new IllegalArgumentException(s"Errore: Il file non esiste nel percorso: $filePath")
    }

    spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(filePath)
  }
}

import org.apache.spark.sql.SparkSession

object TestSpark {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Test Spark")
      .master("local[*]") // Usa tutte le CPU disponibili
      .config("spark.ui.enabled", "false") // Disabilita l'interfaccia utente
      .getOrCreate()

    val data = Seq((1, "Alice"), (2, "Bob"))
    val df = spark.createDataFrame(data).toDF("id", "name")
    df.show()

    spark.stop()
  }
}

package Utils

import org.apache.spark.sql.{Dataset, Row}
import org.apache.spark.sql.functions._

object DatasetUtils {

  def analyzeDataset(data: Dataset[Row]): Unit = {
    println("===== Analisi Primordiale del Dataset =====")

    // Numero di righe e colonne
    val rowCount = data.count()
    val columnCount = data.columns.length
    println(s"Numero di righe: $rowCount")
    println(s"Numero di colonne: $columnCount")

    // Tipi di dati delle colonne
    println("\nTipi di dati per colonna:")
    data.schema.fields.foreach { field =>
      println(s" - ${field.name}: ${field.dataType}")
    }

    // Colonne con valori mancanti
    println("\nColonne con valori mancanti:")
    val missingValues = data.columns.map { colName =>
      val nullCount = data.filter(col(colName).isNull || col(colName) === "").count()
      (colName, nullCount, nullCount.toDouble / rowCount * 100)
    }.filter(_._2 > 0)

    if (missingValues.nonEmpty) {
      missingValues.foreach { case (colName, count, percentage) =>
        println(f" - $colName: $count valori mancanti (${percentage}%.2f%%)")
      }
    } else {
      println("Nessuna colonna ha valori mancanti.")
    }

    // Valori unici per colonna
    println("\nNumero di valori unici per colonna:")
    data.columns.foreach { colName =>
      val uniqueCount = data.select(colName).distinct().count()
      println(s" - $colName: $uniqueCount valori unici")
    }}


    def numericalSummary(data: Dataset[Row]): Unit = {
      println("\n===== Distribuzione Statistica per le Colonne Numeriche =====")
      val numericCols = data.schema.fields.filter(_.dataType.typeName == "double").map(_.name)

      numericCols.foreach { colName =>
        val summary = data.describe(colName).collect()
        println(s"\nColonna: $colName")
        summary.foreach(row => println(s" - ${row(0)}: ${row(1)}"))
      }
    }

    def categoricalDistribution(data: Dataset[Row]): Unit = {
      println("\n===== Distribuzione delle Colonne Categoriali =====")
      val categoricalCols = data.schema.fields.filter(_.dataType.typeName == "string").map(_.name)

      categoricalCols.foreach { colName =>
        println(s"\nColonna: $colName")
        data.groupBy(colName).count()
          .orderBy(desc("count"))
          .show(truncate = false)
      }
    }



    def checkDuplicates(data: Dataset[Row]): Unit = {
      println("\n===== Verifica delle Duplicazioni =====")
      val duplicateCount = data.groupBy(data.columns.map(col): _*).count().filter(col("count") > 1).count()
      println(s"Numero di righe duplicate: $duplicateCount")
    }}


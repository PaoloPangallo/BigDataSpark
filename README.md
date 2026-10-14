<div align="center">

# ♟️ BigDataSpark — Chess Analytics

**Apache Spark · Chess data · Distributed analytics**

![Apache Spark](https://img.shields.io/badge/Apache_Spark-E25A1C?style=for-the-badge&logo=apachespark&logoColor=white)
![Scala](https://img.shields.io/badge/Scala_2.12-DC322F?style=for-the-badge&logo=scala&logoColor=white)
![Java](https://img.shields.io/badge/Java_11-437291?style=for-the-badge)
![Data](https://img.shields.io/badge/Chess_Analytics-7C5CFC?style=for-the-badge)

</div>

A university project exploring analysis of chess game data using **Apache Spark** and a Java/Scala application with web components.

## 🔎 What is in this repository?

- Data-processing logic and queries under [`src/main/Logica/`](src/main/Logica/).
- Web application assets under [`src/main/webapp/`](src/main/webapp/).
- Aggregated CSV and Parquet outputs, historical charts and model-result figures.
- A Maven project describing the Spark/Scala and supporting application dependencies.

The stored figures and CSV/Parquet files are **historical project artifacts**. They are not presented as a newly reproduced benchmark.

## 🧱 Technology

| Area | Stack |
| --- | --- |
| Batch/distributed processing | Apache Spark 3.3.2, Scala 2.12 |
| Application | Java 11, Maven, Jetty |
| Web application dependencies | Vaadin frontend packages |
| Outputs | CSV, Parquet, charts and performance reports |

## 🚀 Setup notes

The project targets the older **Java 11 / Spark 3.3.2 / Scala 2.12** stack declared in [`pom.xml`](pom.xml). A matching Java/Maven environment and the original dataset/configuration are required.

```bash
# Inspect the build environment first
java -version
mvn -version

# Compile using the repository's historical Maven configuration
mvn -DskipTests compile
```

The Maven build and runtime environment **have not been independently verified in a clean container**, and some historical dependencies may need work before reproduction. Consult the source paths for the dataset expectations and application entry points; do not treat the existing outputs as a reproducible one-command run.

## 🧹 Repository hygiene

The working tree intentionally excludes installed `node_modules/` dependencies, IDE metadata (`.idea/`) and generated Hadoop/Spark `*.crc` checksum sidecars.

Generated output figures/data already tracked in the repository have been retained so the project's historical work remains inspectable.

> Git history is left intact. Removing tracked generated files from the latest commit does **not** erase the copies in older commits.

## Author

[Paolo Pangallo](https://github.com/PaoloPangallo) — Computer Engineering / Artificial Intelligence · [LinkedIn](https://www.linkedin.com/in/paolo-pangallo-9aa456170/)

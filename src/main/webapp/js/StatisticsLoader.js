class StatisticsLoader {
    constructor(uiManager) {
        this.uiManager = uiManager;
        this.dataFetcher = new DataFetcher();
        this.chartManager = new ChartManager();
        this.tableGenerator = new TableGenerator();
    }

    load() {
        this.uiManager.hideAllSections();
        this.uiManager.showLoader();

        this.dataFetcher.fetchJSON('/query?type=statistics')
            .then(data => {
                if (!Array.isArray(data) || data.length === 0) {
                    throw new Error("Dati delle statistiche non validi o vuoti.");
                }

                this.uiManager.hideLoader();
                this.processStatistics(data);
            })
            .catch(error => {
                this.uiManager.hideLoader();
                console.error("Errore nel caricamento delle statistiche:", error);
            });
    }

    processStatistics(data) {
        const absoluteStats = [];
        const percentageStats = [];

        data.forEach(row => {
            const value = parseFloat(row.Value);
            if (isNaN(value)) return; // Ignora valori non validi

            if (value > 100) {
                absoluteStats.push({ statistic: row.Statistic, value });
            } else {
                percentageStats.push({ statistic: row.Statistic, value });
            }
        });

        this.tableGenerator.createTableFromJSON('resultContainer', data);

        if (document.getElementById("chart1")) {
            this.chartManager.createChart(
                'chart1', 'bar',
                absoluteStats.map(row => row.statistic),
                absoluteStats.map(row => row.value),
                'Valori Assoluti'
            );
        } else {
            console.warn("Canvas 'chart1' non trovato.");
        }

        if (document.getElementById("chart2")) {
            this.chartManager.createChart(
                'chart2', 'bar',
                percentageStats.map(row => row.statistic),
                percentageStats.map(row => row.value),
                'Percentuali'
            );
        } else {
            console.warn("Canvas 'chart2' non trovato.");
        }
    }
}

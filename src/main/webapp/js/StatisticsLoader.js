class StatisticsLoader {
    constructor(uiManager) {
        this.uiManager = uiManager;
        this.dataFetcher = new DataFetcher();
        this.tableGenerator = new TableGenerator();
        this.charts = {}; // Per memorizzare i riferimenti ai grafici
    }

    load() {
        this.uiManager.hideAllSections();
        document.getElementById('mvpContainer')?.classList.add('hidden');

        this.uiManager.showLoader();

        this.dataFetcher.fetchJSON('/query?type=statistics')
            .then(data => {
                this.uiManager.hideLoader();

                if (!data || !Array.isArray(data) || data.length === 0) {
                    console.error("❌ Dati delle statistiche non validi o vuoti.");
                    return;
                }

                console.log("📊 Dati ricevuti per Statistiche:", data);

                // ✅ Mostra il contenitore solo se esiste
                const statsContainer = document.getElementById('statisticsContainer');
                if (statsContainer) {
                    statsContainer.classList.remove('hidden');
                } else {
                    console.error("⚠️ Elemento 'statisticsContainer' non trovato nel DOM.");
                    return;
                }

                this.processStatistics(data);
            })
            .catch(error => {
                this.uiManager.hideLoader();
                console.error("🚨 Errore nel caricamento delle statistiche:", error);
            });
    }

    processStatistics(data) {
        const absoluteStats = [];
        const percentageStats = [];

        data.forEach(row => {
            const value = parseFloat(row.Value);
            if (isNaN(value)) return;

            if (value > 100) {
                absoluteStats.push({ statistic: row.Statistic, value });
            } else {
                percentageStats.push({ statistic: row.Statistic, value });
            }
        });

        // ✅ Mostra o nasconde il contenitore principale
        this.toggleSection("statisticsContainer", absoluteStats.length > 0 || percentageStats.length > 0);

        // ✅ Creazione della tabella
        this.createTableWithDebug("resultContainer", data);

        // ✅ Creazione dei nuovi grafici delle statistiche generali
        this.createChartSafe("statisticsChartAbsolute", "Valori Assoluti", absoluteStats);
        this.createChartSafe("statisticsChartPercentage", "Percentuali", percentageStats);
    }

    /**
     * 📌 Mostra o nasconde una sezione a seconda della presenza di dati
     */
    toggleSection(sectionId, shouldShow) {
        const section = document.getElementById(sectionId);
        if (section) {
            section.classList.toggle("hidden", !shouldShow);
        } else {
            console.warn(`⚠️ Sezione '${sectionId}' non trovata.`);
        }
    }

    /**
     * 📌 Creazione della tabella con debug
     */
    createTableWithDebug(containerId, data) {
        const container = document.getElementById(containerId);
        if (!container) {
            console.warn(`⚠️ Contenitore tabella '${containerId}' non trovato.`);
            return;
        }
        if (!data || data.length === 0) {
            console.warn(`⚠️ Nessun dato disponibile per la tabella ${containerId}`);
            return;
        }
        console.log(`📋 Creazione tabella per ${containerId}`, data);
        this.tableGenerator.createTableFromJSON(containerId, data);
    }

    /**
     * 📌 Creazione sicura di un grafico evitando sovrapposizioni
     */
    createChartSafe(canvasId, label, data) {
        const canvas = document.getElementById(canvasId);
        if (!canvas) {
            console.warn(`⚠️ Canvas '${canvasId}' non trovato.`);
            return;
        }
        if (!data || data.length === 0) {
            console.warn(`⚠️ Nessun dato valido per il grafico ${label}.`);
            return;
        }

        console.log(`📊 Creazione del grafico ${label}`, data);

        // ✅ Cancella il grafico precedente se esiste
        if (this.charts[canvasId]) {
            this.charts[canvasId].destroy();
        }

        const ctx = canvas.getContext('2d');
        this.charts[canvasId] = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: data.map(entry => entry.statistic),
                datasets: [{
                    label: label,
                    data: data.map(entry => entry.value),
                    backgroundColor: 'rgba(52, 152, 219, 0.7)',
                    borderColor: 'rgba(41, 128, 185, 1)',
                    borderWidth: 2,
                    borderRadius: 8,
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: {
                        ticks: { autoSkip: false, maxRotation: 45, minRotation: 0, font: { size: 12 } }
                    },
                    y: {
                        beginAtZero: true,
                        ticks: { font: { size: 14 } }
                    }
                },
                plugins: {
                    legend: {
                        labels: { font: { size: 14, weight: 'bold' } }
                    }
                }
            }
        });
    }
}

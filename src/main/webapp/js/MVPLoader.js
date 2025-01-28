class MVPLoader {
    constructor(uiManager) {
        this.uiManager = uiManager;
        this.dataFetcher = new DataFetcher();
        this.tableGenerator = new TableGenerator();
        this.charts = {}; // Memorizza i grafici per aggiornarli
    }

    load() {
        this.uiManager.hideAllSections();
        this.uiManager.showLoader();

        this.dataFetcher.fetchJSON('/query?type=mvp')
            .then(data => {
                this.uiManager.hideLoader();
                document.getElementById('mvpContainer').classList.remove('hidden');

                console.log("📊 Dati ricevuti per MVP:", data);

                // Pulizia delle sezioni per evitare duplicazioni
                this.clearContainer('mvpTopPlayersAbsolute');
                this.clearContainer('mvpTopPlayersPercentage');
                this.clearContainer('mvpTopWinningOpenings');
                this.clearContainer('mvpTopTournaments');

                // Creazione delle tabelle MVP
                this.createTableWithDebug('mvpTopPlayersAbsolute', data.topPlayersAbsolute);
                this.createTableWithDebug('mvpTopPlayersPercentage', data.topPlayersPercentage);
                this.createTableWithDebug('mvpTopWinningOpenings', data.topWinningOpenings);
                this.createTableWithDebug('mvpTopTournaments', data.topTournaments);

                // Creazione dei grafici con chiavi corrette
                this.createMVPChart('mvpChartAbsolute', data.topPlayersAbsolute, 'Winner', 'WinsAbsolute', 'Giocatori più vincenti');
                this.createMVPChart('mvpChartPercentage', data.topPlayersPercentage, 'player', 'WinPercentage', 'Vittorie percentuali (%)', true);
                this.createMVPChart('mvpChartOpenings', data.topWinningOpenings, 'Opening', 'Wins', 'Aperture più vincenti');
                this.createMVPChart('mvpChartTournaments', data.topTournaments, 'Tournament', 'GamesPlayed', 'Tornei più giocati');
            })
            .catch(error => {
                this.uiManager.hideLoader();
                console.error("❌ Errore nel caricamento delle statistiche MVP:", error);
            });
    }

    clearContainer(containerId) {
        document.getElementById(containerId).innerHTML = "";
    }

    createTableWithDebug(containerId, data) {
        if (!data || data.length === 0) {
            console.warn(`⚠️ Nessun dato disponibile per la tabella ${containerId}`);
            return;
        }
        console.log(`📋 Creazione tabella per ${containerId}`, data);
        this.tableGenerator.createTableFromJSON(containerId, data);


    }

    createMVPChart(canvasId, data, labelKey, valueKey, chartTitle, isPercentage = false) {
        const canvas = document.getElementById(canvasId);
        if (!canvas) {
            console.warn(`⚠️ Canvas non trovato: ${canvasId}`);
            return;
        }
        const ctx = canvas.getContext('2d');

        console.log(`📊 Creazione del grafico ${chartTitle}`);
        console.log("📌 Dati grezzi:", data);

        if (!data || data.length === 0) {
            console.warn(`⚠️ Nessun dato valido per il grafico ${canvasId}`);
            return;
        }

        // ✅ FIX: Usa il nome corretto della chiave per i giocatori
        const labels = data.map(entry => entry[labelKey] || entry["Player"] || "Sconosciuto");
        let values = data.map(entry => parseFloat(entry[valueKey] || 0));

        console.log(`📌 Etichette per ${chartTitle}:`, labels);
        console.log(`📌 Valori per ${chartTitle}:`, values);

        if (isPercentage) {
            values = values.map(v => v / 100);
        }

        if (!labels.length || !values.length || values.every(v => isNaN(v) || v === 0)) {
            console.warn(`⚠️ Nessun dato numerico valido per il grafico ${canvasId}`);
            return;
        }

        if (this.charts[canvasId]) {
            this.charts[canvasId].destroy();
        }

        this.charts[canvasId] = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: chartTitle,
                    data: values,
                    backgroundColor: 'rgba(108, 92, 231, 0.8)',
                    borderColor: 'rgba(108, 92, 231, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: { beginAtZero: true }
                }
            }
        });
    }
}



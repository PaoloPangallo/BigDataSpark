class SpecificQueriesLoader {
    constructor(uiManager) {
        this.uiManager = uiManager;
        this.dataFetcher = new DataFetcher();
        this.tableGenerator = new TableGenerator();
    }

    load() {
        this.uiManager.hideAllSections();
        this.uiManager.showLoader();

        const endpoints = [
            { id: 'queryOpeningWinStats', title: 'Aperture con il maggior numero di vittorie', filename: 'openingWinStats.csv' },
            { id: 'queryRatingDistribution', title: 'Distribuzione delle partite per fascia di rating', filename: 'ratingDistribution.csv' },
            { id: 'queryTimeoutOpenings', title: 'Aperture più utilizzate in partite terminate per Timeout', filename: 'timeoutOpenings.csv' },
            { id: 'queryRatingDiffByOpening', title: 'Differenze di rating medie per apertura', filename: 'ratingDiffByOpening.csv' },
            { id: 'queryDrawByTimeControl', title: 'Percentuale di pareggi per controllo del tempo', filename: 'drawByTimeControl.csv' },
            { id: 'queryWinByRatingDiff', title: 'Percentuale di vittorie per differenza di rating', filename: 'winByRatingDiff.csv' },
        ];

        // Carica tutti gli endpoint
        Promise.all(
            endpoints.map(endpoint =>
                this.dataFetcher.fetchCSV(`/query?filename=${endpoint.filename}`)
                    .then(data => ({ id: endpoint.id, title: endpoint.title, data }))
            )
        )
            .then(results => {
                this.uiManager.hideLoader();
                const resultContainer = document.getElementById('resultContainer');
                resultContainer.innerHTML = ''; // Pulisce il contenitore

                // Mostra i dati come tabelle
                results.forEach(result => {
                    this.tableGenerator.createTableFromCSV(resultContainer, result.data, result.title);
                });
            })
            .catch(error => {
                this.uiManager.hideLoader();
                console.error("Errore nel caricamento delle query specifiche:", error);
            });
    }
}

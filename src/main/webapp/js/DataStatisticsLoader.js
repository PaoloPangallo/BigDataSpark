class DataStatisticsLoader {
    constructor(uiManager) {
        this.uiManager = uiManager;
        this.dataFetcher = new DataFetcher();
        this.tableGenerator = new TableGenerator();
    }

    load() {
        this.uiManager.hideAllSections();
        this.uiManager.showLoader();

        this.dataFetcher.fetchJSON('/query?type=dateStatistics')
            .then(data => {
                this.uiManager.hideLoader();
                const resultContainer = document.getElementById('resultContainer');
                resultContainer.innerHTML = ''; // Pulisce il contenitore

                // Genera tabelle per ogni tipo di statistica
                this.tableGenerator.createTableFromJSON('resultContainer', data.mostGamesDay, 'Giorno con più partite');
                this.tableGenerator.createTableFromJSON('resultContainer', data.mostGamesForOpeningDay, 'Giorno con più partite per apertura');
                this.tableGenerator.createTableFromJSON('resultContainer', data.dayWithMostUniqueOpenings, 'Giorno con più aperture uniche');
            })
            .catch(error => {
                this.uiManager.hideLoader();
                console.error("Errore nel caricamento delle statistiche delle date:", error);
            });
    }
}

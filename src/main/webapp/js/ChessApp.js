class ChessApp {
    constructor() {
        this.uiManager = new UIManager();
        this.statisticsLoader = new StatisticsLoader(this.uiManager);
        this.mvpLoader = new MVPLoader(this.uiManager);
        this.dateStatisticsLoader = new DataStatisticsLoader(this.uiManager);
        this.specificQueriesLoader = new SpecificQueriesLoader(this.uiManager);
        this.dataFetcher = new DataFetcher();
        this.tableGenerator = new TableGenerator();
    }

    initialize() {
        console.log("ChessApp inizializzata");

        // Lista dei pulsanti e delle relative funzioni
        const buttons = [
            { id: 'btnResultCounts', action: () => this.loadData('query?filename=resultCounts.csv') },
            { id: 'btnPopularOpenings', action: () => this.loadData('query?filename=popularOpenings.csv') },
            { id: 'btnAvgRatingDiff', action: () => this.loadData('query?filename=avgRatingDiff.csv') },
            { id: 'btnStatistics', action: () => this.loadStatistics() },
            { id: 'btnMVPStatistics', action: () => this.loadMVPStatistics() },
            { id: 'btnDateStatistics', action: () => this.loadDateStatistics() },
            { id: 'btnSpecificQueries', action: () => this.loadSpecificQueries() }
        ];

        // Aggiunge gli event listener controllando l'esistenza dei pulsanti
        buttons.forEach(({ id, action }) => {
            const button = document.getElementById(id);
            if (button) {
                button.addEventListener('click', () => {
                    this.disableButtonTemporarily(button);
                    action();
                });
            } else {
                console.warn(`ChessApp: Pulsante con ID '${id}' non trovato.`);
            }
        });
    }

    // Metodo per caricare dati generici (es. file CSV)
    async loadData(endpoint) {
        try {
            this.uiManager.hideAllSections();
            this.uiManager.showLoader();

            const resultContainer = document.getElementById('resultContainer');
            const data = await this.dataFetcher.fetchCSV(endpoint);

            this.uiManager.hideLoader();
            if (data) {
                this.tableGenerator.createTableFromCSV(resultContainer, data);
            } else {
                console.warn(`ChessApp: Nessun dato ricevuto da ${endpoint}`);
                resultContainer.innerHTML = '<p>Errore nel caricamento dei dati.</p>';
            }
        } catch (error) {
            this.uiManager.hideLoader();
            console.error(`ChessApp: Errore nel caricamento dei dati da ${endpoint}:`, error);
        }
    }

    async loadStatistics() {
        try {
            await this.statisticsLoader.load();
        } catch (error) {
            console.error("ChessApp: Errore durante il caricamento delle statistiche:", error);
        }
    }

    async loadMVPStatistics() {
        try {
            await this.mvpLoader.load();
        } catch (error) {
            console.error("ChessApp: Errore durante il caricamento delle statistiche MVP:", error);
        }
    }

    async loadDateStatistics() {
        try {
            await this.dateStatisticsLoader.load();
        } catch (error) {
            console.error("ChessApp: Errore durante il caricamento delle statistiche delle date:", error);
        }
    }

    async loadSpecificQueries() {
        try {
            await this.specificQueriesLoader.load();
        } catch (error) {
            console.error("ChessApp: Errore durante il caricamento delle query specifiche:", error);
        }
    }

    // Disabilita temporaneamente un pulsante per evitare doppie richieste
    disableButtonTemporarily(button) {
        button.disabled = true;
        setTimeout(() => button.disabled = false, 1500); // Riabilita dopo 1.5 secondi
    }
}

// Inizializzazione dell'app al caricamento della pagina
window.onload = () => {
    const app = new ChessApp();
    app.initialize();
};

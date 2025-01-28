class ChartManager {
    constructor() {
        this.charts = {}; // Oggetto per tenere traccia dei grafici creati
    }

    /**
     * Crea un grafico su un canvas esistente.
     * @param {string} canvasId - L'ID del canvas HTML su cui disegnare il grafico.
     * @param {string} type - Il tipo di grafico (es. 'bar', 'line', 'pie').
     * @param {Array} labels - Le etichette da visualizzare sull'asse X.
     * @param {Array} data - I valori dei dati da rappresentare.
     * @param {string} label - L'etichetta della serie di dati.
     */
    createChart(canvasId, type, labels, data, label) {
        const canvasElement = document.getElementById(canvasId);
        if (!canvasElement) {
            console.warn(`ChartManager: Nessun elemento trovato con ID '${canvasId}'.`);
            return;
        }

        const ctx = canvasElement.getContext('2d');

        // Se esiste già un grafico in questo canvas, distruggilo prima di crearne uno nuovo
        if (this.charts[canvasId]) {
            this.charts[canvasId].destroy();
        }

        this.charts[canvasId] = new Chart(ctx, {
            type: type,
            data: {
                labels: labels,
                datasets: [{
                    label: label,
                    data: data,
                    backgroundColor: this.getRandomColors(data.length),
                    borderColor: '#2d3436',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: true }
                }
            }
        });
    }

    /**
     * Metodo per rimuovere un grafico specifico.
     * @param {string} canvasId - L'ID del canvas il cui grafico deve essere distrutto.
     */
    destroyChart(canvasId) {
        if (this.charts[canvasId]) {
            this.charts[canvasId].destroy();
            delete this.charts[canvasId];
            console.log(`ChartManager: Grafico con ID '${canvasId}' rimosso.`);
        } else {
            console.warn(`ChartManager: Nessun grafico trovato per ID '${canvasId}'.`);
        }
    }

    /**
     * Metodo per rimuovere tutti i grafici attivi.
     */
    destroyAllCharts() {
        Object.keys(this.charts).forEach(canvasId => {
            this.charts[canvasId].destroy();
        });
        this.charts = {};
        console.log("ChartManager: Tutti i grafici sono stati rimossi.");
    }

    /**
     * Genera una serie di colori casuali per i dataset.
     * @param {number} count - Il numero di colori da generare.
     * @returns {Array} - Un array di colori in formato rgba.
     */
    getRandomColors(count) {
        const colors = [
            'rgba(75, 192, 192, 0.6)',
            'rgba(255, 99, 132, 0.6)',
            'rgba(255, 206, 86, 0.6)',
            'rgba(54, 162, 235, 0.6)',
            'rgba(153, 102, 255, 0.6)',
            'rgba(201, 203, 207, 0.6)'
        ];
        return Array.from({ length: count }, (_, i) => colors[i % colors.length]);
    }
}

// Rende la classe disponibile globalmente se non si usa il modulo ES6
window.ChartManager = ChartManager;

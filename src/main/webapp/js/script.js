// Funzione per creare una tabella dinamica dai dati CSV
function createTableFromCSV(csvData) {
    const rows = csvData.trim().split("\n");
    const headers = rows[0].split(",");
    const table = document.createElement("table");
    table.classList.add("data-table");

    // Creazione dell'intestazione
    const thead = table.createTHead();
    const headerRow = thead.insertRow();
    headers.forEach(header => {
        const th = document.createElement("th");
        th.textContent = header.trim();
        headerRow.appendChild(th);
    });

    // Creazione del corpo della tabella
    const tbody = table.createTBody();
    rows.slice(1).forEach(row => {
        const rowData = row.split(",");
        const tableRow = tbody.insertRow();
        rowData.forEach(cell => {
            const td = document.createElement("td");
            td.textContent = cell.trim();
            tableRow.appendChild(td);
        });
    });

    return table;
}

let chart1; // Variabile per il grafico 1
let chart2; // Variabile per il grafico 2

// Funzione per nascondere le statistiche (grafici e tabella)
function hideStatistics() {
    document.getElementById('resultContainer').innerHTML = ''; // Svuota la tabella
    document.getElementById('chartContainer1').classList.add('hidden'); // Nasconde il grafico 1
    document.getElementById('chartContainer2').classList.add('hidden'); // Nasconde il grafico 2

    // Distruggi i grafici esistenti
    if (chart1) {
        chart1.destroy();
        chart1 = null;
    }
    if (chart2) {
        chart2.destroy();
        chart2 = null;
    }
}

// Funzione per caricare le statistiche
function loadStatistics() {
    // Nasconde eventuali dati generici
    hideStatistics();

    const loader = document.getElementById('loader');
    const resultContainer = document.getElementById('resultContainer');
    const chartContainer1 = document.getElementById('chartContainer1');
    const chartContainer2 = document.getElementById('chartContainer2');

    // Mostra i contenitori per i grafici
    chartContainer1.classList.remove('hidden');
    chartContainer2.classList.remove('hidden');

    loader.classList.remove('hidden');
    resultContainer.innerHTML = '';

    fetch('/query?type=statistics')
        .then(response => {
            if (!response.ok) {
                throw new Error("Errore durante il recupero delle statistiche");
            }
            return response.json();
        })
        .then(data => {
            console.log("Dati ricevuti dal server:", data);

            loader.classList.add('hidden');

            // Dividi i dati in valori assoluti e percentuali
            const absoluteStats = [];
            const percentageStats = [];

            data.forEach(row => {
                if (row.Value !== undefined && row.Value !== null) {
                    row.Value = parseFloat(row.Value.trim().replace("\r", ""));
                    if (row.Value > 100) {
                        absoluteStats.push(row);
                    } else {
                        percentageStats.push(row);
                    }
                }
            });

            // Tabella HTML
            let html = '<table class="data-table"><tr><th>Statistic</th><th>Value</th></tr>';
            data.forEach(row => {
                html += `<tr><td>${row.Statistic}</td><td>${row.Value.toFixed(2)}</td></tr>`;
            });
            html += '</table>';
            resultContainer.innerHTML = html;

            // Distruggi i grafici esistenti
            if (chart1) {
                chart1.destroy();
            }
            if (chart2) {
                chart2.destroy();
            }

            // Grafico per valori assoluti
            chart1 = new Chart(document.getElementById('chart1').getContext('2d'), {
                type: 'bar',
                data: {
                    labels: absoluteStats.map(row => row.Statistic),
                    datasets: [{
                        label: 'Valori Assoluti',
                        data: absoluteStats.map(row => row.Value),
                        backgroundColor: ['#74b9ff', '#a29bfe', '#fd79a8', '#55efc4'],
                    }]
                },
                options: {
                    responsive: true,
                    plugins: { legend: { display: false } },
                    scales: { y: { beginAtZero: true } }
                }
            });

            // Grafico per percentuali
            chart2 = new Chart(document.getElementById('chart2').getContext('2d'), {
                type: 'bar',
                data: {
                    labels: percentageStats.map(row => row.Statistic),
                    datasets: [{
                        label: 'Percentuali',
                        data: percentageStats.map(row => row.Value),
                        backgroundColor: ['#fab1a0', '#e17055', '#ffeaa7'],
                    }]
                },
                options: {
                    responsive: true,
                    plugins: { legend: { display: false } },
                    scales: { y: { beginAtZero: true } }
                }
            });
        })
        .catch(error => {
            loader.classList.add('hidden');
            resultContainer.innerHTML = 'Errore nel caricamento delle statistiche.';
            console.error("Errore:", error);
        });
}

// Funzione per caricare dati generici (CSV)
function loadData(endpoint) {
    // Nasconde le statistiche se si clicca su un altro bottone
    hideStatistics();

    const resultContainer = document.getElementById("resultContainer");
    const loader = document.getElementById("loader");

    loader.classList.remove("hidden");
    resultContainer.innerHTML = "";

    fetch(endpoint)
        .then(response => {
            if (!response.ok) {
                throw new Error("Errore nel recupero dei dati");
            }
            return response.text();
        })
        .then(data => {
            console.log("Dati ricevuti:", data);
            loader.classList.add("hidden");

            const table = createTableFromCSV(data);
            resultContainer.innerHTML = "";
            resultContainer.appendChild(table);
        })
        .catch(error => {
            console.error("Errore:", error);
            loader.classList.add("hidden");
            resultContainer.innerHTML = "Errore nel recupero dei dati.";
        });
}

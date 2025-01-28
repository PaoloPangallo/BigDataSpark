// Variabili globali per i grafici
let chart1 = null;
let chart2 = null;

// Funzione per nascondere tutte le sezioni e resettare le viste
function hideAllSections() {
    document.getElementById('resultContainer').innerHTML = '';
    document.getElementById('chartSection1').classList.add('hidden');
    document.getElementById('chartSection2').classList.add('hidden');
    document.getElementById('mvpContainer').classList.add('hidden');

    // Distruggi i grafici esistenti, se presenti
    if (chart1) {
        chart1.destroy();
        chart1 = null;
    }
    if (chart2) {
        chart2.destroy();
        chart2 = null;
    }
}

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
        th.textContent = header.trim().replace(/"/g, ""); // Rimuove le virgolette

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

// Funzione per caricare statistiche generali
function loadStatistics() {
    hideAllSections(); // Nascondi tutto prima di caricare nuove statistiche

    const loader = document.getElementById('loader');
    const resultContainer = document.getElementById('resultContainer');
    const chartContainer1 = document.getElementById('chart1').getContext('2d');
    const chartContainer2 = document.getElementById('chart2').getContext('2d');

    loader.classList.remove('hidden');
    resultContainer.innerHTML = '';

    fetch('/query?type=statistics')
        .then(response => {
            if (!response.ok) throw new Error("Errore durante il recupero delle statistiche");
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
                    const value = parseFloat(row.Value.trim());
                    if (value > 100) {
                        absoluteStats.push(row);
                    } else {
                        percentageStats.push(row);
                    }
                }
            });

            // Visualizza tabella
            let html = '<table class="data-table"><tr><th>Statistic</th><th>Value</th></tr>';
            data.forEach(row => {
                html += `<tr><td>${row.Statistic}</td><td>${row.Value}</td></tr>`;
            });
            html += '</table>';
            resultContainer.innerHTML = html;

            // Grafico per valori assoluti
            chart1 = new Chart(chartContainer1, {
                type: 'bar',
                data: {
                    labels: absoluteStats.map(row => row.Statistic),
                    datasets: [{
                        label: 'Valori Assoluti',
                        data: absoluteStats.map(row => row.Value),
                        backgroundColor: '#74b9ff'
                    }]
                },
                options: { responsive: true, plugins: { legend: { display: false } } }
            });

            // Grafico per percentuali
            chart2 = new Chart(chartContainer2, {
                type: 'bar',
                data: {
                    labels: percentageStats.map(row => row.Statistic),
                    datasets: [{
                        label: 'Percentuali',
                        data: percentageStats.map(row => row.Value),
                        backgroundColor: '#fab1a0'
                    }]
                },
                options: { responsive: true, plugins: { legend: { display: false } } }
            });
        })
        .catch(error => {
            loader.classList.add('hidden');
            resultContainer.innerHTML = 'Errore nel caricamento delle statistiche.';
            console.error("Errore:", error);
        });
}

// Funzione per caricare statistiche MVP
function loadMVPStatistics() {
    hideAllSections(); // Nascondi tutto prima di caricare le statistiche MVP

    const loader = document.getElementById('loader');
    const mvpContainer = document.getElementById('mvpContainer');
    loader.classList.remove('hidden');

    fetch('/query?type=mvp')
        .then(response => {
            if (!response.ok) throw new Error("Errore durante il recupero delle statistiche MVP");
            return response.json();
        })
        .then(data => {
            console.log("Dati MVP ricevuti:", data);

            loader.classList.add('hidden');
            mvpContainer.classList.remove('hidden');

            // Visualizza le tabelle MVP
            populateTable('mvpTopPlayersAbsolute', 'Giocatori più vincenti (assoluto)', data.topPlayersAbsolute);
            populateTable('mvpTopPlayersPercentage', 'Giocatori con maggior percentuale di vittorie', data.topPlayersPercentage);
            populateTable('mvpTopWinningOpenings', 'Aperture più vincenti', data.topWinningOpenings);
            populateTable('mvpTopTournaments', 'Tornei più giocati', data.topTournaments);
        })
        .catch(error => {
            loader.classList.add('hidden');
            alert("Errore nel caricamento delle statistiche MVP.");
            console.error("Errore:", error);
        });
}

// Funzione per popolare una tabella con dati JSON
function populateTable(containerId, title, data) {
    const container = document.getElementById(containerId);
    if (!data || data.length === 0) {
        container.innerHTML = `<h3>${title}</h3><p>Nessun dato disponibile</p>`;
        return;
    }

    let html = `<h3>${title}</h3><table class="data-table"><tr>`;
    const headers = Object.keys(data[0]);
    headers.forEach(header => {
        html += `<th>${header}</th>`;
    });
    html += '</tr>';

    data.forEach(row => {
        html += '<tr>';
        headers.forEach(header => {
            html += `<td>${row[header]}</td>`;
        });
        html += '</tr>';
    });
    html += '</table>';
    container.innerHTML = html;
}

// Funzione per caricare dati generici
function loadData(endpoint) {
    hideAllSections(); // Nascondi tutto prima di caricare nuovi dati

    const loader = document.getElementById("loader");
    const resultContainer = document.getElementById("resultContainer");

    loader.classList.remove("hidden");
    resultContainer.innerHTML = '';

    fetch(endpoint)
        .then(response => {
            if (!response.ok) throw new Error("Errore nel recupero dei dati");
            return response.text();
        })
        .then(data => {
            loader.classList.add("hidden");
            const table = createTableFromCSV(data);
            resultContainer.appendChild(table);
        })
        .catch(error => {
            loader.classList.add("hidden");
            resultContainer.innerHTML = "Errore nel recupero dei dati.";
            console.error("Errore:", error);
        });
}

// Funzione per caricare statistiche delle date
function loadDateStatistics() {
    console.log("Funzione loadDateStatistics chiamata");
    hideAllSections(); // Nascondi tutte le sezioni prima di caricare le statistiche delle date

    const loader = document.getElementById('loader');
    const resultContainer = document.getElementById('resultContainer');

    // Mostra il loader
    loader.classList.remove('hidden');

    // Effettua la richiesta all'endpoint delle statistiche delle date
    fetch('/query?type=dateStatistics')
        .then(response => {
            if (!response.ok) throw new Error("Errore durante il recupero delle statistiche delle date");
            return response.json();
        })
        .then(data => {
            console.log("Dati delle statistiche delle date ricevuti:", data);

            loader.classList.add('hidden');
            resultContainer.innerHTML = ''; // Pulisci il contenitore prima di aggiungere i dati

            // Crea una tabella per ogni gruppo di statistiche
            let html = '';

            // Tabella per mostGamesDay
            html += '<h3>Giorno con più partite</h3>';
            html += '<table class="data-table"><tr><th>Day</th><th>TotalGames</th></tr>';
            data.mostGamesDay.forEach(row => {
                html += `<tr><td>${row.Day}</td><td>${row.TotalGames}</td></tr>`;
            });
            html += '</table>';

            // Tabella per mostGamesForOpeningDay
            html += '<h3>Giorno con più partite per apertura</h3>';
            html += '<table class="data-table"><tr><th>Day</th><th>Apertura siciliana</th></tr>';
            data.mostGamesForOpeningDay.forEach(row => {
                html += `<tr><td>${row.Day}</td><td>${row["Apertura siciliana"]}</td></tr>`;
            });
            html += '</table>';

            // Tabella per dayWithMostUniqueOpenings
            html += '<h3>Giorno con più aperture uniche</h3>';
            html += '<table class="data-table"><tr><th>Day</th><th>UniqueOpenings</th></tr>';
            data.dayWithMostUniqueOpenings.forEach(row => {
                html += `<tr><td>${row.Day}</td><td>${row.UniqueOpenings}</td></tr>`;
            });
            html += '</table>';

            resultContainer.innerHTML = html; // Mostra i dati nella pagina
        })
        .catch(error => {
            loader.classList.add('hidden');
            resultContainer.innerHTML = "Errore nel caricamento delle statistiche delle date.";
            console.error("Errore:", error);
        });
}

// Funzione per caricare query specifiche
function loadSpecificQueries() {
    hideAllSections(); // Nascondi tutte le sezioni esistenti prima di caricare le query specifiche

    const loader = document.getElementById('loader');
    const resultContainer = document.getElementById('resultContainer');

    // Mostra il loader
    loader.classList.remove('hidden');

    // Elenco dei file CSV da caricare
    const endpoints = [
        { id: 'queryOpeningWinStats', title: 'Aperture con il maggior numero di vittorie', filename: 'openingWinStats.csv' },
        { id: 'queryRatingDistribution', title: 'Distribuzione delle partite per fascia di rating', filename: 'ratingDistribution.csv' },
        { id: 'queryTimeoutOpenings', title: 'Aperture più utilizzate in partite terminate per Timeout', filename: 'timeoutOpenings.csv' },
        { id: 'queryRatingDiffByOpening', title: 'Differenze di rating medie per apertura', filename: 'ratingDiffByOpening.csv' },
        { id: 'queryDrawByTimeControl', title: 'Percentuale di pareggi per controllo del tempo', filename: 'drawByTimeControl.csv' },
        { id: 'queryWinByRatingDiff', title: 'Percentuale di vittorie per differenza di rating', filename: 'winByRatingDiff.csv' },
    ];

    // Carica ogni file CSV e mostra i risultati
    Promise.all(
        endpoints.map(endpoint =>
            fetch(`/query?filename=${endpoint.filename}`)
                .then(response => {
                    if (!response.ok) throw new Error(`Errore nel caricamento di ${endpoint.filename}`);
                    return response.text();
                })
                .then(data => ({ id: endpoint.id, title: endpoint.title, data }))
        )
    )
        .then(results => {
            loader.classList.add('hidden');
            resultContainer.innerHTML = ''; // Pulisci il contenitore dei risultati

            results.forEach(result => {
                const section = document.createElement('div');
                section.classList.add('query-section');
                section.innerHTML = `<h3>${result.title}</h3>`;
                const table = createTableFromCSV(result.data);
                section.appendChild(table);
                resultContainer.appendChild(section);
            });
        })
        .catch(error => {
            loader.classList.add('hidden');
            resultContainer.innerHTML = 'Errore nel caricamento delle query specifiche.';
            console.error('Errore:', error);
        });
}


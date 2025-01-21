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

// Modifica della funzione loadData
function loadData(endpoint) {
    const resultContainer = document.getElementById("resultContainer");
    const loader = document.getElementById("loader");

    // Mostra il loader
    loader.classList.remove("hidden");
    resultContainer.innerHTML = "";

    // Effettua una richiesta al server
    fetch(endpoint)
        .then(response => {
            if (!response.ok) {
                throw new Error("Errore nel recupero dei dati");
            }
            return response.text(); // Legge i dati come testo
        })
        .then(data => {
            console.log("Dati ricevuti:", data); // Log dei dati
            loader.classList.add("hidden");

            // Mostra i dati in una tabella
            const table = createTableFromCSV(data);
            resultContainer.innerHTML = "";
            resultContainer.appendChild(table);
        })
        .catch(error => {
            console.error("Errore:", error); // Log degli errori
            loader.classList.add("hidden");
            resultContainer.innerHTML = "Errore nel recupero dei dati.";
        });
}

// Funzione per caricare i dati da un endpoint
async function loadData(endpoint) {
    // Seleziona il contenitore dei risultati
    const resultContainer = document.getElementById('resultContainer');
    resultContainer.innerHTML = "<p style='color: gray;'>Caricamento in corso...</p>"; // Mostra un messaggio di caricamento

    try {
        // Effettua una richiesta al server
        const response = await fetch(endpoint);

        if (!response.ok) {
            throw new Error(`Errore ${response.status}: ${response.statusText}`);
        }

        // Legge i dati come testo
        const data = await response.text();
        console.log("Dati ricevuti:", data); // Logga i dati nella console per debugging

        // Visualizza i dati nel contenitore in formato tabellare se possibile
        if (endpoint.endsWith(".csv")) {
            resultContainer.innerHTML = formatCSVToHTMLTable(data);
        } else {
            resultContainer.innerHTML = `<pre>${data}</pre>`; // Visualizza i dati come testo
        }
    } catch (error) {
        console.error("Errore:", error); // Logga gli errori nella console
        resultContainer.innerHTML = "<p style='color: red;'>Errore nel recupero dei dati.</p>"; // Mostra un messaggio di errore
    }
}

// Funzione per convertire il contenuto CSV in una tabella HTML
function formatCSVToHTMLTable(csvData) {
    const rows = csvData.split("\n");
    if (rows.length === 0) return "<p style='color: red;'>Nessun dato disponibile.</p>";

    const header = rows[0].split(",");
    const tableRows = rows.slice(1).map(row => {
        const cells = row.split(",");
        return `<tr>${cells.map(cell => `<td>${cell.trim()}</td>`).join("")}</tr>`;
    });

    // Costruisce la tabella HTML
    return `
        <table style="width: 100%; border-collapse: collapse; text-align: left;">
            <thead style="background-color: #6200ea; color: white;">
                <tr>${header.map(col => `<th style="padding: 8px; border: 1px solid #ddd;">${col.trim()}</th>`).join("")}</tr>
            </thead>
            <tbody>
                ${tableRows.join("")}
            </tbody>
        </table>
    `;
}

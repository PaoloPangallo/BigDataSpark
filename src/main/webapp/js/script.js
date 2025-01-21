// Funzione per caricare i dati da un endpoint
function loadData(endpoint) {
    // Seleziona il contenitore dei risultati
    const resultContainer = document.getElementById('resultContainer');
    resultContainer.innerHTML = "Caricamento in corso..."; // Mostra un messaggio di caricamento

    // Effettua una richiesta al server
    fetch(endpoint)
        .then(response => {
            if (!response.ok) {
                throw new Error("Errore nel recupero dei dati");
            }
            return response.text(); // Legge i dati come testo
        })
        .then(data => {
            console.log("Dati ricevuti:", data); // Logga i dati nella console per debugging
            // Mostra i dati ricevuti nella pagina
            resultContainer.innerHTML = "<pre>" + data + "</pre>"; // Visualizza i dati come testo
        })
        .catch(error => {
            console.error("Errore:", error); // Logga gli errori nella console
            resultContainer.innerHTML = "Errore nel recupero dei dati."; // Mostra un messaggio di errore
        });
}

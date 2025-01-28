class DataFetcher {
    async fetchJSON(url, options = {}) {
        try {
            const response = await Promise.race([
                fetch(url, options),
                this.timeout(10000) // Timeout di 10 secondi
            ]);

            if (!response.ok) throw new Error(`Errore HTTP ${response.status}: ${response.statusText}`);

            return await response.json();
        } catch (error) {
            console.error(`DataFetcher: Errore nel recupero JSON da ${url}:`, error);
            return null; // Ritorna `null` invece di interrompere l'esecuzione
        }
    }

    async fetchCSV(url, options = {}) {
        try {
            const response = await Promise.race([
                fetch(url, options),
                this.timeout(10000) // Timeout di 10 secondi
            ]);

            if (!response.ok) throw new Error(`Errore HTTP ${response.status}: ${response.statusText}`);

            return await response.text();
        } catch (error) {
            console.error(`DataFetcher: Errore nel recupero CSV da ${url}:`, error);
            return null;
        }
    }

    timeout(ms) {
        return new Promise((_, reject) =>
            setTimeout(() => reject(new Error("Timeout nella richiesta al server")), ms)
        );
    }
}

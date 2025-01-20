document.addEventListener('DOMContentLoaded', () => {
    const button = document.getElementById('testButton');
    button.addEventListener('click', async () => {
        try {
            const response = await fetch('/api/test'); // Modifica con l'endpoint corretto
            if (!response.ok) {
                throw new Error('Errore nella risposta');
            }
            const data = await response.json();
            alert(`Risultato API: ${data.message}`);
        } catch (error) {
            console.error('Errore durante la richiesta:', error);
            alert('Qualcosa è andato storto.');
        }
    });
});

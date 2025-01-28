class UIManager {
    constructor() {
        this.resultContainer = document.getElementById('resultContainer');
        this.loader = document.getElementById('loader');
    }

    hideAllSections() {
        this.resultContainer.innerHTML = '';
        document.querySelectorAll('.chart-section, #mvpContainer, #dateStatisticsContainer, #specificQueriesContainer')
            .forEach(section => section.classList.add('hidden'));
    }

    showLoader() {
        this.loader.classList.remove('hidden');
    }

    hideLoader() {
        this.loader.classList.add('hidden');
    }

    showSection(sectionId) {
        this.hideAllSections();
        const section = document.getElementById(sectionId);
        if (section) {
            section.classList.remove('hidden');
        } else {
            console.error(`UIManager: Nessuna sezione trovata con ID '${sectionId}'`);
        }
    }
}

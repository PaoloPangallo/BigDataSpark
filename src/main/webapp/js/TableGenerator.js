class TableGenerator {
    createTableFromJSON(containerId, jsonData, title = '') {
        const container = document.getElementById(containerId);
        let html = title ? `<h3>${title}</h3>` : '';
        html += '<table class="data-table"><tr>';

        const headers = Object.keys(jsonData[0]);
        headers.forEach(header => {
            html += `<th>${header}</th>`;
        });
        html += '</tr>';

        jsonData.forEach(row => {
            html += '<tr>';
            headers.forEach(header => {
                html += `<td>${row[header]}</td>`;
            });
            html += '</tr>';
        });

        html += '</table>';
        container.innerHTML += html;
    }

    createTableFromCSV(container, csvData, title = '') {
        const rows = csvData.trim().split("\n");
        const headers = rows[0].split(",");
        let html = title ? `<h3>${title}</h3>` : '';
        html += '<table class="data-table"><tr>';

        headers.forEach(header => {
            html += `<th>${header.trim()}</th>`;
        });
        html += '</tr>';

        rows.slice(1).forEach(row => {
            const cells = row.split(",");
            html += '<tr>';
            cells.forEach(cell => {
                html += `<td>${cell.trim()}</td>`;
            });
            html += '</tr>';
        });

        html += '</table>';
        container.innerHTML += html;
    }
}

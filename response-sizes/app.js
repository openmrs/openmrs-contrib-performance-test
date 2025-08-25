let tableData = [];
let currentSort = {
    columnKey: 'name', // Start with an initial sort by name
    direction: 'asc'
};

document.addEventListener('DOMContentLoaded', () => {
    loadReport();
});

async function loadReport() {
    const reportContainer = document.getElementById('reportContainer');
    // Ensure this path is correct for your project structure
    const csvFilePath = 'response_sizes.csv';

    try {
        const response = await fetch(csvFilePath);
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}. Could not find or access '${csvFilePath}'.`);
        }
        const text = await response.text();
        const data = parseCSV(text);
        if (data.length === 0) {
            throw new Error("CSV file is empty or could not be parsed.");
        }

        // Store the calculated stats globally
        tableData = calculateStatistics(data);
        // Initial display of the report
        displayReport(tableData);

    } catch (error) {
        console.error('Failed to load report:', error);
        reportContainer.innerHTML = `<div class="error-message">Failed to load report. Please check the console for details.</div>`;
    }
}

function displayReport(stats) {
    const reportContainer = document.getElementById('reportContainer');
    reportContainer.innerHTML = ''; // Clear loading message

    const table = document.createElement('table');
    const thead = document.createElement('thead');
    const tbody = document.createElement('tbody');
    tbody.id = 'report-tbody';

    // CHANGE 1: Updated header configuration with new names and 'mean' column
    const headerConfig = [
        { text: 'Request Name', key: 'name', type: 'string' },
        { text: 'Total Requests', key: 'count', type: 'number' },
        { text: 'Min', key: 'min', type: 'number' },
        { text: 'Max', key: 'max', type: 'number' },
        { text: 'Mean', key: 'mean', type: 'number' },
        { text: '50th pct', key: 'p50', type: 'number' },
        { text: '75th pct', key: 'p75', type: 'number' },
        { text: '95th pct', key: 'p95', type: 'number' },
        { text: '99th pct', key: 'p99', type: 'number' }
    ];

    const headerRow = document.createElement('tr');
    headerConfig.forEach(config => {
        const th = document.createElement('th');
        th.textContent = config.text;
        th.dataset.key = config.key;
        th.dataset.type = config.type;
        th.classList.add('sortable');

        th.addEventListener('click', () => {
            sortTable(config.key, config.type);
        });

        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);

    table.appendChild(thead);
    table.appendChild(tbody);
    reportContainer.appendChild(table);

    renderTableBody(stats);
    updateHeaderStyles();
}

function sortTable(key, type) {
    if (currentSort.columnKey === key) {
        currentSort.direction = currentSort.direction === 'asc' ? 'desc' : 'asc';
    } else {
        currentSort.columnKey = key;
        currentSort.direction = 'asc';
    }

    tableData.sort((a, b) => {
        const valA = a[key];
        const valB = b[key];
        let comparison = 0;

        if (type === 'string') {
            comparison = valA.localeCompare(valB);
        } else { // type === 'number'
            comparison = valA - valB;
        }

        return comparison * (currentSort.direction === 'asc' ? 1 : -1);
    });

    renderTableBody(tableData);
    updateHeaderStyles();
}

// CHANGE 3 (Helper Function): Formats byte values into KB strings
function formatBytesToKB(bytes) {
    if (typeof bytes !== 'number' || isNaN(bytes)) {
        return 'N/A';
    }
    const kilobytes = bytes / 1024;
    // Using toFixed(2) to show two decimal places for precision
    return `${kilobytes.toFixed(2)} KB`;
}

function renderTableBody(stats) {
    const tbody = document.getElementById('report-tbody');
    tbody.innerHTML = '';

    stats.forEach(stat => {
        const row = document.createElement('tr');
        // CHANGES 2 & 3: Applied right-alignment and KB formatting
        row.innerHTML = `
            <td class="text-left">${stat.name}</td>
            <td class="text-right">${stat.count}</td>
            <td class="text-right">${formatBytesToKB(stat.min)}</td>
            <td class="text-right">${formatBytesToKB(stat.max)}</td>
            <td class="text-right">${formatBytesToKB(stat.mean)}</td>
            <td class="text-right">${formatBytesToKB(stat.p50)}</td>
            <td class="text-right">${formatBytesToKB(stat.p75)}</td>
            <td class="text-right">${formatBytesToKB(stat.p95)}</td>
            <td class="text-right">${formatBytesToKB(stat.p99)}</td>
        `;
        tbody.appendChild(row);
    });
}

function updateHeaderStyles() {
    document.querySelectorAll('#reportContainer th.sortable').forEach(th => {
        th.classList.remove('sorted-asc', 'sorted-desc');
        if (th.dataset.key === currentSort.columnKey) {
            th.classList.add(currentSort.direction === 'asc' ? 'sorted-asc' : 'sorted-desc');
        }
    });
}

function parseCSV(text) {
    const lines = text.trim().split(/\r?\n/);
    if (lines.length < 2) return [];

    const headers = lines[0].split(',');
    const records = [];

    for (let i = 1; i < lines.length; i++) {
        const values = lines[i].split(',');
        if (values.length === headers.length) {
            let record = {};
            headers.forEach((header, index) => {
                const value = values[index].replace(/"/g, '').trim();
                record[header.trim()] = value;
            });
            records.push(record);
        }
    }
    return records;
}

function calculateStatistics(data) {
    const groups = data.reduce((acc, record) => {
        const name = record.RequestName;
        const size = parseInt(record['ResponseSize(Bytes)'], 10);

        if (!name || isNaN(size)) return acc;

        if (!acc[name]) {
            acc[name] = [];
        }
        acc[name].push(size);
        return acc;
    }, {});

    const results = [];
    for (const name in groups) {
        const sizes = groups[name].sort((a, b) => a - b);

        // CHANGE 1: Added calculation for mean
        const sum = sizes.reduce((a, b) => a + b, 0);
        const mean = sizes.length > 0 ? sum / sizes.length : 0;

        results.push({
            name: name,
            count: sizes.length,
            min: sizes[0],
            max: sizes[sizes.length - 1],
            mean: mean, // Added mean to the result object
            p50: calculatePercentile(sizes, 50),
            p75: calculatePercentile(sizes, 75),
            p95: calculatePercentile(sizes, 95),
            p99: calculatePercentile(sizes, 99),
        });
    }
    // Initial sort by name (ascending)
    return results.sort((a, b) => a.name.localeCompare(b.name));
}

function calculatePercentile(sortedArr, percentile) {
    if (sortedArr.length === 0) return 0;
    const index = (percentile / 100) * (sortedArr.length - 1);
    const lower = Math.floor(index);
    const upper = Math.ceil(index);

    if (lower === upper) {
        return sortedArr[index];
    }
    // Interpolation for more accuracy
    return sortedArr[lower] * (upper - index) + sortedArr[upper] * (index - lower);
}
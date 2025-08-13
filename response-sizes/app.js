let tableData = [];
let currentSort = {
    columnKey: null,
    direction: 'asc'
};

document.addEventListener('DOMContentLoaded', () => {
    loadReport();
});

async function loadReport() {
    const reportContainer = document.getElementById('reportContainer');
    const csvFilePath = 'response_sizes.csv'; // Or your correct path

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
        reportContainer.innerHTML = `<div class="error-message">...</div>`; // Your existing error handling
    }
}

function displayReport(stats) {
    const reportContainer = document.getElementById('reportContainer');
    reportContainer.innerHTML = ''; // Clear loading message

    const table = document.createElement('table');
    const thead = document.createElement('thead');
    const tbody = document.createElement('tbody');
    tbody.id = 'report-tbody'; // Give tbody an ID for easy access

    const headerConfig = [
        { text: 'Request Name', key: 'name', type: 'string' },
        { text: 'Total Requests', key: 'count', type: 'number' },
        { text: 'Min', key: 'min', type: 'number' },
        { text: 'Max', key: 'max', type: 'number' },
        { text: 'Median (P50)', key: 'p50', type: 'number' },
        { text: 'P75', key: 'p75', type: 'number' },
        { text: 'P95', key: 'p95', type: 'number' },
        { text: 'P99', key: 'p99', type: 'number' }
    ];

    const headerRow = document.createElement('tr');
    headerConfig.forEach(config => {
        const th = document.createElement('th');
        th.textContent = config.text;
        th.dataset.key = config.key; // Store the key for sorting
        th.dataset.type = config.type; // Store the data type
        th.classList.add('sortable'); // Add class to apply sorting styles

        // Add click event listener to each header
        th.addEventListener('click', () => {
            sortTable(config.key, config.type);
        });

        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);

    table.appendChild(thead);
    table.appendChild(tbody); // Append empty tbody
    reportContainer.appendChild(table);

    renderTableBody(stats); // Render the initial data
    updateHeaderStyles(); // Set initial header styles
}

function sortTable(key, type) {
    // Determine sort direction
    if (currentSort.columnKey === key) {
        currentSort.direction = currentSort.direction === 'asc' ? 'desc' : 'asc';
    } else {
        currentSort.columnKey = key;
        currentSort.direction = 'asc';
    }

    // Sort the global data array
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

    // Re-render the table body with sorted data
    renderTableBody(tableData);
    // Update the header styles to show the sort arrow
    updateHeaderStyles();
}

function renderTableBody(stats) {
    const tbody = document.getElementById('report-tbody');
    tbody.innerHTML = ''; // Clear existing rows

    stats.forEach(stat => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td class="text-left">${stat.name}</td>
            <td class="text-right">${stat.count}</td>
            <td class="text-right">${stat.min}</td>
            <td class="text-right">${stat.max}</td>
            <td class="text-right">${stat.p50}</td>
            <td class="text-right">${stat.p75}</td>
            <td class="text-right">${stat.p95}</td>
            <td class="text-right">${stat.p99}</td>
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

// --- Your existing helper functions (no changes needed) ---
function parseCSV(text) { /* ... same as before ... */ }
function calculateStatistics(data) { /* ... same as before ... */ }
function calculatePercentile(sortedArr, percentile) { /* ... same as before ... */ }

// --- PASTE YOUR EXISTING HELPER FUNCTIONS HERE ---
// To keep this block clean, I've omitted them, but you should
// ensure parseCSV, calculateStatistics, and calculatePercentile are still here.
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
        results.push({
            name: name,
            count: sizes.length,
            min: sizes[0],
            max: sizes[sizes.length - 1],
            p50: calculatePercentile(sizes, 50),
            p75: calculatePercentile(sizes, 75),
            p95: calculatePercentile(sizes, 95),
            p99: calculatePercentile(sizes, 99),
        });
    }
    return results.sort((a, b) => a.name.localeCompare(b.name)); // Initial sort by name
}

function calculatePercentile(sortedArr, percentile) {
    if (sortedArr.length === 0) return 0;
    const index = (percentile / 100) * (sortedArr.length - 1);
    const lower = Math.floor(index);
    const upper = Math.ceil(index);

    if (lower === upper) {
        return sortedArr[index];
    }
    return Math.round(sortedArr[lower] * (upper - index) + sortedArr[upper] * (index - lower));
}
document.addEventListener('DOMContentLoaded', () => {
    // Set default chart colors for dark theme
    Chart.defaults.color = '#E0E0E0';
    Chart.defaults.borderColor = '#555555';

    // DOM Elements
    const chartsContainer = document.getElementById('charts-container');
    const metricSelectButton = document.getElementById('metric-select-button');
    const metricOptionsList = document.getElementById('metric-options-list');
    const requestSelectButton = document.getElementById('request-select-button');
    const requestNameList = document.getElementById('request-name-list');
    const searchInput = document.getElementById('search-input');
    const checkboxContainer = document.getElementById('checkbox-container');

    let parsedData = [];
    let chartInstances = [];
    const metricOptions = ['Total', 'OK', 'KO', 'Min', 'p50', 'p75', 'p95', 'p99', 'Max', 'Mean', 'StdDev'];

    async function initialize() {
        populateMetricRadioButtons();
        try {
            const response = await fetch('requests-trend.csv');
            if (!response.ok) throw new Error('Network response was not ok.');
            const csvText = await response.text();
            parsedData = parseCSV(csvText);
            parsedData.sort((a, b) => new Date(a.Timestamp) - new Date(b.Timestamp));
            populateRequestNameCheckboxes();
            renderDashboard();
        } catch (error) {
            console.error('Failed to load or parse CSV file:', error);
            alert('Could not load data. Please check the console for errors.');
        }
    }

    function parseCSV(text) {
        const lines = text.trim().split('\n');
        const headers = lines[0].split(',');
        return lines.slice(1).map(line => {
            const values = line.split(',');
            const obj = {};
            headers.forEach((header, i) => {
                const value = values[i].replace(/"/g, '');
                obj[header] = isNaN(value) || value.trim() === '' ? value : parseFloat(value);
            });
            return obj;
        });
    }

    function populateMetricRadioButtons() {
        metricOptionsList.innerHTML = '';
        metricOptions.forEach(option => {
            const label = document.createElement('label');
            const radio = document.createElement('input');
            radio.type = 'radio';
            radio.name = 'metric';
            radio.value = option;
            if (option === 'Mean') radio.checked = true;
            label.appendChild(radio);
            label.appendChild(document.createTextNode(option));
            metricOptionsList.appendChild(label);
        });
    }

    function populateRequestNameCheckboxes() {
        const requestNames = [...new Set(parsedData.map(row => row.RequestName))];
        checkboxContainer.innerHTML = '';
        requestNames.forEach(name => {
            const label = document.createElement('label');
            const checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.value = name;
            label.appendChild(checkbox);
            label.appendChild(document.createTextNode(name));
            checkboxContainer.appendChild(label);
        });
    }

    function renderDashboard() {
        const selectedMetricRadio = metricOptionsList.querySelector('input[type=radio]:checked');
        const selectedMetric = selectedMetricRadio ? selectedMetricRadio.value : 'Mean';
        const selectedRequests = [...checkboxContainer.querySelectorAll('input[type=checkbox]:checked')].map(checkbox => checkbox.value);

        metricSelectButton.textContent = selectedMetric;
        requestSelectButton.textContent = selectedRequests.length > 0 ? `${selectedRequests.length} Selected` : 'None Selected';

        chartInstances.forEach(chart => chart.destroy());
        chartInstances = [];
        chartsContainer.innerHTML = '';

        selectedRequests.forEach(requestName => {
            const chartData = parsedData.filter(row => row.RequestName === requestName).map(row => ({ x: row.Timestamp, y: row[selectedMetric] }));
            const card = document.createElement('div');
            card.className = 'chart-card';
            const canvas = document.createElement('canvas');
            card.appendChild(canvas);
            chartsContainer.appendChild(card);

            const newChart = new Chart(canvas, {
                type: 'line',
                data: {
                    datasets: [{
                        label: requestName,
                        data: chartData,
                        borderColor: '#0D8AD4', // Gatling blue
                        backgroundColor: 'rgba(13, 138, 212, 0.2)',
                        fill: true,
                        tension: 0.1
                    }]
                },
                options: {
                    responsive: true,
                    plugins: {
                        title: { display: true, text: `${requestName} - ${selectedMetric}`, font: { size: 16 } },
                        legend: { display: false }
                    },
                    scales: {
                        x: { type: 'time', time: { unit: 'day', displayFormats: { day: 'MMM d' } }, title: { display: true, text: 'Date' } },
                        y: { title: { display: true, text: selectedMetric } }
                    }
                }
            });
            chartInstances.push(newChart);
        });
    }

    // --- Event Listeners ---
    metricOptionsList.addEventListener('change', () => {
        renderDashboard();
        metricOptionsList.classList.add('hidden'); // Close after selection
    });
    checkboxContainer.addEventListener('change', renderDashboard);

    metricSelectButton.addEventListener('click', (e) => {
        metricOptionsList.classList.toggle('hidden');
        requestNameList.classList.add('hidden');
        e.stopPropagation();
    });

    requestSelectButton.addEventListener('click', (e) => {
        requestNameList.classList.toggle('hidden');
        metricOptionsList.classList.add('hidden');
        e.stopPropagation();
    });

    // UPDATED: This listener now checks if the click was outside the dropdowns
    document.addEventListener('click', (e) => {
        if (!document.getElementById('metric-select-box').contains(e.target)) {
            metricOptionsList.classList.add('hidden');
        }
        if (!document.getElementById('request-select-box').contains(e.target)) {
            requestNameList.classList.add('hidden');
        }
    });

    searchInput.addEventListener('input', (e) => {
        const searchTerm = e.target.value.toLowerCase();
        const labels = checkboxContainer.querySelectorAll('label');
        labels.forEach(label => {
            const requestName = label.textContent.toLowerCase();
            label.style.display = requestName.includes(searchTerm) ? 'block' : 'none';
        });
    });

    initialize();
});
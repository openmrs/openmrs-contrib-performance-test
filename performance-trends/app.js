document.addEventListener("DOMContentLoaded", () => {
  Chart.defaults.color = "#E0E0E0";
  Chart.defaults.borderColor = "#555555";

  const chartsContainer = document.getElementById("charts-container");
  const metricOptionsList = document.getElementById("metric-options-list");
  const requestNameContainer = document.getElementById("checkbox-container");
  const searchInput = document.getElementById("search-input");

  const metricOptions = [
    "Total",
    "OK",
    "KO",
    "Min",
    "p50",
    "p75",
    "p95",
    "p99",
    "Max",
    "Mean",
    "StdDev",
  ];

  let parsedData = [];
  let chartInstances = [];

  async function initialize() {
    try {
      populateMetricRadioButtons();
      const response = await fetch("requests-trend.csv");
      if (!response.ok)
        throw new Error(
          `Failed to load CSV: ${response.status} ${response.statusText}`
        );
      const csvText = await response.text();
      parsedData = parseCSV(csvText);
      parsedData.sort((a, b) => new Date(a.Timestamp) - new Date(b.Timestamp));
      populateRequestNameCheckboxes();
      renderDashboard();
    } catch (error) {
      console.error(error);
      alert("Could not load data. Please check the console for details.");
    }
  }

  function parseCSV(text) {
    const lines = text.trim().split("\n");
    const headers = lines[0].split(",");
    return lines.slice(1).map((line) => {
      const values = line.split(",");
      const obj = {};
      headers.forEach((header, i) => {
        const val = values[i].replace(/"/g, "");
        obj[header] = isNaN(val) || val.trim() === "" ? val : parseFloat(val);
      });
      return obj;
    });
  }

  function populateMetricRadioButtons() {
    metricOptionsList.innerHTML = "";
    metricOptions.forEach((option) => {
      const label = document.createElement("label");
      const radio = document.createElement("input");
      radio.type = "radio";
      radio.name = "metric";
      radio.value = option;
      if (option === "Total") radio.checked = true;
      label.appendChild(radio);
      label.appendChild(document.createTextNode(option));
      metricOptionsList.appendChild(label);
    });
  }

  function populateRequestNameCheckboxes() {
    const uniqueRequests = [...new Set(parsedData.map((d) => d.RequestName))];
    requestNameContainer.innerHTML = "";
    uniqueRequests.forEach((name) => {
      const label = document.createElement("label");
      const checkbox = document.createElement("input");
      checkbox.type = "checkbox";
      checkbox.value = name;
      checkbox.checked = true;
      label.appendChild(checkbox);
      label.appendChild(document.createTextNode(name));
      requestNameContainer.appendChild(label);
    });
  }

  function renderDashboard() {
    const selectedMetricNode = metricOptionsList.querySelector(
      "input[type=radio]:checked"
    );
    const selectedMetric = selectedMetricNode
      ? selectedMetricNode.value
      : "Total";

    const selectedRequests = [
      ...requestNameContainer.querySelectorAll("input[type=checkbox]:checked"),
    ].map((cb) => cb.value);

    chartInstances.forEach((c) => c.destroy());
    chartInstances = [];
    chartsContainer.innerHTML = "";

    if (selectedRequests.length === 0) {
      chartsContainer.innerHTML =
        '<p style="color:var(--text-color); text-align:center;">No requests selected. Please choose requests to display charts.</p>';
      return;
    }

    selectedRequests.forEach((requestName) => {
      const dataPoints = parsedData
        .filter((d) => d.RequestName === requestName)
        .map((d) => ({
          x: d.Timestamp,
          y: d[selectedMetric],
        }));

      const card = document.createElement("div");
      card.className = "chart-card";
      const canvas = document.createElement("canvas");
      card.appendChild(canvas);
      chartsContainer.appendChild(card);

      const chart = new Chart(canvas, {
        type: "line",
        data: {
          labels: dataPoints.map(({ x }) => x),
          datasets: [
            {
              label: requestName,
              data: dataPoints,
              borderColor: getComputedStyle(document.body)
                .getPropertyValue("--chart-line-color")
                .trim(),
              backgroundColor: getComputedStyle(document.body)
                .getPropertyValue("--chart-line-bg")
                .trim(),
              fill: true,
              tension: 0.2,
            },
          ],
        },
        options: {
          animation: { duration: 2000, easing: "easeInOutQuart" },
          responsive: true,
          plugins: {
            title: {
              display: true,
              text: `${requestName} - ${selectedMetric}`,
              font: {
                family:
                  "'Montserrat', 'Helvetica Neue', Helvetica, Arial, sans-serif",
                size: 16,
                weight: "bold",
              },
            },
            legend: {
              display: false,
              labels: {
                font: {
                  family:
                    "'Montserrat', 'Helvetica Neue', Helvetica, Arial, sans-serif",
                  size: 12,
                },
              },
            },
            tooltip: {
              mode: "index",
              intersect: false,
              bodyFont: {
                family:
                  "'Montserrat', 'Helvetica Neue', Helvetica, Arial, sans-serif",
                size: 12,
              },
              titleFont: {
                family:
                  "'Montserrat', 'Helvetica Neue', Helvetica, Arial, sans-serif",
                size: 14,
                weight: "bold",
              },
            },
          },
          interaction: { mode: "nearest", axis: "x", intersect: false },
          scales: {
            x: {
              type: "time",
              time: { unit: "day", displayFormats: { day: "MMM d" } },
              title: {
                display: true,
                text: "Date",
                font: {
                  family:
                    "'Montserrat', 'Helvetica Neue', Helvetica, Arial, sans-serif",
                  size: 14,
                  weight: "bold",
                },
              },
              ticks: {
                font: {
                  family:
                    "'Montserrat', 'Helvetica Neue', Helvetica, Arial, sans-serif",
                  size: 12,
                },
                maxRotation: 0,
                autoSkip: true,
              },
            },
            y: {
              title: {
                display: true,
                text: selectedMetric,
                font: {
                  family:
                    "'Montserrat', 'Helvetica Neue', Helvetica, Arial, sans-serif",
                  size: 14,
                  weight: "bold",
                },
              },
              ticks: {
                font: {
                  family:
                    "'Montserrat', 'Helvetica Neue', Helvetica, Arial, sans-serif",
                  size: 12,
                },
              },
              min: 0,
            },
          },
        },
      });

      chartInstances.push(chart);
    });
  }

  function filterRequestNames(term) {
    const labels = requestNameContainer.querySelectorAll("label");
    labels.forEach((label) => {
      const text = label.textContent.toLowerCase();
      label.style.display = text.includes(term.toLowerCase()) ? "flex" : "none";
    });
  }

  metricOptionsList.addEventListener("change", renderDashboard);
  requestNameContainer.addEventListener("change", renderDashboard);
  searchInput.addEventListener("input", (e) =>
    filterRequestNames(e.target.value)
  );

  initialize();
});

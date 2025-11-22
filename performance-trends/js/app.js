document.addEventListener("DOMContentLoaded", () => {
	Chart.defaults.color = "#E0E0E0";
	Chart.defaults.borderColor = "#555555";

	const chartsContainer = document.getElementById("charts-container");
	const metricOptionsList = document.getElementById("metric-options-list");
	const requestNameContainer = document.getElementById("checkbox-container");
	const searchInput = document.getElementById("search-input");
	const selectAllButton = document.getElementById("select-all-button");

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

	const metricLabelsMap = {
		Total: "Total",
		OK: "OK",
		KO: "KO",
		Min: "Min",
		p50: "50th pct",
		p75: "75th pct",
		p95: "95th pct",
		p99: "99th pct",
		Max: "Max",
		Mean: "Mean",
		StdDev: "Std Dev",
	};

	const metricYAxisLabelsMap = {
		Total: "Number of requests",
		OK: "Number of successful requests",
		KO: "Number of failed requests",
		Min: "Response time (ms) - Min",
		p50: "Response time (ms) - 50th pct",
		p75: "Response time (ms) - 75th pct",
		p95: "Response time (ms) - 95th pct",
		p99: "Response time (ms) - 99th pct",
		Max: "Response time (ms) - Max",
		Mean: "Response time (ms) - Mean",
		StdDev: "Response time (ms) - Std Dev",
	};

	let parsedData = [];
	let chartInstances = [];
	let currentDateRange = "last_6_months";

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
			wireDateRangeControls();
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
		metricOptionsList.style.display = "grid";
		metricOptionsList.style.gridTemplateColumns = "repeat(2, 1fr)";
		metricOptionsList.style.gap = "8px";
		metricOptions.forEach((option) => {
			const label = document.createElement("label");
			const radio = document.createElement("input");
			radio.type = "radio";
			radio.name = "metric";
			radio.value = option;
			if (option === "Total") radio.checked = true;
			label.appendChild(radio);
			label.appendChild(
				document.createTextNode(metricLabelsMap[option] || option)
			);
			metricOptionsList.appendChild(label);
		});
	}
	function populateRequestNameCheckboxes() {
		const uniqueRequests = [...new Set(parsedData.map((d) => d.RequestName))];
		requestNameContainer.innerHTML = "";
		uniqueRequests.forEach((name, index) => {
			const label = document.createElement("label");
			const checkbox = document.createElement("input");
			checkbox.type = "checkbox";
			checkbox.value = name;
			checkbox.checked = index < 4;
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

		const yAxisLabel = metricYAxisLabelsMap[selectedMetric] || selectedMetric;

		const selectedRequests = [
			...requestNameContainer.querySelectorAll("input[type=checkbox]:checked"),
		].map((cb) => cb.value);

		const range = computeSelectedRange();

		const dataInRange = parsedData.filter((d) => {
			const t = new Date(d.Timestamp);
			if (isNaN(t)) return false;
			if (!range) return true;
			return t >= range.start && t <= range.end;
		});

		chartInstances.forEach((c) => c.destroy());
		chartInstances = [];
		chartsContainer.innerHTML = "";

		if (selectedRequests.length === 0) {
			chartsContainer.innerHTML =
				'<p style="color:var(--text-color); text-align:center;">No requests selected. Please choose requests to display charts.</p>';
			return;
		}

		selectedRequests.forEach((requestName) => {
			const dataPoints = dataInRange
				.filter((d) => d.RequestName === requestName)
				.map((d) => ({
					x: d.Timestamp,
					y: d[selectedMetric],
				}));

			const card = document.createElement("div");
			card.className = "chart-card";
			const canvas = document.createElement("canvas");
			canvas.width = 800;
			canvas.height = 300;
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
							text: `${requestName} - ${
								metricYAxisLabelsMap[selectedMetric] || selectedMetric
							}`,
							font: {
								family: "'Figtree', sans-serif",
								size: 16,
								weight: "bold",
							},
						},
						legend: {
							display: false,
							labels: {
								font: {
									family: "'Figtree', sans-serif",
									size: 12,
								},
							},
						},
						tooltip: {
							mode: "index",
							intersect: false,
							bodyFont: {
								family: "'Figtree', sans-serif",
								size: 12,
							},
							titleFont: {
								family: "'Figtree', sans-serif",
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
									family: "'Figtree', sans-serif",
									size: 14,
									weight: "bold",
								},
							},
							ticks: {
								font: {
									family: "'Figtree', sans-serif",
									size: 12,
								},
								maxRotation: 0,
								autoSkip: true,
							},
						},
						y: {
							title: {
								display: true,
								text: yAxisLabel,
								font: {
									family: "'Figtree', sans-serif",
									size: 14,
									weight: "bold",
								},
							},
							ticks: {
								font: {
									family: "'Figtree', sans-serif",
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

	function parseDateInput(value) {
		if (!value) return null;
		const d = new Date(value);
		return isNaN(d) ? null : d;
	}

	function computeSelectedRange() {
		const sel = document.getElementById("date-range-select");
		if (!sel) return null;
		const val = sel.value;
		const today = new Date();
		function endOfDay(d) {
			const e = new Date(d);
			e.setHours(23, 59, 59, 999);
			return e;
		}
		function startOfDay(d) {
			const s = new Date(d);
			s.setHours(0, 0, 0, 0);
			return s;
		}

		if (val === "all_time") {
			if (parsedData.length === 0) return null;
			const start = startOfDay(new Date(parsedData[0].Timestamp));
			const end = endOfDay(
				new Date(parsedData[parsedData.length - 1].Timestamp)
			);
			return { start, end };
		}

		if (val === "last_6_months") {
			const end = endOfDay(today);
			const start = startOfDay(
				new Date(today.getFullYear(), today.getMonth() - 6, today.getDate())
			);
			return { start, end };
		}

		if (val === "ytd") {
			const start = startOfDay(new Date(today.getFullYear(), 0, 1));
			const end = endOfDay(today);
			return { start, end };
		}

		if (val === "last_month") {
			const start = startOfDay(
				new Date(today.getFullYear(), today.getMonth() - 1, 1)
			);
			const end = endOfDay(new Date(today.getFullYear(), today.getMonth(), 0)); // last day of previous month
			return { start, end };
		}

		if (val === "last_week") {
			const end = endOfDay(today);
			const start = startOfDay(
				new Date(today.getFullYear(), today.getMonth(), today.getDate() - 7)
			);
			return { start, end };
		}

		if (val === "custom") {
			const sVal = document.getElementById("custom-start").value;
			const eVal = document.getElementById("custom-end").value;
			const s = parseDateInput(sVal);
			const e = parseDateInput(eVal);
			if (s && e) return { start: startOfDay(s), end: endOfDay(e) };
			return null;
		}

		return null;
	}

	function wireDateRangeControls() {
		const sel = document.getElementById("date-range-select");
		const customDiv = document.getElementById("custom-date-range");
		const applyBtn = document.getElementById("apply-custom-range");

		if (!sel) return;

		sel.addEventListener("change", (e) => {
			if (e.target.value === "custom") {
				customDiv.style.display = "flex";
			} else {
				customDiv.style.display = "none";
				renderDashboard();
			}
		});

		if (applyBtn) {
			applyBtn.addEventListener("click", () => {
				renderDashboard();
			});
		}
	}

	function updateSelectAllButtonText() {
		const checkboxes = requestNameContainer.querySelectorAll(
			"input[type=checkbox]"
		);
		const allChecked = Array.from(checkboxes).every((cb) => cb.checked);
		selectAllButton.textContent = allChecked ? "Reset Selection" : "Select All";
	}

	selectAllButton.addEventListener("click", () => {
		const checkboxes = requestNameContainer.querySelectorAll(
			"input[type=checkbox]"
		);
		const allChecked = Array.from(checkboxes).every((cb) => cb.checked);
		if (allChecked) {
			checkboxes.forEach((cb) => (cb.checked = false));
		} else {
			checkboxes.forEach((cb) => (cb.checked = true));
		}
		updateSelectAllButtonText();
		renderDashboard();
	});

	metricOptionsList.addEventListener("change", renderDashboard);
	requestNameContainer.addEventListener("change", renderDashboard);
	searchInput.addEventListener("input", (e) =>
		filterRequestNames(e.target.value)
	);

	initialize();
	updateSelectAllButtonText();
});

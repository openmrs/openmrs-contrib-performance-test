// charts.js - rendering charts
import { parsedData } from "./data.js";
import { computeSelectedRange } from "./utils.js";

export let chartInstances = [];

export function renderDashboard({
	chartsContainer,
	metricOptionsList,
	requestNameContainer,
	metricYAxisLabelsMap,
}) {
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
			.map((d) => ({ x: d.Timestamp, y: d[selectedMetric] }));

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
						font: { family: "'Figtree', sans-serif", size: 16, weight: "bold" },
					},
					legend: {
						display: false,
						labels: { font: { family: "'Figtree', sans-serif", size: 12 } },
					},
					tooltip: {
						mode: "index",
						intersect: false,
						bodyFont: { family: "'Figtree', sans-serif", size: 12 },
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
							font: { family: "'Figtree', sans-serif", size: 12 },
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
						ticks: { font: { family: "'Figtree', sans-serif", size: 12 } },
						min: 0,
					},
				},
			},
		});

		chartInstances.push(chart);
	});
}

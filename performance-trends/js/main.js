// main.js - orchestrates UI, data loading, and charts (refactored from app.js)
import { loadData } from "./data.js";
import {
	populateMetricRadioButtons,
	populateRequestNameCheckboxes,
	filterRequestNames,
	updateSelectAllButtonText,
	wireDateRangeControls,
} from "./ui.js";
import { renderDashboard } from "./charts.js";

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

async function initialize() {
	try {
		populateMetricRadioButtons(
			metricOptions,
			metricLabelsMap,
			metricOptionsList
		);
		await loadData();
		populateRequestNameCheckboxes(requestNameContainer);
		renderDashboard({
			chartsContainer,
			metricOptionsList,
			requestNameContainer,
			metricYAxisLabelsMap,
		});
		wireDateRangeControls(() =>
			renderDashboard({
				chartsContainer,
				metricOptionsList,
				requestNameContainer,
				metricYAxisLabelsMap,
			})
		);
	} catch (err) {
		console.error(err);
		alert("Could not load data. Please check the console for details.");
	}
}

function toggleSelectAll() {
	const checkboxes = requestNameContainer.querySelectorAll(
		"input[type=checkbox]"
	);
	const allChecked = Array.from(checkboxes).every((cb) => cb.checked);
	if (allChecked) {
		checkboxes.forEach((cb) => (cb.checked = false));
	} else {
		checkboxes.forEach((cb) => (cb.checked = true));
	}
	updateSelectAllButtonText(selectAllButton, requestNameContainer);
	renderDashboard({
		chartsContainer,
		metricOptionsList,
		requestNameContainer,
		metricYAxisLabelsMap,
	});
}

selectAllButton.addEventListener("click", toggleSelectAll);

metricOptionsList.addEventListener("change", () =>
	renderDashboard({
		chartsContainer,
		metricOptionsList,
		requestNameContainer,
		metricYAxisLabelsMap,
	})
);
requestNameContainer.addEventListener("change", () =>
	renderDashboard({
		chartsContainer,
		metricOptionsList,
		requestNameContainer,
		metricYAxisLabelsMap,
	})
);
searchInput.addEventListener("input", (e) =>
	filterRequestNames(e.target.value, requestNameContainer)
);

initialize();
updateSelectAllButtonText(selectAllButton, requestNameContainer);

// ui.js - UI population and controls
import { parsedData } from "./data.js";

export function populateMetricRadioButtons(
	metricOptions,
	metricLabelsMap,
	metricOptionsList
) {
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

export function populateRequestNameCheckboxes(requestNameContainer) {
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

export function filterRequestNames(term, requestNameContainer) {
	const labels = requestNameContainer.querySelectorAll("label");
	labels.forEach((label) => {
		const text = label.textContent.toLowerCase();
		label.style.display = text.includes(term.toLowerCase()) ? "flex" : "none";
	});
}

export function updateSelectAllButtonText(
	selectAllButton,
	requestNameContainer
) {
	const checkboxes = requestNameContainer.querySelectorAll(
		"input[type=checkbox]"
	);
	const allChecked = Array.from(checkboxes).every((cb) => cb.checked);
	selectAllButton.textContent = allChecked ? "Reset Selection" : "Select All";
}

export function wireDateRangeControls(renderDashboard) {
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

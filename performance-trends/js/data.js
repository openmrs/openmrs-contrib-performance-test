// data.js - responsible for loading and parsing CSV data
export let parsedData = [];

export function parseCSV(text) {
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

export async function loadData() {
	const response = await fetch("requests-trend.csv");
	if (!response.ok)
		throw new Error(
			`Failed to load CSV: ${response.status} ${response.statusText}`
		);
	const csvText = await response.text();
	parsedData = parseCSV(csvText);
	parsedData.sort((a, b) => new Date(a.Timestamp) - new Date(b.Timestamp));
	return parsedData;
}

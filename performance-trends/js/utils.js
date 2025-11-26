// utils.js - date helpers
import { parsedData } from "./data.js";

export function parseDateInput(value) {
	if (!value) return null;
	const d = new Date(value);
	return isNaN(d) ? null : d;
}

export function computeSelectedRange() {
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
		const end = endOfDay(new Date(parsedData[parsedData.length - 1].Timestamp));
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
		const end = endOfDay(new Date(today.getFullYear(), today.getMonth(), 0));
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

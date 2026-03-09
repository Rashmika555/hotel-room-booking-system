const API_BASE = "/api/bookings";

const dom = {
	bookingTableBody: document.getElementById("bookingTableBody"),
	emptyState: document.getElementById("emptyState"),
	searchInput: document.getElementById("searchInput"),
	statusFilter: document.getElementById("statusFilter"),
	refreshBtn: document.getElementById("refreshBtn"),
	addBookingForm: document.getElementById("addBookingForm"),
	editBookingForm: document.getElementById("editBookingForm"),
	loadingOverlay: document.getElementById("loadingOverlay"),
	appToast: document.getElementById("appToast"),
	toastMessage: document.getElementById("toastMessage"),
	statTotal: document.getElementById("statTotal"),
	statActive: document.getElementById("statActive"),
	statCheckedOut: document.getElementById("statCheckedOut"),
	statCancelled: document.getElementById("statCancelled"),
};

let addModal;
let editModal;
let searchDebounce;

window.addEventListener("DOMContentLoaded", () => {
	addModal = new bootstrap.Modal(document.getElementById("addBookingModal"));
	editModal = new bootstrap.Modal(
		document.getElementById("editBookingModal"),
	);

	bindEvents();
	loadDashboardData();
});

function bindEvents() {
	dom.searchInput.addEventListener("input", () => {
		clearTimeout(searchDebounce);
		searchDebounce = setTimeout(() => {
			loadDashboardData();
		}, 250);
	});

	dom.statusFilter.addEventListener("change", loadDashboardData);
	dom.refreshBtn.addEventListener("click", loadDashboardData);

	dom.addBookingForm.addEventListener("submit", async (event) => {
		event.preventDefault();
		await submitAddForm();
	});

	dom.editBookingForm.addEventListener("submit", async (event) => {
		event.preventDefault();
		await submitEditForm();
	});

	dom.bookingTableBody.addEventListener("click", async (event) => {
		const editButton = event.target.closest(".btn-edit");
		const deleteButton = event.target.closest(".btn-delete");

		if (editButton) {
			const bookingId = Number(editButton.dataset.id);
			await openEditModal(bookingId);
			return;
		}

		if (deleteButton) {
			const bookingId = Number(deleteButton.dataset.id);
			await deleteBooking(bookingId);
		}
	});
}

async function loadDashboardData() {
	toggleLoading(true);

	try {
		const searchTerm = dom.searchInput.value.trim();
		const selectedStatus = dom.statusFilter.value;

		const [listData, allData] = await resolveDataSets(
			searchTerm,
			selectedStatus,
		);
		renderTable(listData);
		renderStats(allData);
	} catch (error) {
		showToast(error.message || "Unable to load bookings.", "error");
	} finally {
		toggleLoading(false);
	}
}

async function resolveDataSets(searchTerm, selectedStatus) {
	if (!searchTerm && selectedStatus === "All") {
		const data = await apiRequest(API_BASE);
		return [data, data];
	}

	if (searchTerm && selectedStatus === "All") {
		const [listData, allData] = await Promise.all([
			apiRequest(
				`${API_BASE}/search?customerName=${encodeURIComponent(searchTerm)}`,
			),
			apiRequest(API_BASE),
		]);
		return [listData, allData];
	}

	if (!searchTerm) {
		const [listData, allData] = await Promise.all([
			apiRequest(
				`${API_BASE}/status?status=${encodeURIComponent(selectedStatus)}`,
			),
			apiRequest(API_BASE),
		]);
		return [listData, allData];
	}

	const [searchData, allData] = await Promise.all([
		apiRequest(
			`${API_BASE}/search?customerName=${encodeURIComponent(searchTerm)}`,
		),
		apiRequest(API_BASE),
	]);

	const filtered = searchData.filter(
		(booking) => normalizeStatus(booking.bookingStatus) === selectedStatus,
	);
	return [filtered, allData];
}

function renderTable(bookings) {
	dom.bookingTableBody.innerHTML = "";

	if (!bookings || bookings.length === 0) {
		dom.emptyState.classList.remove("d-none");
		return;
	}

	dom.emptyState.classList.add("d-none");

	bookings.forEach((booking, index) => {
		const status = normalizeStatus(booking.bookingStatus);
		const rowClass =
			status === "Checked-Out"
				? "row-checked-out"
				: status === "Cancelled"
					? "row-cancelled"
					: "";

		const statusClass = status.toLowerCase().replace(/[^a-z]/g, "-");
		const row = document.createElement("tr");
		row.className = rowClass;
		row.style.animation = `riseIn 0.35s ease ${Math.min(index * 0.04, 0.32)}s both`;

		row.innerHTML = `
            <td>${booking.bookingId}</td>
            <td>${escapeHtml(booking.customerName || "-")}</td>
            <td>${booking.roomNumber ?? "-"}</td>
            <td>${escapeHtml(booking.roomType || "-")}</td>
            <td>${formatDate(booking.checkInDate)}</td>
            <td>${formatDate(booking.checkOutDate)}</td>
            <td>${formatAmount(booking.bookingAmount)}</td>
            <td><span class="status-pill status-${statusClass}">${status}</span></td>
            <td>${formatDateTime(booking.createdDateTime)}</td>
            <td>
                <div class="d-flex gap-2">
                    <button class="btn btn-sm btn-outline-brand btn-edit" data-id="${booking.bookingId}">Edit</button>
                    <button class="btn btn-sm btn-danger btn-delete" data-id="${booking.bookingId}">Delete</button>
                </div>
            </td>
        `;

		dom.bookingTableBody.appendChild(row);
	});
}

function renderStats(bookings) {
	const total = bookings.length;
	let checkedOut = 0;
	let cancelled = 0;

	bookings.forEach((booking) => {
		const status = normalizeStatus(booking.bookingStatus);
		if (status === "Checked-Out") {
			checkedOut += 1;
		} else if (status === "Cancelled") {
			cancelled += 1;
		}
	});

	const active = total - checkedOut - cancelled;

	dom.statTotal.textContent = total;
	dom.statActive.textContent = active;
	dom.statCheckedOut.textContent = checkedOut;
	dom.statCancelled.textContent = cancelled;
}

async function submitAddForm() {
	try {
		const payload = collectPayload(dom.addBookingForm, false);

		toggleLoading(true);
		await apiRequest(API_BASE, {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(payload),
		});

		dom.addBookingForm.reset();
		addModal.hide();
		showToast("Booking added successfully.", "success");
		await loadDashboardData();
	} catch (error) {
		showToast(error.message || "Failed to add booking.", "error");
	} finally {
		toggleLoading(false);
	}
}

async function openEditModal(bookingId) {
	try {
		toggleLoading(true);
		const booking = await apiRequest(`${API_BASE}/${bookingId}`);

		dom.editBookingForm.elements.bookingId.value = booking.bookingId;
		dom.editBookingForm.elements.customerName.value =
			booking.customerName || "";
		dom.editBookingForm.elements.roomNumber.value =
			booking.roomNumber || "";
		dom.editBookingForm.elements.roomType.value = normalizeRoomType(
			booking.roomType,
		);
		dom.editBookingForm.elements.bookingAmount.value =
			booking.bookingAmount ?? 0;
		dom.editBookingForm.elements.checkInDate.value =
			booking.checkInDate || "";
		dom.editBookingForm.elements.checkOutDate.value =
			booking.checkOutDate || "";
		dom.editBookingForm.elements.bookingStatus.value = normalizeStatus(
			booking.bookingStatus,
		);

		editModal.show();
	} catch (error) {
		showToast(error.message || "Failed to load booking.", "error");
	} finally {
		toggleLoading(false);
	}
}

async function submitEditForm() {
	const bookingId = Number(dom.editBookingForm.elements.bookingId.value);

	try {
		const payload = collectPayload(dom.editBookingForm, true);

		toggleLoading(true);
		await apiRequest(`${API_BASE}/${bookingId}`, {
			method: "PUT",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(payload),
		});

		editModal.hide();
		showToast("Booking updated successfully.", "success");
		await loadDashboardData();
	} catch (error) {
		showToast(error.message || "Failed to update booking.", "error");
	} finally {
		toggleLoading(false);
	}
}

async function deleteBooking(bookingId) {
	const accepted = window.confirm("Delete this booking permanently?");
	if (!accepted) {
		return;
	}

	try {
		toggleLoading(true);
		await apiRequest(`${API_BASE}/${bookingId}`, { method: "DELETE" });
		showToast("Booking deleted successfully.", "success");
		await loadDashboardData();
	} catch (error) {
		showToast(error.message || "Failed to delete booking.", "error");
	} finally {
		toggleLoading(false);
	}
}

function collectPayload(form, includeStatus) {
	const customerName = form.elements.customerName.value.trim();
	const roomNumber = Number(form.elements.roomNumber.value);
	const roomType = form.elements.roomType.value;
	const checkInDate = form.elements.checkInDate.value;
	const checkOutDate = form.elements.checkOutDate.value;
	const bookingAmount = Number(form.elements.bookingAmount.value);

	if (!customerName) {
		throw new Error("Customer name is mandatory.");
	}
	if (!roomNumber || roomNumber <= 0) {
		throw new Error("Room number must be greater than 0.");
	}
	if (!checkInDate) {
		throw new Error("Check-in date is mandatory.");
	}
	if (checkOutDate && checkOutDate <= checkInDate) {
		throw new Error("Check-out date must be later than check-in date.");
	}
	if (Number.isNaN(bookingAmount) || bookingAmount < 0) {
		throw new Error("Booking amount must be zero or positive.");
	}

	const payload = {
		customerName,
		roomNumber,
		roomType,
		checkInDate,
		checkOutDate: checkOutDate || null,
		bookingAmount,
	};

	if (includeStatus) {
		payload.bookingStatus = form.elements.bookingStatus.value;
	}

	return payload;
}

async function apiRequest(url, options = {}) {
	const response = await fetch(url, options);

	if (response.status === 204) {
		return null;
	}

	const responseBody = await response.text();
	let data = null;

	if (responseBody) {
		try {
			data = JSON.parse(responseBody);
		} catch (error) {
			data = { message: responseBody };
		}
	}

	if (!response.ok) {
		throw new Error(data?.message || "Server request failed.");
	}

	return data;
}

function normalizeStatus(status) {
	if (!status) {
		return "Booked";
	}

	const normalized = status.trim().toLowerCase();
	if (normalized === "booked") {
		return "Booked";
	}
	if (["checked-in", "checked in", "checked_in"].includes(normalized)) {
		return "Checked-In";
	}
	if (["checked-out", "checked out", "checked_out"].includes(normalized)) {
		return "Checked-Out";
	}
	if (["cancelled", "canceled"].includes(normalized)) {
		return "Cancelled";
	}

	return status;
}

function normalizeRoomType(roomType) {
	if (!roomType) {
		return "Single";
	}

	const normalized = roomType.trim().toLowerCase();
	if (normalized === "single") {
		return "Single";
	}
	if (normalized === "double") {
		return "Double";
	}
	if (normalized === "suite") {
		return "Suite";
	}

	return "Single";
}

function formatDate(value) {
	if (!value) {
		return "-";
	}

	const datePart = String(value).includes("T")
		? String(value).split("T")[0]
		: String(value);
	const parts = datePart.split("-").map(Number);
	if (parts.length !== 3 || parts.some(Number.isNaN)) {
		return value;
	}

	const [year, month, day] = parts;
	const date = new Date(year, month - 1, day);

	return new Intl.DateTimeFormat("en-GB", {
		day: "2-digit",
		month: "short",
		year: "numeric",
	}).format(date);
}

function formatDateTime(value) {
	if (!value) {
		return "-";
	}

	const normalized = String(value).replace(" ", "T");
	const date = new Date(normalized);
	if (Number.isNaN(date.getTime())) {
		return value;
	}

	return new Intl.DateTimeFormat("en-GB", {
		day: "2-digit",
		month: "short",
		year: "numeric",
		hour: "2-digit",
		minute: "2-digit",
	}).format(date);
}

function formatAmount(value) {
	if (value === null || value === undefined || Number.isNaN(value)) {
		return "-";
	}
	return new Intl.NumberFormat("en-US", {
		style: "currency",
		currency: "USD",
	}).format(Number(value));
}

function escapeHtml(text) {
	return text
		.replaceAll("&", "&amp;")
		.replaceAll("<", "&lt;")
		.replaceAll(">", "&gt;")
		.replaceAll('"', "&quot;")
		.replaceAll("'", "&#039;");
}

function toggleLoading(isVisible) {
	dom.loadingOverlay.classList.toggle("d-none", !isVisible);
}

function showToast(message, type = "success") {
	dom.toastMessage.textContent = message;
	dom.appToast.classList.remove("toast-success", "toast-error");
	dom.appToast.classList.add(
		type === "success" ? "toast-success" : "toast-error",
	);
	bootstrap.Toast.getOrCreateInstance(dom.appToast, { delay: 2600 }).show();
}

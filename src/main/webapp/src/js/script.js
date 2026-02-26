const API_BASE = 'http://localhost:8080/api';

let currentUser = null;
let allEmployees = [];
let departments = [];
let positions = [];
let pendingDeleteId = null;
let pendingDeleteName = null;


function getToken() {
    return sessionStorage.getItem('token');
}

function setToken(t) {
    sessionStorage.setItem('token', t);
}

function clearToken() {
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('user');
}

function authHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${getToken()}`
    };
}

async function apiFetch(path, options = {}) {
    const res = await fetch(API_BASE + path, options);
    const json = await res.json().catch(() => ({}));
    return {ok: res.ok, status: res.status, data: json};
}

function fmt(val, type = 'text') {
    if (val === null || val === undefined) return '—';
    if (type === 'currency') return 'LKR ' + Number(val).toLocaleString('en-LK', {minimumFractionDigits: 2});
    if (type === 'date') return new Date(val).toLocaleDateString('en-GB', {
        year: 'numeric',
        month: 'short',
        day: '2-digit'
    });
    return val;
}

function showAlert(containerId, message, type = 'error') {
    const el = document.getElementById(containerId);
    if (!el) return;
    el.className = `alert alert-${type}`;
    el.textContent = message;
    el.classList.remove('hidden');
    if (type !== 'error') setTimeout(() => el.classList.add('hidden'), 4000);
}

function hideAlert(containerId) {
    const el = document.getElementById(containerId);
    if (el) el.classList.add('hidden');
}

//authentication
async function handleLogin() {
    const username = document.getElementById('loginUsername').value.trim();
    const password = document.getElementById('loginPassword').value;

    if (!username || !password) {
        showAlert('loginError', 'Please enter username and password.');
        return;
    }

    const btn = document.getElementById('loginBtn');
    btn.disabled = true;
    document.getElementById('loginBtnText').textContent = 'Signing in...';

    const {ok, data} = await apiFetch('/auth/login', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({username, password})
    });

    btn.disabled = false;
    document.getElementById('loginBtnText').textContent = 'Sign In';

    if (ok && data.success) {
        setToken(data.data.token);
        currentUser = {username: data.data.username, role: data.data.role};
        sessionStorage.setItem('user', JSON.stringify(currentUser));
        initApp();
    } else {
        showAlert('loginError', data.message || 'Login failed.');
    }
}

function handleLogout() {
    clearToken();
    currentUser = null;
    document.getElementById('loginScreen').classList.remove('hidden');
    document.getElementById('appScreen').classList.add('hidden');
    document.getElementById('loginUsername').value = '';
    document.getElementById('loginPassword').value = '';
}

function initApp() {
    document.getElementById('loginScreen').classList.add('hidden');
    document.getElementById('appScreen').classList.remove('hidden');

    document.getElementById('sidebarUsername').textContent = currentUser.username;
    document.getElementById('sidebarRole').textContent = currentUser.role;
    document.getElementById('userAvatar').textContent = currentUser.username[0].toUpperCase();

    if (currentUser.role === 'VIEWER') {
        document.querySelectorAll('.btn[onclick="openEmployeeModal()"]')
            .forEach(b => b.style.display = 'none');
    }

    loadLookups();
    navigateTo('dashboard');
}

//navigation
function navigateTo(page) {
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
    document.getElementById('page-' + page).classList.add('active');
    document.querySelector(`[data-page="${page}"]`).classList.add('active');

    if (page === 'dashboard') loadDashboard();
    if (page === 'employees') loadEmployees();
    if (page === 'past') loadPastEmployees();
}

async function loadLookups() {
    const [dRes, pRes] = await Promise.all([
        apiFetch('/lookups/departments', {headers: authHeaders()}),
        apiFetch('/lookups/positions', {headers: authHeaders()})
    ]);

    if (dRes.ok) departments = dRes.data.data || [];
    if (pRes.ok) positions = pRes.data.data || [];

    populateSearchFilters();
}

function populateSearchFilters() {
    const deptSel = document.getElementById('searchDept');
    const posSel = document.getElementById('searchPos');

    deptSel.innerHTML = '<option value="">All Departments</option>';
    departments.forEach(d => {
        const o = document.createElement('option');
        o.value = d.departmentName;
        o.textContent = d.departmentName;
        deptSel.appendChild(o);
    });

    posSel.innerHTML = '<option value="">All Positions</option>';
    positions.forEach(p => {
        const o = document.createElement('option');
        o.value = p.title;
        o.textContent = p.title;
        posSel.appendChild(o);
    });
}

function populateModalDropdowns() {
    const deptSel = document.getElementById('empDepartment');
    const posSel = document.getElementById('empPosition');

    deptSel.innerHTML = '<option value="">Select Department</option>';
    departments.forEach(d => {
        const o = document.createElement('option');
        o.value = d.departmentId;
        o.textContent = d.departmentName;
        deptSel.appendChild(o);
    });

    posSel.innerHTML = '<option value="">Select Position</option>';
    positions.forEach(p => {
        const o = document.createElement('option');
        o.value = p.positionId;
        o.textContent = p.title;
        posSel.appendChild(o);
    });
}

//dashboard
async function loadDashboard() {
    const [empRes, pastRes] = await Promise.all([
        apiFetch('/employees', {headers: authHeaders()}),
        apiFetch('/employees/past', {headers: authHeaders()})
    ]);

    const emps = empRes.ok ? (empRes.data.data || []) : [];
    const past = pastRes.ok ? (pastRes.data.data || []) : [];

    const deptSet = new Set(emps.map(e => e.department?.departmentId));
    document.getElementById('statTotal').textContent = emps.length;
    document.getElementById('statDepts').textContent = deptSet.size;
    document.getElementById('statPast').textContent = past.length;

    const recent = [...emps].slice(-5).reverse();
    const recentList = document.getElementById('recentList');
    if (recent.length === 0) {
        recentList.innerHTML = `<div class="empty-state"><p>No employees yet.</p></div>`;
        return;
    }
    recentList.innerHTML = `
        <table>
            <thead><tr>
                <th>Name</th><th>Department</th><th>Position</th><th>Hire Date</th>
            </tr></thead>
            <tbody>
                ${recent.map(e => `
                    <tr>
                        <td><strong>${e.firstName} ${e.lastName}</strong></td>
                        <td><span class="badge badge-blue">${e.department?.departmentName || '—'}</span></td>
                        <td>${e.position?.title || '—'}</td>
                        <td>${fmt(e.hireDate, 'date')}</td>
                    </tr>`).join('')}
            </tbody>
        </table>`;
}

//employees
async function loadEmployees() {
    const {ok, data} = await apiFetch('/employees', {headers: authHeaders()});
    if (!ok) {
        showAlert('employeeAlert', 'Failed to load employees.');
        return;
    }
    allEmployees = data.data || [];
    renderEmployeeTable(allEmployees);
}

function renderEmployeeTable(employees) {
    const container = document.getElementById('employeeList');
    if (employees.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <svg viewBox="0 0 24 24"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
                <h3>No employees found</h3>
                <p>Try a different search or add a new employee.</p>
            </div>`;
        return;
    }

    const canEdit = currentUser?.role !== 'VIEWER';
    const canDelete = currentUser?.role === 'ADMIN';

    container.innerHTML = `
        <table>
            <thead><tr>
                <th>#No</th>
                <th>Name</th>
                <th>Email</th>
                <th>Department</th>
                <th>Position</th>
                <th>Hire Date</th>
                <th>Salary</th>
                <th>Actions</th>
            </tr></thead>
            <tbody>
                ${employees.map((e, i) => `
                    <tr>
                        <td>${e.employeeId}</td>
                        <td><strong>${e.firstName} ${e.lastName}</strong></td>
                        <td>${e.email}</td>
                        <td><span class="badge badge-blue">${e.department?.departmentName || '—'}</span></td>
                        <td>${e.position?.title || '—'}</td>
                        <td>${fmt(e.hireDate, 'date')}</td>
                        <td>${fmt(e.salary, 'currency')}</td>
                        <td>
                            <div class="actions">
                                <button class="btn-icon" onclick="viewEmployee(${e.employeeId})" title="View">
                                    <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                                    <circle cx="12" cy="12" r="3"/>
                                    </svg>
                                </button>
                                ${canEdit ? `
                                <button class="btn-icon" onclick="editEmployee(${e.employeeId})" title="Edit">
                                    <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                                    </svg>
                                </button>` : ''}
                                ${canDelete ? `
                                <button class="btn-icon danger" onclick="openDeleteModal(${e.employeeId}, '${e.firstName} ${e.lastName}')" title="Archive">
                                    <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <polyline points="3 6 5 6 21 6"/>
                                    <path d="M19 6l-1 14H6L5 6"/>
                                    <path d="M10 11v6"/>
                                    <path d="M14 11v6"/>
                                    <path d="M9 6V4h6v2"/>
                                    </svg>
                                </button>` : ''}
                            </div>
                        </td>
                    </tr>`).join('')}
            </tbody>
        </table>`;
}

//search
let searchTimeout;

function handleSearch() {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(async () => {
        const name = document.getElementById('searchName').value.trim();
        const dept = document.getElementById('searchDept').value;
        const pos = document.getElementById('searchPos').value;
        const date = document.getElementById('searchDate').value;

        const params = new URLSearchParams();
        if (name) params.append('name', name);
        if (dept) params.append('department', dept);
        if (pos) params.append('position', pos);
        if (date) params.append('hireDate', date);

        const qs = params.toString();
        const {ok, data} = await apiFetch(`/employees/search${qs ? '?' + qs : ''}`,
            {headers: authHeaders()});
        if (ok) renderEmployeeTable(data.data || []);
    }, 300);
}

function clearSearch() {
    document.getElementById('searchName').value = '';
    document.getElementById('searchDept').value = '';
    document.getElementById('searchPos').value = '';
    document.getElementById('searchDate').value = '';
    renderEmployeeTable(allEmployees);
}

//view employee
async function viewEmployee(id) {
    const {ok, data} = await apiFetch(`/employees/${id}`, {headers: authHeaders()});
    if (!ok) return;
    const e = data.data;
    document.getElementById('viewModalBody').innerHTML = `
        <div class="detail-grid">
            <div class="detail-item"><label>First Name</label><span>${e.firstName}</span></div>
            <div class="detail-item"><label>Last Name</label><span>${e.lastName}</span></div>
            <div class="detail-item"><label>Email</label><span>${e.email}</span></div>
            <div class="detail-item"><label>Phone</label><span>${e.phone || '—'}</span></div>
            <div class="detail-item"><label>Department</label><span>${e.department?.departmentName || '—'}</span></div>
            <div class="detail-item"><label>Position</label><span>${e.position?.title || '—'} (${e.position?.payGrade || '—'})</span></div>
            <div class="detail-item"><label>Hire Date</label><span>${fmt(e.hireDate, 'date')}</span></div>
            <div class="detail-item"><label>Salary</label><span>${fmt(e.salary, 'currency')}</span></div>
            <div class="detail-item"><label>Status</label><span><span class="badge badge-green">${e.status}</span></span></div>
            <div class="detail-item"><label>Employee ID</label><span>${e.employeeId}</span></div>
        </div>`;
    document.getElementById('viewModal').classList.remove('hidden');
}

//employee model
function openEmployeeModal() {
    document.getElementById('modalTitle').textContent = 'Add Employee';
    document.getElementById('saveBtn').textContent = 'Save Employee';
    document.getElementById('empId').value = '';
    document.getElementById('employeeForm').reset();
    hideAlert('modalAlert');
    populateModalDropdowns();
    document.getElementById('employeeModal').classList.remove('hidden');
}

async function editEmployee(id) {
    const {ok, data} = await apiFetch(`/employees/${id}`, {headers: authHeaders()});
    if (!ok) return;
    const e = data.data;

    populateModalDropdowns();
    document.getElementById('modalTitle').textContent = 'Edit Employee';
    document.getElementById('saveBtn').textContent = 'Update Employee';
    document.getElementById('empId').value = e.employeeId;
    document.getElementById('empFirstName').value = e.firstName;
    document.getElementById('empLastName').value = e.lastName;
    document.getElementById('empEmail').value = e.email;
    document.getElementById('empPhone').value = e.phone || '';
    document.getElementById('empHireDate').value = e.hireDate;
    document.getElementById('empSalary').value = e.salary;
    hideAlert('modalAlert');

    setTimeout(() => {
        document.getElementById('empDepartment').value = e.department?.departmentId || '';
        document.getElementById('empPosition').value = e.position?.positionId || '';
    }, 50);

    document.getElementById('employeeModal').classList.remove('hidden');
}

function closeEmployeeModal() {
    document.getElementById('employeeModal').classList.add('hidden');
}

async function saveEmployee() {
    const id = document.getElementById('empId').value;
    const payload = {
        firstName: document.getElementById('empFirstName').value.trim(),
        lastName: document.getElementById('empLastName').value.trim(),
        email: document.getElementById('empEmail').value.trim(),
        phone: document.getElementById('empPhone').value.trim() || null,
        department: {departmentId: parseInt(document.getElementById('empDepartment').value)},
        position: {positionId: parseInt(document.getElementById('empPosition').value)},
        hireDate: document.getElementById('empHireDate').value,
        salary: parseFloat(document.getElementById('empSalary').value)
    };

    if (!payload.firstName || !payload.lastName || !payload.email ||
        !payload.hireDate || isNaN(payload.salary) ||
        !payload.department.departmentId || !payload.position.positionId) {
        showAlert('modalAlert', 'Please fill in all required fields.');
        return;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(payload.email)) {
        showAlert('modalAlert', 'Please enter a valid email address.');
        return;
    }

    const btn = document.getElementById('saveBtn');
    btn.disabled = true;

    const method = id ? 'PUT' : 'POST';
    const path = id ? `/employees/${id}` : '/employees';
    const {ok, data} = await apiFetch(path, {
        method,
        headers: authHeaders(),
        body: JSON.stringify(payload)
    });

    btn.disabled = false;

    if (ok && data.success) {
        closeEmployeeModal();
        showAlert('employeeAlert', data.message, 'success');
        loadEmployees();
    } else {
        showAlert('modalAlert', data.message || 'An error occurred.');
    }
}

//partial delete
function openDeleteModal(id, name) {
    pendingDeleteId = id;
    pendingDeleteName = name;
    document.getElementById('deleteEmpName').textContent = name;
    document.getElementById('deleteReason').value = '';
    document.getElementById('deleteModal').classList.remove('hidden');
}

function closeDeleteModal() {
    document.getElementById('deleteModal').classList.add('hidden');
    pendingDeleteId = null;
}

async function confirmDelete() {
    if (!pendingDeleteId) return;
    const reason = encodeURIComponent(document.getElementById('deleteReason').value.trim());
    const path = `/employees/${pendingDeleteId}?reason=${reason}`;

    const {ok, data} = await apiFetch(path, {
        method: 'DELETE',
        headers: authHeaders()
    });

    closeDeleteModal();

    if (ok && data.success) {
        showAlert('employeeAlert', `${pendingDeleteName || 'Employee'} has been archived to Past Employees.`, 'success');
        loadEmployees();
    } else {
        showAlert('employeeAlert', data.message || 'Failed to archive employee.');
    }
}

//past employees
async function loadPastEmployees() {
    const {ok, data} = await apiFetch('/employees/past', {headers: authHeaders()});
    const container = document.getElementById('pastList');

    if (!ok) {
        container.innerHTML = `<div class="empty-state"><p>Failed to load past employees.</p></div>`;
        return;
    }

    const past = data.data || [];

    if (past.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <svg viewBox="0 0 24 24"><path d="M20 9V7a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v2"/>
                <path d="M20 9v11a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V9"/></svg>
                <h3>No past employees</h3>
                <p>Archived employees will appear here.</p>
            </div>`;
        return;
    }

    container.innerHTML = `
        <table>
            <thead><tr>
                <th>#NO</th>
                <th>Name</th>
                <th>Email</th>
                <th>Department</th>
                <th>Position</th>
                <th>Hire Date</th>
                <th>Termination Date</th>
                <th>Reason</th>
                <th>Final Salary</th>
            </tr></thead>
            <tbody>
                ${past.map(e => `
                    <tr>
                        <td>${e.pastEmployeeId}</td>
                        <td><strong>${e.firstName} ${e.lastName}</strong><br>
                        <small class="text-muted">Orig. ID: ${e.originalEmployeeId}</small>
                        </td>
                        <td>${e.email}</td>
                        <td><span class="badge badge-gray">${e.department?.departmentName || '—'}</span></td>
                        <td>${e.position?.title || '—'}</td>
                        <td>${fmt(e.hireDate, 'date')}</td>
                        <td><span class="badge badge-red">${fmt(e.terminationDate, 'date')}</span></td>
                        <td>${e.terminationReason || '—'}</td>
                        <td>${fmt(e.salary, 'currency')}</td>
                    </tr>`).join('')}
            </tbody>
        </table>`;
}

function closeModalOnOverlay(event) {
    if (event.target === event.currentTarget) {
        event.currentTarget.classList.add('hidden');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('loginPassword').addEventListener('keydown', e => {
        if (e.key === 'Enter') handleLogin();
    });
    document.getElementById('loginUsername').addEventListener('keydown', e => {
        if (e.key === 'Enter') document.getElementById('loginPassword').focus();
    });

    const saved = sessionStorage.getItem('user');
    const token = getToken();
    if (saved && token) {
        currentUser = JSON.parse(saved);
        initApp();
    }
});

const $ = (s, root = document) => root.querySelector(s);
const $$ = (s, root = document) => [...root.querySelectorAll(s)];
let currentUser = { username: 'User', role: 'EMPLOYEE' };

const esc = v => String(v ?? '').replace(/[&<>'"]/g, c => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;'
}[c]));

const initials = e => `${(e.firstName || '').charAt(0)}${(e.lastName || '').charAt(0)}`.toUpperCase() || '?';

const money = n => n == null || n === '' ? '—' : new Intl.NumberFormat('en-IN', {
    style: 'currency', currency: 'INR', maximumFractionDigits: 0
}).format(Number(n));

const date = d => d ? new Date(d + 'T00:00:00').toLocaleDateString('en-IN', {
    day: '2-digit', month: 'short', year: 'numeric'
}) : '—';

function toast(msg) {
    const t = document.createElement('div');
    t.className = 'toast';
    t.textContent = msg;
    document.body.appendChild(t);
    setTimeout(() => t.remove(), 2600);
}

function csrfToken() {
    const match = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]*)/);
    return match ? decodeURIComponent(match[1]) : '';
}

async function api(url, opts = {}) {
    const headers = { Accept: 'application/json', ...(opts.headers || {}) };
    if (opts.body && !headers['Content-Type']) headers['Content-Type'] = 'application/json';
    const token = csrfToken();
    if (token) headers['X-XSRF-TOKEN'] = token;
    const r = await fetch(url, { credentials: 'same-origin', ...opts, headers });
    if (!r.ok) {
        let msg = `Request failed (${r.status})`;
        try {
            const x = await r.json();
            msg = x.detail || x.message || msg;
        } catch (_) {
            try { msg = (await r.text()) || msg; } catch (_) {}
        }
        throw new Error(msg);
    }
    if (r.status === 204) return null;
    return r.json();
}

async function setupUser() {
    try {
        currentUser = await api('/api/me');
        $$('.mini-user').forEach(u => {
            const b = u.querySelector('b');
            const sm = u.querySelector('small');
            if (b) b.textContent = currentUser.username;
            if (sm) sm.textContent = currentUser.role;
        });
        $$('.welcome-user').forEach(el => el.textContent = currentUser.username);
        if (currentUser.role === 'EMPLOYEE') {
            $$('.role-write, .role-admin').forEach(el => el.remove());
            $$('a[href^="/employee-form"]').forEach(el => el.remove());
        }
        $$('.sidebar-footer').forEach(footer => {
            if (!footer.querySelector('.logout-form')) {
                footer.insertAdjacentHTML('beforeend',
                    `<form class="logout-form" method="post" action="/logout"><input type="hidden" name="_csrf" value="${esc(csrfToken())}"><button class="nav-item logout-btn" type="submit">↪ <span>Sign out</span></button></form>`);
            }
        });
        const add = $('#addDepartmentBtn');
        if (add && currentUser.role === 'EMPLOYEE') add.remove();
    } catch (e) {
        console.error('User setup failed', e);
    }
}

function setupTheme() {
    const b = $('#themeToggle');
    const saved = localStorage.getItem('hrms-theme');
    if (saved === 'dark') document.body.classList.add('dark');
    const sync = () => { if (b) b.textContent = document.body.classList.contains('dark') ? '☀' : '☾'; };
    sync();
    if (b) b.onclick = () => {
        document.body.classList.toggle('dark');
        localStorage.setItem('hrms-theme', document.body.classList.contains('dark') ? 'dark' : 'light');
        sync();
    };
}

async function getEmployees() { return api('/api/employees'); }
async function getDepartments() { return api('/api/departments'); }

function photo(e, cls = 'person-avatar') {
    return e.photoUrl
        ? `<img class="${cls}" src="${esc(e.photoUrl)}" alt="${esc(e.firstName || 'Employee')}" loading="lazy" onerror="this.replaceWith(Object.assign(document.createElement('div'),{className:'${cls}',textContent:'${esc(initials(e))}'}))">`
        : `<div class="${cls}">${esc(initials(e))}</div>`;
}

function employeeRow(e) {
    let actions = `<a class="icon-link" title="View" href="/employee-profile?id=${e.id}">↗</a>`;
    if (currentUser.role === 'ADMIN') {
        actions += `<a class="icon-link" title="Edit" href="/employee-form?id=${e.id}">✎</a><button class="icon-link danger" title="Delete" data-delete="${e.id}">×</button>`;
    } else if (currentUser.role === 'HR') {
        actions += `<a class="icon-link" title="Edit" href="/employee-form?id=${e.id}">✎</a>`;
    }
    return `<tr><td><div class="person">${photo(e)}<div class="person-name"><b>${esc(e.firstName)} ${esc(e.lastName)}</b><small>${esc(e.email)}</small></div></div></td><td>${esc(e.jobTitle || '—')}</td><td>${esc(e.department || '—')}</td><td>${esc(e.employmentType || '—')}</td><td>${date(e.joiningDate)}</td><td><span class="status ${e.active ? 'active' : 'inactive'}">${e.active ? 'Active' : 'Inactive'}</span></td><td><div class="table-actions">${actions}</div></td></tr>`;
}

function renderPaginationBar({ container, totalItems, page, pageSize, onPageChange }) {
    if (!container) return;
    const totalPages = Math.ceil(totalItems / pageSize) || 1;
    if (totalPages <= 1) {
        container.innerHTML = '';
        return;
    }
    const start = (page - 1) * pageSize + 1;
    const end = Math.min(page * pageSize, totalItems);

    let pageBtns = '';
    for (let i = 1; i <= totalPages; i++) {
        if (i === 1 || i === totalPages || (i >= page - 1 && i <= page + 1)) {
            pageBtns += `<button type="button" class="page-num-btn ${i === page ? 'active' : ''}" data-p="${i}">${i}</button>`;
        } else if (i === page - 2 || i === page + 2) {
            pageBtns += `<span class="page-dots">…</span>`;
        }
    }

    container.innerHTML = `
        <div class="pagination-bar">
            <span class="pagination-info">Showing <b>${start}–${end}</b> of <b>${totalItems}</b></span>
            <div class="pagination-controls">
                <button type="button" class="page-nav-btn" data-p="${page - 1}" ${page <= 1 ? 'disabled' : ''}>← Prev</button>
                ${pageBtns}
                <button type="button" class="page-nav-btn" data-p="${page + 1}" ${page >= totalPages ? 'disabled' : ''}>Next →</button>
            </div>
        </div>
    `;

    container.querySelectorAll('button[data-p]').forEach(b => {
        b.onclick = () => {
            const p = Number(b.dataset.p);
            if (p >= 1 && p <= totalPages && p !== page) {
                onPageChange(p);
            }
        };
    });
}

/* ==========================================================================
   DASHBOARD
   ========================================================================== */
async function dashboard() {
    try {
        const [employees, departments, stats] = await Promise.all([
            getEmployees(),
            getDepartments(),
            api('/api/dashboard/stats').catch(() => ({}))
        ]);

        const activeEmployees = employees.filter(e => e.active).length;
        if ($('#totalEmployees')) $('#totalEmployees').textContent = stats.totalEmployees ?? employees.length;
        if ($('#activeEmployees')) $('#activeEmployees').textContent = stats.activeEmployees ?? activeEmployees;
        if ($('#activeRate')) $('#activeRate').textContent = employees.length ? `${Math.round(activeEmployees / employees.length * 100)}% active workforce` : 'No records yet';
        if ($('#totalDepartments')) $('#totalDepartments').textContent = stats.totalDepartments ?? departments.length;
        if ($('#monthlyPayroll')) $('#monthlyPayroll').textContent = money(stats.monthlyPayroll ?? (employees.reduce((sum, e) => sum + Number(e.salary || 0), 0) / 12));

        const welcome = $('#welcomeUser');
        if (welcome) welcome.textContent = currentUser.username;

        // 1. Chart.js: Workforce Distribution by Department
        const deptCanvas = $('#deptChartCanvas');
        if (deptCanvas && typeof Chart !== 'undefined') {
            const counts = stats.byDepartment || {};
            const labels = Object.keys(counts);
            const dataValues = Object.values(counts);

            if (window._deptChartInstance) window._deptChartInstance.destroy();
            window._deptChartInstance = new Chart(deptCanvas, {
                type: 'doughnut',
                data: {
                    labels: labels.length ? labels : ['No Departments'],
                    datasets: [{
                        data: dataValues.length ? dataValues : [1],
                        backgroundColor: [
                            '#4f46e5', '#06b6d4', '#10b981', '#f59e0b',
                            '#ec4899', '#8b5cf6', '#3b82f6', '#14b8a6'
                        ],
                        borderWidth: 2,
                        borderColor: document.body.classList.contains('dark') ? '#1e293b' : '#ffffff'
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { position: 'bottom', labels: { boxWidth: 12, font: { size: 11 } } }
                    },
                    cutout: '65%'
                }
            });
        }

        // 2. Chart.js: Today's Attendance Pulse
        const attCanvas = $('#attendanceChartCanvas');
        if (attCanvas && typeof Chart !== 'undefined') {
            const present = stats.presentToday || 0;
            const halfDay = stats.halfDayToday || 0;
            const onLeave = stats.onLeaveToday || 0;
            const absent = stats.absentToday || 0;

            if (window._attChartInstance) window._attChartInstance.destroy();
            window._attChartInstance = new Chart(attCanvas, {
                type: 'doughnut',
                data: {
                    labels: ['Present', 'Half Day', 'On Leave', 'Absent'],
                    datasets: [{
                        data: [present, halfDay, onLeave, absent],
                        backgroundColor: ['#10b981', '#f59e0b', '#8b5cf6', '#ef4444'],
                        borderWidth: 2,
                        borderColor: document.body.classList.contains('dark') ? '#1e293b' : '#ffffff'
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { position: 'bottom', labels: { boxWidth: 12, font: { size: 11 } } }
                    },
                    cutout: '65%'
                }
            });
        }

        // 3. Recent Employees
        const recent = [...employees].sort((a, b) => (b.id || 0) - (a.id || 0)).slice(0, 6);
        const recentContainer = $('#recentEmployees');
        if (recentContainer) {
            recentContainer.innerHTML = recent.length ? `
                <table class="employee-table">
                    <thead><tr><th>Employee</th><th>Role</th><th>Department</th><th>Status</th></tr></thead>
                    <tbody>${recent.map(e => `
                        <tr>
                            <td><div class="person">${photo(e)}<div class="person-name"><b>${esc(e.firstName)} ${esc(e.lastName)}</b><small>${esc(e.email)}</small></div></div></td>
                            <td>${esc(e.jobTitle || '—')}</td>
                            <td>${esc(e.department || '—')}</td>
                            <td><span class="status ${e.active ? 'active' : 'inactive'}">${e.active ? 'Active' : 'Inactive'}</span></td>
                        </tr>`).join('')}
                    </tbody>
                </table>` : '<div class="empty-state">No employees yet. Add your first employee.</div>';
        }

        // 4. Culture & Celebrations (Birthdays & Anniversaries)
        const celebContainer = $('#celebrationsContainer');
        if (celebContainer) {
            const bdays = stats.upcomingBirthdays || [];
            const annivs = stats.upcomingAnniversaries || [];
            if (!bdays.length && !annivs.length) {
                celebContainer.innerHTML = '<div class="empty-state" style="padding:16px;">No upcoming milestones in the next 30 days.</div>';
            } else {
                let html = '';
                bdays.slice(0, 3).forEach(b => {
                    html += `
                        <div style="display:flex;align-items:center;justify-content:space-between;padding:10px 14px;background:var(--surface-hover, rgba(0,0,0,.02));border-radius:10px;">
                            <div style="display:flex;align-items:center;gap:10px;">
                                <span style="font-size:18px;">🎂</span>
                                <div><b style="font-size:13px;">${esc(b.name)}</b><br><small style="color:var(--muted);">${esc(b.department || 'Team Member')}</small></div>
                            </div>
                            <span class="badge blue">Birthday</span>
                        </div>`;
                });
                annivs.slice(0, 3).forEach(a => {
                    html += `
                        <div style="display:flex;align-items:center;justify-content:space-between;padding:10px 14px;background:var(--surface-hover, rgba(0,0,0,.02));border-radius:10px;">
                            <div style="display:flex;align-items:center;gap:10px;">
                                <span style="font-size:18px;">🎖</span>
                                <div><b style="font-size:13px;">${esc(a.name)}</b><br><small style="color:var(--muted);">${esc(a.department || 'Team Member')} • ${a.years} yr milestone</small></div>
                            </div>
                            <span class="badge purple">${a.years} Years</span>
                        </div>`;
                });
                celebContainer.innerHTML = html;
            }
        }

        // 5. Announcements Widget in Dashboard
        const annContainer = $('#dashboardAnnouncements');
        if (annContainer) {
            try {
                const anns = await api('/api/announcements');
                const activeAnns = (anns || []).filter(a => a.active).slice(0, 3);
                if (!activeAnns.length) {
                    annContainer.innerHTML = '<div class="empty-state" style="padding:16px;">No announcements posted yet.</div>';
                } else {
                    annContainer.innerHTML = activeAnns.map(a => {
                        const prioClass = a.priority === 'CRITICAL' ? 'red' : a.priority === 'HIGH' ? 'orange' : 'blue';
                        return `
                            <div style="display:flex;align-items:flex-start;gap:12px;padding:12px 14px;background:var(--surface-hover, rgba(0,0,0,.02));border-radius:12px;border-left:3px solid var(--${prioClass === 'red' ? 'danger' : prioClass === 'orange' ? 'warning' : 'primary'});">
                                <span style="font-size:16px;line-height:1;">${a.pinned ? '📌' : '📢'}</span>
                                <div style="flex:1;">
                                    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:3px;">
                                        <b style="font-size:13px;">${esc(a.title)}</b>
                                        <span class="badge ${prioClass}" style="font-size:10px;">${esc(a.priority)}</span>
                                    </div>
                                    <p style="margin:0 0 4px;font-size:12px;color:var(--text);display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden;">${esc(a.content)}</p>
                                    <small style="color:var(--muted);">${date(a.createdAt ? a.createdAt.split('T')[0] : '')}</small>
                                </div>
                            </div>
                        `;
                    }).join('');
                }
            } catch (_) {
                annContainer.innerHTML = '<div class="empty-state">Unable to load announcements.</div>';
            }
        }
    } catch (e) {
        console.error(e);
        toast(e.message || 'Unable to load dashboard data');
    }
}

/* ==========================================================================
   EMPLOYEES PAGE
   ========================================================================== */
async function employeesPage() {
    let all = [];
    let currentPage = 1;
    const pageSize = 8;
    const search = $('#employeeSearch'), dept = $('#departmentFilter'), status = $('#statusFilter'), table = $('#employeeTable');
    const render = () => {
        const q = (search.value || '').trim().toLowerCase();
        const filtered = all.filter(e => {
            const text = [e.firstName, e.lastName, e.email, e.phone, e.department, e.jobTitle].join(' ').toLowerCase();
            return (!q || text.includes(q)) && (!dept.value || e.department === dept.value) && (!status.value || String(e.active) === status.value);
        });
        $('#resultCount').textContent = `${filtered.length} employee${filtered.length === 1 ? '' : 's'}`;
        if (!filtered.length) {
            table.innerHTML = '<div class="empty-state">No employees match your search.</div>';
            return;
        }

        const totalPages = Math.ceil(filtered.length / pageSize) || 1;
        if (currentPage > totalPages) currentPage = totalPages;
        const startIdx = (currentPage - 1) * pageSize;
        const pageItems = filtered.slice(startIdx, startIdx + pageSize);

        table.innerHTML = `<div class="table-wrap"><table class="employee-table"><thead><tr><th>Employee</th><th>Job title</th><th>Department</th><th>Type</th><th>Joining date</th><th>Status</th><th>Actions</th></tr></thead><tbody>${pageItems.map(employeeRow).join('')}</tbody></table></div><div id="empPagination"></div>`;
        $$('[data-delete]').forEach(b => b.onclick = () => deleteEmployee(b.dataset.delete));

        renderPaginationBar({
            container: $('#empPagination'),
            totalItems: filtered.length,
            page: currentPage,
            pageSize: pageSize,
            onPageChange: (newPage) => {
                currentPage = newPage;
                render();
            }
        });
    };
    try {
        all = await getEmployees();
        const ds = [...new Set(all.map(e => e.department).filter(Boolean))].sort();
        dept.innerHTML = '<option value="">All departments</option>' + ds.map(d => `<option value="${esc(d)}">${esc(d)}</option>`).join('');
        render();
    } catch (e) { toast(e.message); }
    [search, dept, status].forEach(el => el.addEventListener('input', () => { currentPage = 1; render(); }));
    [dept, status].forEach(el => el.addEventListener('change', () => { currentPage = 1; render(); }));
}

async function deleteEmployee(id) {
    if (!confirm('Delete this employee? This cannot be undone.')) return;
    try { await api(`/api/employees/${id}`, { method: 'DELETE' }); toast('Employee deleted'); setTimeout(() => location.reload(), 350); }
    catch (e) { toast(e.message); }
}

async function loadDepartmentOptions(selected) {
    const select = $('#departmentSelect');
    if (!select) return;
    try {
        const departments = await getDepartments();
        const active = departments.filter(d => d.active || d.name === selected);
        select.innerHTML = '<option value="">Select department</option>' + active.map(d => `<option value="${esc(d.name)}">${esc(d.name)}${d.active ? '' : ' (Inactive)'}</option>`).join('');
        if (selected) select.value = selected;
    } catch (e) { select.innerHTML = '<option value="">Unable to load departments</option>'; throw e; }
}

/* ==========================================================================
   EMPLOYEE FORM
   ========================================================================== */
async function formPage() {
    if (currentUser.role === 'EMPLOYEE') { location.href = '/access-denied'; return; }
    const id = new URLSearchParams(location.search).get('id');
    const form = $('#employeeForm');
    let existing = null;
    try {
        if (id) {
            $('#formTitle').textContent = 'Edit employee';
            existing = await api(`/api/employees/${id}`);
        }
        await loadDepartmentOptions(existing?.department);
        if (existing) {
            for (const [k, v] of Object.entries(existing)) {
                const el = form.elements[k];
                if (el && v != null) el.value = k === 'active' ? String(v) : v;
            }
            updatePreview();
        }
    } catch (e) { toast(e.message); }
    $('#photoUrl').addEventListener('input', updatePreview);
    function updatePreview() {
        const url = $('#photoUrl').value.trim();
        $('#photoPreview').innerHTML = url ? `<img src="${esc(url)}" alt="Preview" onerror="this.parentElement.innerHTML='👤'">` : '👤';
    }
    form.onsubmit = async ev => {
        ev.preventDefault();
        const data = Object.fromEntries(new FormData(form).entries());
        data.active = data.active === 'true';
        if (data.salary === '') data.salary = null;
        for (const k of ['dateOfBirth', 'joiningDate']) if (data[k] === '') data[k] = null;
        try {
            await api(id ? `/api/employees/${id}` : '/api/employees', { method: id ? 'PUT' : 'POST', body: JSON.stringify(data) });
            toast(id ? 'Employee updated' : 'Employee created');
            setTimeout(() => location.href = '/employees', 450);
        } catch (e) { $('#formMessage').textContent = e.message; }
    };
}

/* ==========================================================================
   EMPLOYEE PROFILE
   ========================================================================== */
async function profilePage() {
    const id = new URLSearchParams(location.search).get('id');
    if (!id) { location.href = '/employees'; return; }
    try {
        const [e, balances, docs] = await Promise.all([
            api(`/api/employees/${id}`),
            api(`/api/leave-balances/${id}`).catch(() => []),
            api(`/api/documents/${id}`).catch(() => [])
        ]);

        if ($('#editProfile')) {
            if (currentUser.role === 'EMPLOYEE') $('#editProfile').remove();
            else $('#editProfile').href = `/employee-form?id=${e.id}`;
        }

        // Leave Balances HTML
        let balancesHtml = '';
        if (balances && balances.length) {
            balancesHtml = `
                <section class="panel" style="grid-column: 1 / -1; padding: 20px; border-radius: 16px;">
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 14px;">
                        <h3 style="margin:0; font-size:15px; font-weight:700;">Leave Quotas & Balances (${new Date().getFullYear()})</h3>
                        <span style="font-size:12px; color:var(--muted)">Annual entitlement</span>
                    </div>
                    <div class="quota-grid">
                        ${balances.map(b => {
                            const pct = b.allocatedDays > 0 ? Math.min(100, Math.round((b.used / b.allocatedDays) * 100)) : 0;
                            const isLow = b.remaining <= 2;
                            return `
                                <div class="quota-card">
                                    <div class="quota-header">
                                        <span class="quota-title">${esc(b.leaveType)}</span>
                                        <span class="quota-badge" style="background:${isLow ? '#fee2e2' : '#ecfdf5'}; color:${isLow ? '#dc2626' : '#16a34a'};">
                                            ${b.remaining} Left
                                        </span>
                                    </div>
                                    <div class="quota-numbers">
                                        <span class="quota-remain">${b.remaining}</span>
                                        <span class="quota-total">/ ${b.allocatedDays} days</span>
                                    </div>
                                    <div class="quota-bar-bg">
                                        <div class="quota-bar-fill" style="width:${pct}%; background:${isLow ? '#ef4444' : '#635bff'};"></div>
                                    </div>
                                    <small style="color:var(--muted); font-size:11px;">Used: ${b.used} days</small>
                                </div>
                            `;
                        }).join('')}
                    </div>
                </section>
            `;
        }

        // Documents HTML
        const formatBytes = (bytes) => {
            if (!bytes || bytes === 0) return '0 B';
            const k = 1024, dm = 1, sizes = ['B', 'KB', 'MB', 'GB'];
            const i = Math.floor(Math.log(bytes) / Math.log(k));
            return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
        };

        const renderDocsList = (items) => {
            if (!items.length) {
                return '<div class="empty-state" style="padding:16px;">No documents uploaded yet for this employee.</div>';
            }
            return `
                <div class="document-grid">
                    ${items.map(d => `
                        <div class="document-card">
                            <div style="display:flex; align-items:center; gap:12px; overflow:hidden;">
                                <span style="font-size:24px;">📄</span>
                                <div style="overflow:hidden;">
                                    <b style="font-size:13px; display:block; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" title="${esc(d.originalFileName)}">${esc(d.originalFileName)}</b>
                                    <div style="display:flex; gap:6px; align-items:center; margin-top:2px;">
                                        <span class="badge blue" style="font-size:10px;">${esc(d.category || 'OTHER')}</span>
                                        <small style="color:var(--muted);">${formatBytes(d.fileSize)}</small>
                                    </div>
                                    ${d.description ? `<small style="color:var(--muted); font-style:italic;">${esc(d.description)}</small>` : ''}
                                </div>
                            </div>
                            <div style="display:flex; gap:6px; align-items:center;">
                                <a href="/api/documents/download/${d.id}" class="btn secondary" style="padding:4px 8px; font-size:11px;" title="Download" download>⬇</a>
                                ${currentUser.role !== 'EMPLOYEE' ? `<button type="button" class="btn secondary danger" style="padding:4px 8px; font-size:11px;" onclick="deleteDoc(${d.id})" title="Delete">×</button>` : ''}
                            </div>
                        </div>
                    `).join('')}
                </div>
            `;
        };

        let docsSectionHtml = `
            <section class="panel" style="grid-column: 1 / -1; padding: 20px; border-radius: 16px;">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 14px;">
                    <div>
                        <h3 style="margin:0; font-size:15px; font-weight:700;">Employee Documents</h3>
                        <p style="margin:2px 0 0; font-size:12px; color:var(--muted)">Resumes, identity records, offer letters, and contracts</p>
                    </div>
                </div>

                ${currentUser.role !== 'EMPLOYEE' ? `
                    <div style="background:var(--surface-hover, rgba(0,0,0,.02)); border:1px solid var(--border); border-radius:14px; padding:16px; margin-bottom:18px;">
                        <h4 style="margin:0 0 10px; font-size:13px; font-weight:600;">Upload New Document</h4>
                        <form id="docUploadForm" style="display:flex; flex-wrap:wrap; gap:12px; align-items:center;">
                            <input type="file" id="docFileInput" required style="font-size:12px;">
                            <select id="docCategorySelect" style="padding:6px 10px; border-radius:8px; border:1px solid var(--border); font-size:12px; background:var(--surface); color:var(--text);">
                                <option value="RESUME">Resume / CV</option>
                                <option value="ID_PROOF">ID Proof</option>
                                <option value="OFFER_LETTER">Offer Letter</option>
                                <option value="CONTRACT">Contract</option>
                                <option value="OTHER">Other</option>
                            </select>
                            <input type="text" id="docDescInput" placeholder="Brief note / description..." style="flex:1; min-width:180px; padding:6px 10px; border-radius:8px; border:1px solid var(--border); font-size:12px; background:var(--surface); color:var(--text);">
                            <button type="submit" class="btn primary" id="docUploadBtn" style="font-size:12px; padding:6px 14px;">Upload Document</button>
                        </form>
                    </div>
                ` : ''}

                <div id="docsListContainer">
                    ${renderDocsList(docs)}
                </div>
            </section>
        `;

        $('#profileContent').innerHTML = `
            <div class="profile-hero">
                <div class="profile-main">
                    ${e.photoUrl ? `<img class="profile-photo" src="${esc(e.photoUrl)}" alt="Profile" loading="lazy">` : `<div class="profile-photo">${esc(initials(e))}</div>`}
                    <div>
                        <h2>${esc(e.firstName)} ${esc(e.lastName)}</h2>
                        <p>${esc(e.jobTitle || 'Employee')} · ${esc(e.department || 'Unassigned')}</p>
                        <div class="profile-meta">
                            <span>${e.active ? '● Active' : '● Inactive'}</span>
                            <span>${esc(e.employmentType || 'Employment type not set')}</span>
                            <span>Employee #${e.id}</span>
                        </div>
                    </div>
                </div>
                <div>
                    ${currentUser.role !== 'EMPLOYEE' ? `<a class="btn primary" href="/employee-form?id=${e.id}">✎ Edit profile</a>` : ''}
                </div>
            </div>

            <div class="profile-grid">
                <section class="panel detail-card">
                    <h3>Personal information</h3>
                    <div class="details">
                        <div><label>Email</label><p>${esc(e.email)}</p></div>
                        <div><label>Phone</label><p>${esc(e.phone || '—')}</p></div>
                        <div><label>Date of birth</label><p>${date(e.dateOfBirth)}</p></div>
                        <div><label>Gender</label><p>${esc(e.gender || '—')}</p></div>
                    </div>
                </section>
                <section class="panel detail-card">
                    <h3>Employment details</h3>
                    <div class="details">
                        <div><label>Department</label><p>${esc(e.department || '—')}</p></div>
                        <div><label>Job title</label><p>${esc(e.jobTitle || '—')}</p></div>
                        <div><label>Employment type</label><p>${esc(e.employmentType || '—')}</p></div>
                        <div><label>Joining date</label><p>${date(e.joiningDate)}</p></div>
                        <div><label>Annual salary</label><p>${money(e.salary)}</p></div>
                        <div><label>Status</label><p>${e.active ? 'Active' : 'Inactive'}</p></div>
                    </div>
                </section>
                <section class="panel detail-card">
                    <h3>Address</h3>
                    <div class="details">
                        <div><label>Street</label><p>${esc(e.address || '—')}</p></div>
                        <div><label>City</label><p>${esc(e.city || '—')}</p></div>
                        <div><label>State</label><p>${esc(e.state || '—')}</p></div>
                        <div><label>Postal code</label><p>${esc(e.postalCode || '—')}</p></div>
                    </div>
                </section>
                <section class="panel detail-card">
                    <h3>Emergency contact</h3>
                    <div class="details">
                        <div><label>Name</label><p>${esc(e.emergencyContactName || '—')}</p></div>
                        <div><label>Phone</label><p>${esc(e.emergencyContactPhone || '—')}</p></div>
                    </div>
                </section>

                ${balancesHtml}
                ${docsSectionHtml}
            </div>
        `;

        // Bind upload form
        const uploadForm = $('#docUploadForm');
        if (uploadForm) {
            uploadForm.onsubmit = async (ev) => {
                ev.preventDefault();
                const fileInput = $('#docFileInput');
                if (!fileInput.files.length) {
                    toast('Please select a file to upload');
                    return;
                }
                const btn = $('#docUploadBtn');
                btn.disabled = true;
                btn.textContent = 'Uploading...';

                const fd = new FormData();
                fd.append('file', fileInput.files[0]);
                fd.append('category', $('#docCategorySelect').value);
                const desc = $('#docDescInput').value.trim();
                if (desc) fd.append('description', desc);

                try {
                    const token = csrfToken();
                    const headers = {};
                    if (token) headers['X-XSRF-TOKEN'] = token;
                    const res = await fetch(`/api/documents/${e.id}/upload`, {
                        method: 'POST',
                        body: fd,
                        headers
                    });
                    if (!res.ok) throw new Error('Upload failed (' + res.status + ')');
                    toast('Document uploaded successfully');
                    await profilePage();
                } catch (err) {
                    toast(err.message || 'Failed to upload document');
                } finally {
                    btn.disabled = false;
                    btn.textContent = 'Upload Document';
                }
            };
        }

        window.deleteDoc = async (docId) => {
            if (!confirm('Are you sure you want to delete this document?')) return;
            try {
                await api(`/api/documents/${docId}`, { method: 'DELETE' });
                toast('Document deleted');
                await profilePage();
            } catch (err) {
                toast(err.message);
            }
        };

    } catch (e) {
        $('#profileContent').innerHTML = `<div class="panel empty-state">${esc(e.message)}</div>`;
    }
}

/* ==========================================================================
   DEPARTMENTS
   ========================================================================== */
let editingDepartmentId = null;
async function departmentsPage() {
    const addBtn = $('#addDepartmentBtn');
    if (addBtn && currentUser.role !== 'EMPLOYEE') addBtn.onclick = openAddDepartment;
    await loadDepartmentsPage();
}

async function loadDepartmentsPage() {
    try {
        const [departments, employees] = await Promise.all([getDepartments(), getEmployees()]);
        $('#totalDepartments').textContent = departments.length;
        $('#activeDepartments').textContent = departments.filter(d => d.active).length;
        const counts = employees.reduce((m, e) => { if (e.department) m[e.department.toLowerCase()] = (m[e.department.toLowerCase()] || 0) + 1; return m; }, {});
        $('#departmentTable').innerHTML = departments.length ? `<div style="overflow-x:auto"><table class="employee-table"><thead><tr><th>Department</th><th>Department Head</th><th>Description</th><th>Employees</th><th>Status</th><th>Actions</th></tr></thead><tbody>${departments.map(d => {
            const count = counts[d.name.toLowerCase()] || 0;
            const actions = currentUser.role === 'EMPLOYEE' ? '' : `<button class="btn" data-dept-edit="${d.id}">Edit</button>${currentUser.role === 'ADMIN' ? `<button class="btn" data-dept-delete="${d.id}">Delete</button>` : ''}`;
            return `<tr><td><strong>${esc(d.name)}</strong></td><td>${esc(d.departmentHead || '—')}</td><td>${esc(d.description || '—')}</td><td>${count}</td><td><span class="status ${d.active ? 'active' : 'inactive'}">${d.active ? 'Active' : 'Inactive'}</span></td><td><div class="table-actions">${actions || '—'}</div></td></tr>`;
        }).join('')}</tbody></table></div>` : '<div class="empty-state">No departments yet.</div>';
        $$('[data-dept-edit]').forEach(b => b.onclick = () => editDepartment(b.dataset.deptEdit));
        $$('[data-dept-delete]').forEach(b => b.onclick = () => deleteDepartment(b.dataset.deptDelete));
    } catch (e) { toast(e.message); $('#departmentTable').innerHTML = `<div class="empty-state">${esc(e.message)}</div>`; }
}

function openAddDepartment() {
    editingDepartmentId = null;
    $('#modalTitle').textContent = 'Add Department';
    $('#departmentForm').reset();
    $('#departmentActive').checked = true;
    $('#departmentModal').style.display = 'flex';
}

async function editDepartment(id) {
    try {
        const d = await api(`/api/departments/${id}`);
        editingDepartmentId = id;
        $('#modalTitle').textContent = 'Edit Department';
        $('#departmentName').value = d.name || '';
        $('#departmentHead').value = d.departmentHead || '';
        $('#departmentDescription').value = d.description || '';
        $('#departmentActive').checked = d.active;
        $('#departmentModal').style.display = 'flex';
    } catch (e) { toast(e.message); }
}

async function deleteDepartment(id) {
    if (!confirm('Delete this department? Departments with assigned employees cannot be deleted.')) return;
    try { await api(`/api/departments/${id}`, { method: 'DELETE' }); toast('Department deleted'); await loadDepartmentsPage(); }
    catch (e) { toast(e.message); }
}

function closeModal() { const m = $('#departmentModal'); if (m) m.style.display = 'none'; }
window.closeModal = closeModal;

async function bindDepartmentForm() {
    const form = $('#departmentForm');
    if (!form) return;
    form.addEventListener('submit', async ev => {
        ev.preventDefault();
        const payload = {
            name: $('#departmentName').value,
            departmentHead: $('#departmentHead').value,
            description: $('#departmentDescription').value,
            active: $('#departmentActive').checked
        };
        try {
            await api(editingDepartmentId ? `/api/departments/${editingDepartmentId}` : '/api/departments', { method: editingDepartmentId ? 'PUT' : 'POST', body: JSON.stringify(payload) });
            closeModal();
            toast(editingDepartmentId ? 'Department updated' : 'Department created');
            await loadDepartmentsPage();
        } catch (e) { toast(e.message); }
    });
}

/* ==========================================================================
   ATTENDANCE
   ========================================================================== */
async function attendancePage() {
    let logs = [];
    const search = $('#attendanceSearch');
    const dateInput = $('#attendanceDateFilter');
    const statusSelect = $('#attendanceStatusFilter');
    const container = $('#attendanceTableContainer');

    async function loadSummary() {
        try {
            const s = await api('/api/attendance/summary');
            if ($('#presentCount')) $('#presentCount').textContent = s.present || 0;
            if ($('#halfDayCount')) $('#halfDayCount').textContent = s.halfDay || 0;
            if ($('#leaveCount')) $('#leaveCount').textContent = s.onLeave || 0;
            if ($('#absentCount')) $('#absentCount').textContent = s.absent || 0;
        } catch (_) {}
    }

    async function syncClockButtons() {
        try {
            const todayRecord = await api('/api/attendance/today');
            const inBtn = $('#clockInBtn');
            const outBtn = $('#clockOutBtn');
            if (todayRecord && todayRecord.checkInTime) {
                if (todayRecord.checkOutTime) {
                    if (inBtn) inBtn.style.display = 'none';
                    if (outBtn) {
                        outBtn.style.display = 'inline-flex';
                        outBtn.textContent = 'Shift Ended (' + todayRecord.checkOutTime + ')';
                        outBtn.disabled = true;
                    }
                } else {
                    if (inBtn) inBtn.style.display = 'none';
                    if (outBtn) {
                        outBtn.style.display = 'inline-flex';
                        outBtn.textContent = 'Clock Out (' + todayRecord.checkInTime + ') ⏹';
                        outBtn.disabled = false;
                    }
                }
            } else {
                if (inBtn) inBtn.style.display = 'inline-flex';
                if (outBtn) outBtn.style.display = 'none';
            }
        } catch (_) {}
    }

    let currentPage = 1;
    const pageSize = 10;

    const render = () => {
        const q = (search?.value || '').trim().toLowerCase();
        const stat = statusSelect?.value || '';
        const dt = dateInput?.value || '';

        const filtered = logs.filter(a => {
            const matchQuery = !q || (a.employeeName || '').toLowerCase().includes(q) || (a.remarks || '').toLowerCase().includes(q);
            const matchStatus = !stat || a.status === stat;
            const matchDate = !dt || a.date === dt;
            return matchQuery && matchStatus && matchDate;
        });

        if ($('#attendanceResultCount')) $('#attendanceResultCount').textContent = `${filtered.length} record${filtered.length === 1 ? '' : 's'}`;

        if (!container) return;
        if (!filtered.length) {
            container.innerHTML = '<div class="empty-state">No attendance records found.</div>';
            return;
        }

        const totalPages = Math.ceil(filtered.length / pageSize) || 1;
        if (currentPage > totalPages) currentPage = totalPages;
        const startIdx = (currentPage - 1) * pageSize;
        const pageItems = filtered.slice(startIdx, startIdx + pageSize);

        const getStatusPill = s => {
            if (s === 'PRESENT') return '<span class="status active">● Present</span>';
            if (s === 'HALF_DAY') return '<span class="status" style="background:#fff3df;color:#d97706">◑ Half Day</span>';
            if (s === 'ON_LEAVE') return '<span class="status" style="background:#eeecff;color:#635bff">♧ On Leave</span>';
            return '<span class="status inactive">✕ Absent</span>';
        };

        container.innerHTML = `<div class="table-wrap"><table class="employee-table">
            <thead>
                <tr>
                    <th>Employee</th>
                    <th>Date</th>
                    <th>Check In</th>
                    <th>Check Out</th>
                    <th>Work Hours</th>
                    <th>Status</th>
                    <th>Remarks</th>
                </tr>
            </thead>
            <tbody>
                ${pageItems.map(a => `<tr>
                    <td><strong>${esc(a.employeeName || 'Employee #' + a.employeeId)}</strong></td>
                    <td>${date(a.date)}</td>
                    <td>${esc(a.checkInTime || '—')}</td>
                    <td>${esc(a.checkOutTime || '—')}</td>
                    <td>${a.workHours != null ? a.workHours + ' hrs' : '—'}</td>
                    <td>${getStatusPill(a.status)}</td>
                    <td><small style="color:var(--muted)">${esc(a.remarks || '—')}</small></td>
                </tr>`).join('')}
            </tbody>
        </table></div><div id="attPagination"></div>`;

        renderPaginationBar({
            container: $('#attPagination'),
            totalItems: filtered.length,
            page: currentPage,
            pageSize: pageSize,
            onPageChange: (newPage) => {
                currentPage = newPage;
                render();
            }
        });
    };

    try {
        await Promise.all([loadSummary(), syncClockButtons()]);
        logs = await api('/api/attendance');
        render();
    } catch (e) {
        toast(e.message);
        if (container) container.innerHTML = `<div class="empty-state">${esc(e.message)}</div>`;
    }

    if (search) search.addEventListener('input', () => { currentPage = 1; render(); });
    if (dateInput) dateInput.addEventListener('change', () => { currentPage = 1; render(); });
    if (statusSelect) statusSelect.addEventListener('change', () => { currentPage = 1; render(); });

    const inBtn = $('#clockInBtn');
    if (inBtn) {
        inBtn.onclick = async () => {
            try {
                await api('/api/attendance/clock-in', { method: 'POST' });
                toast('Successfully clocked in!');
                await attendancePage();
            } catch (e) { toast(e.message); }
        };
    }

    const outBtn = $('#clockOutBtn');
    if (outBtn) {
        outBtn.onclick = async () => {
            try {
                await api('/api/attendance/clock-out', { method: 'POST' });
                toast('Successfully clocked out! Great work today.');
                await attendancePage();
            } catch (e) { toast(e.message); }
        };
    }

    // Bind mark attendance modal
    const recBtn = $('#recordAttendanceBtn');
    const modal = $('#attendanceModal');
    if (recBtn && modal) {
        recBtn.onclick = async () => {
            const select = $('#modalEmployeeSelect');
            if (select && select.options.length <= 1) {
                try {
                    const emps = await getEmployees();
                    select.innerHTML = '<option value="">Select Employee</option>' + emps.map(e => `<option value="${e.id}">${esc(e.firstName)} ${esc(e.lastName)} (${esc(e.department)})</option>`).join('');
                } catch (_) {}
            }
            $('#modalDate').value = new Date().toISOString().split('T')[0];
            modal.style.display = 'flex';
        };
    }

    const form = $('#attendanceForm');
    if (form) {
        form.onsubmit = async ev => {
            ev.preventDefault();
            const payload = {
                employeeId: Number($('#modalEmployeeSelect').value),
                date: $('#modalDate').value,
                checkInTime: $('#modalCheckIn').value || null,
                checkOutTime: $('#modalCheckOut').value || null,
                status: $('#modalStatus').value,
                remarks: $('#modalRemarks').value || null
            };
            try {
                await api('/api/attendance', { method: 'POST', body: JSON.stringify(payload) });
                if (modal) modal.style.display = 'none';
                toast('Attendance recorded');
                await attendancePage();
            } catch (e) { toast(e.message); }
        };
    }
}

/* ==========================================================================
   LEAVE MANAGEMENT
   ========================================================================== */
async function leavePage() {
    let leaves = [];
    let currentPage = 1;
    const pageSize = 8;
    const search = $('#leaveSearch');
    const statusSelect = $('#leaveStatusFilter');
    const typeSelect = $('#leaveTypeFilter');
    const container = $('#leaveTableContainer');

    async function loadSummary() {
        try {
            const s = await api('/api/leaves/summary');
            if ($('#totalLeaveRequests')) $('#totalLeaveRequests').textContent = s.total || 0;
            if ($('#pendingLeaveRequests')) $('#pendingLeaveRequests').textContent = s.pending || 0;
            if ($('#approvedLeaveRequests')) $('#approvedLeaveRequests').textContent = s.approved || 0;
            if ($('#rejectedLeaveRequests')) $('#rejectedLeaveRequests').textContent = s.rejected || 0;
        } catch (_) {}
    }

    async function loadBalances() {
        const qGrid = $('#leaveQuotaGrid');
        if (!qGrid) return;
        try {
            const balances = await api('/api/leave-balances');
            if (!balances || !balances.length) {
                qGrid.innerHTML = '<div class="empty-state" style="padding:14px;">No personal leave quotas recorded for the current year.</div>';
                return;
            }
            qGrid.innerHTML = balances.map(b => {
                const pct = b.allocatedDays > 0 ? Math.min(100, Math.round((b.used / b.allocatedDays) * 100)) : 0;
                const isLow = b.remaining <= 2;
                return `
                    <div class="quota-card">
                        <div class="quota-header">
                            <span class="quota-title">${esc(b.leaveType)}</span>
                            <span class="quota-badge" style="background:${isLow ? '#fee2e2' : '#ecfdf5'}; color:${isLow ? '#dc2626' : '#16a34a'};">
                                ${b.remaining} Left
                            </span>
                        </div>
                        <div class="quota-numbers">
                            <span class="quota-remain">${b.remaining}</span>
                            <span class="quota-total">/ ${b.allocatedDays} days</span>
                        </div>
                        <div class="quota-bar-bg">
                            <div class="quota-bar-fill" style="width:${pct}%; background:${isLow ? '#ef4444' : '#635bff'};"></div>
                        </div>
                        <small style="color:var(--muted); font-size:11px;">Used: ${b.used} days • Year ${b.year}</small>
                    </div>
                `;
            }).join('');
        } catch (_) {
            qGrid.innerHTML = '<div class="empty-state" style="padding:14px;">Leave quota tracking available for linked profiles.</div>';
        }
    }

    const render = () => {
        const q = (search?.value || '').trim().toLowerCase();
        const stat = statusSelect?.value || '';
        const typ = typeSelect?.value || '';

        const filtered = leaves.filter(l => {
            const matchQuery = !q || (l.employeeName || '').toLowerCase().includes(q) || (l.reason || '').toLowerCase().includes(q);
            const matchStatus = !stat || l.status === stat;
            const matchType = !typ || l.leaveType === typ;
            return matchQuery && matchStatus && matchType;
        });

        if ($('#leaveResultCount')) $('#leaveResultCount').textContent = `${filtered.length} request${filtered.length === 1 ? '' : 's'}`;

        if (!container) return;
        if (!filtered.length) {
            container.innerHTML = '<div class="empty-state">No leave applications found.</div>';
            return;
        }

        const totalPages = Math.ceil(filtered.length / pageSize) || 1;
        if (currentPage > totalPages) currentPage = totalPages;
        const startIdx = (currentPage - 1) * pageSize;
        const pageItems = filtered.slice(startIdx, startIdx + pageSize);

        const getStatusPill = s => {
            if (s === 'APPROVED') return '<span class="status active">✓ Approved</span>';
            if (s === 'REJECTED') return '<span class="status inactive">✕ Rejected</span>';
            return '<span class="status" style="background:#fff3df;color:#d97706">◷ Pending</span>';
        };

        container.innerHTML = `<div class="table-wrap"><table class="employee-table">
            <thead>
                <tr>
                    <th>Employee</th>
                    <th>Type</th>
                    <th>Duration</th>
                    <th>Days</th>
                    <th>Reason</th>
                    <th>Applied On</th>
                    <th>Status</th>
                    <th>Reviewer</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                ${pageItems.map(l => {
                    let actions = '—';
                    if (currentUser.role !== 'EMPLOYEE' && l.status === 'PENDING') {
                        actions = `<button class="btn" style="min-height:28px;padding:0 9px;font-size:10px;background:var(--success-soft);color:var(--success);border:0;" data-approve="${l.id}">Approve</button>
                                   <button class="btn" style="min-height:28px;padding:0 9px;font-size:10px;background:var(--danger-soft);color:var(--danger);border:0;" data-reject="${l.id}">Reject</button>`;
                    }
                    return `<tr>
                        <td><strong>${esc(l.employeeName || 'Employee #' + l.employeeId)}</strong><br><small style="color:var(--muted)">${esc(l.department || '')}</small></td>
                        <td>${esc(l.leaveType)}</td>
                        <td>${date(l.startDate)} → ${date(l.endDate)}</td>
                        <td><b>${l.totalDays} day${l.totalDays === 1 ? '' : 's'}</b></td>
                        <td style="max-width:200px;white-space:normal;"><small>${esc(l.reason || '—')}</small></td>
                        <td>${date(l.appliedAt)}</td>
                        <td>${getStatusPill(l.status)}</td>
                        <td><small>${esc(l.reviewedBy ? l.reviewedBy + (l.reviewerComment ? ': ' + l.reviewerComment : '') : '—')}</small></td>
                        <td><div class="table-actions">${actions}</div></td>
                    </tr>`;
                }).join('')}
            </tbody>
        </table></div><div id="leavePagination"></div>`;

        $$('[data-approve]').forEach(b => b.onclick = () => reviewLeave(b.dataset.approve, 'approve'));
        $$('[data-reject]').forEach(b => b.onclick = () => reviewLeave(b.dataset.reject, 'reject'));

        renderPaginationBar({
            container: $('#leavePagination'),
            totalItems: filtered.length,
            page: currentPage,
            pageSize: pageSize,
            onPageChange: (newPage) => {
                currentPage = newPage;
                render();
            }
        });
    };

    async function reviewLeave(id, decision) {
        const comment = prompt(`Enter optional note for ${decision}:`, decision === 'approve' ? 'Approved' : 'Rejected');
        if (comment === null) return;
        try {
            await api(`/api/leaves/${id}/${decision}`, { method: 'POST', body: JSON.stringify({ comment }) });
            toast(`Leave request ${decision}d`);
            await leavePage();
        } catch (e) { toast(e.message); }
    }

    try {
        await Promise.all([loadSummary(), loadBalances()]);
        leaves = await api('/api/leaves');
        render();
    } catch (e) {
        toast(e.message);
        if (container) container.innerHTML = `<div class="empty-state">${esc(e.message)}</div>`;
    }

    if (search) search.addEventListener('input', () => { currentPage = 1; render(); });
    if (statusSelect) statusSelect.addEventListener('change', () => { currentPage = 1; render(); });
    if (typeSelect) typeSelect.addEventListener('change', () => { currentPage = 1; render(); });

    // Apply leave modal
    const applyBtn = $('#applyLeaveBtn');
    const modal = $('#leaveModal');
    if (applyBtn && modal) {
        applyBtn.onclick = async () => {
            const empField = $('#leaveEmployeeField');
            const select = $('#leaveEmployeeSelect');
            if (currentUser.role !== 'EMPLOYEE') {
                if (empField) empField.style.display = 'grid';
                if (select && select.options.length <= 1) {
                    try {
                        const emps = await getEmployees();
                        select.innerHTML = '<option value="">Select Employee</option>' + emps.map(e => `<option value="${e.id}">${esc(e.firstName)} ${esc(e.lastName)} (${esc(e.department)})</option>`).join('');
                    } catch (_) {}
                }
            } else {
                if (empField) empField.style.display = 'none';
            }
            modal.style.display = 'flex';
        };
    }

    const form = $('#leaveForm');
    if (form) {
        form.onsubmit = async ev => {
            ev.preventDefault();
            const payload = {
                leaveType: $('#leaveType').value,
                startDate: $('#leaveStartDate').value,
                endDate: $('#leaveEndDate').value,
                reason: $('#leaveReason').value
            };
            if (currentUser.role !== 'EMPLOYEE') {
                const empId = $('#leaveEmployeeSelect')?.value;
                if (!empId) { alert('Please select an employee'); return; }
                payload.employeeId = Number(empId);
            }
            try {
                await api('/api/leaves/apply', { method: 'POST', body: JSON.stringify(payload) });
                if (modal) modal.style.display = 'none';
                form.reset();
                toast('Leave request submitted successfully');
                await leavePage();
            } catch (e) { toast(e.message); }
        };
    }
}

/* ==========================================================================
   PAYROLL & PDF PAYSLIPS
   ========================================================================== */
async function payrollPage() {
    let records = [];
    const search = $('#payrollSearch');
    const deptFilter = $('#payrollDepartmentFilter');
    const statusFilter = $('#payrollStatusFilter');
    const container = $('#payrollTableContainer');

    const render = () => {
        const q = (search?.value || '').trim().toLowerCase();
        const dept = deptFilter?.value || '';
        const stat = statusFilter?.value || '';

        const filtered = records.filter(p => {
            const empName = p.employeeName || p.name || '';
            const matchQuery = !q || empName.toLowerCase().includes(q) || (p.department || '').toLowerCase().includes(q) || (p.jobTitle || '').toLowerCase().includes(q);
            const matchDept = !dept || p.department === dept;
            const matchStat = !stat || p.status === stat;
            return matchQuery && matchDept && matchStat;
        });

        if ($('#payrollResultCount')) $('#payrollResultCount').textContent = `${filtered.length} record${filtered.length === 1 ? '' : 's'}`;

        if (!container) return;
        if (!filtered.length) {
            container.innerHTML = '<div class="empty-state">No payroll or payslip records match your search criteria.</div>';
            return;
        }

        container.innerHTML = `<div class="table-wrap"><table class="employee-table">
            <thead>
                <tr>
                    <th>Employee</th>
                    <th>Department</th>
                    <th>Pay Period</th>
                    <th>Gross Earnings</th>
                    <th>Deductions</th>
                    <th>Net Salary</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                ${filtered.map(p => `<tr>
                    <td><strong>${esc(p.employeeName || p.name)}</strong><br><small style="color:var(--muted)">${esc(p.jobTitle || 'Specialist')}</small></td>
                    <td>${esc(p.department || '—')}</td>
                    <td><span class="badge blue">${esc(p.payPeriod || 'Monthly')}</span></td>
                    <td><b>${money(p.grossEarnings || p.monthlyGross)}</b></td>
                    <td style="color:var(--danger)">-${money(p.totalDeductions)}</td>
                    <td style="color:var(--success);font-weight:800;font-size:12px;">${money(p.netSalary || p.netPay)}</td>
                    <td><span class="badge ${p.status === 'PAID' ? 'green' : 'orange'}">${esc(p.status || 'GENERATED')}</span></td>
                    <td>
                        <div style="display:flex;gap:6px;align-items:center;">
                            ${p.id ? `<a class="btn primary" style="min-height:28px;padding:0 8px;font-size:11px;text-decoration:none;" href="/api/payroll/payslips/${p.id}/pdf" target="_blank" title="Download Official PDF">📥 PDF</a>` : ''}
                            <button class="btn secondary" style="min-height:28px;padding:0 8px;font-size:11px;" data-preview="${p.id || p.employeeId}">Preview</button>
                            ${currentUser.role !== 'EMPLOYEE' && p.id && p.status !== 'PAID' ? `<button class="btn secondary" style="min-height:28px;padding:0 8px;font-size:11px;color:var(--success);" data-pay="${p.id}">✓ Disburse</button>` : ''}
                        </div>
                    </td>
                </tr>`).join('')}
            </tbody>
        </table></div>`;

        $$('[data-preview]').forEach(b => b.onclick = () => openPayslip(b.dataset.preview));
        $$('[data-pay]').forEach(b => b.onclick = async () => {
            try {
                await api(`/api/payroll/payslips/${b.dataset.pay}/pay`, { method: 'POST' });
                toast('Disbursement recorded successfully!');
                await payrollPage();
            } catch (e) { toast(e.message); }
        });
    };

    function openPayslip(idOrEmpId) {
        const p = records.find(r => String(r.id) === String(idOrEmpId) || String(r.employeeId) === String(idOrEmpId));
        if (!p) return;

        const period = p.payPeriod || new Date().toLocaleDateString('en-IN', { month: 'long', year: 'numeric' });
        $('#payslipContent').innerHTML = `
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:15px;margin-bottom:20px;padding:16px;background:var(--surface-2);border-radius:12px;">
                <div>
                    <label style="color:var(--muted);font-size:9px;text-transform:uppercase;">Employee Name</label>
                    <p style="margin:2px 0 0;font-size:14px;font-weight:750;">${esc(p.employeeName || p.name)}</p>
                    <small style="color:var(--muted)">Role: ${esc(p.jobTitle || 'Specialist')}</small>
                </div>
                <div>
                    <label style="color:var(--muted);font-size:9px;text-transform:uppercase;">Pay Period</label>
                    <p style="margin:2px 0 0;font-size:14px;font-weight:750;">${esc(period)}</p>
                    <small style="color:var(--muted)">Department: ${esc(p.department || '—')}</small>
                </div>
            </div>

            <div style="display:grid;grid-template-columns:1fr 1fr;gap:20px;">
                <div class="panel" style="padding:16px;">
                    <h3 style="margin:0 0 12px;font-size:12px;color:var(--success);">EARNINGS</h3>
                    <div style="display:grid;gap:8px;font-size:11px;">
                        <div style="display:flex;justify-content:space-between;"><span>Basic Salary</span><b>${money(p.basicSalary || p.basic)}</b></div>
                        <div style="display:flex;justify-content:space-between;"><span>House Rent Allowance (HRA)</span><b>${money(p.hra)}</b></div>
                        <div style="display:flex;justify-content:space-between;"><span>Special Allowance</span><b>${money(p.specialAllowance)}</b></div>
                        <div style="display:flex;justify-content:space-between;"><span>Bonus / Incentive</span><b>${money(p.bonus || 0)}</b></div>
                        <div style="display:flex;justify-content:space-between;border-top:1px solid var(--border);padding-top:8px;font-size:12px;">
                            <strong>Gross Earnings</strong><strong style="color:var(--text)">${money(p.grossEarnings || p.monthlyGross)}</strong>
                        </div>
                    </div>
                </div>

                <div class="panel" style="padding:16px;">
                    <h3 style="margin:0 0 12px;font-size:12px;color:var(--danger);">DEDUCTIONS</h3>
                    <div style="display:grid;gap:8px;font-size:11px;">
                        <div style="display:flex;justify-content:space-between;"><span>Provident Fund (PF)</span><b>${money(p.pfDeduction)}</b></div>
                        <div style="display:flex;justify-content:space-between;"><span>Professional Tax (PT)</span><b>${money(p.professionalTax || p.profTax)}</b></div>
                        <div style="display:flex;justify-content:space-between;"><span>Income Tax (TDS)</span><b>${money(p.incomeTax || 0)}</b></div>
                        <div style="display:flex;justify-content:space-between;border-top:1px solid var(--border);padding-top:8px;font-size:12px;">
                            <strong>Total Deductions</strong><strong style="color:var(--danger)">${money(p.totalDeductions)}</strong>
                        </div>
                    </div>
                </div>
            </div>

            <div style="margin-top:20px;padding:18px;background:var(--primary-soft);border-radius:12px;display:flex;justify-content:space-between;align-items:center;">
                <div>
                    <span style="color:var(--primary);font-size:10px;text-transform:uppercase;font-weight:800;">Net Disbursed Amount</span>
                    <p style="margin:2px 0 0;font-size:10px;color:var(--muted)">Status: <b style="color:var(--success)">${esc(p.status || 'GENERATED')}</b> • Ref: ${esc(p.transactionReference || 'TXN-DISBURSED')}</p>
                </div>
                <strong style="font-size:24px;color:var(--primary);">${money(p.netSalary || p.netPay)}</strong>
            </div>
        `;

        const downloadLink = $('#downloadPdfLink');
        if (downloadLink) {
            if (p.id) {
                downloadLink.style.display = 'inline-flex';
                downloadLink.href = `/api/payroll/payslips/${p.id}/pdf`;
            } else {
                downloadLink.style.display = 'none';
            }
        }

        $('#payslipModal').style.display = 'flex';
    }

    try {
        records = await api('/api/payroll');
        const gross = records.reduce((s, r) => s + Number(r.grossEarnings || r.monthlyGross || 0), 0);
        const deductions = records.reduce((s, r) => s + Number(r.totalDeductions || 0), 0);
        const net = records.reduce((s, r) => s + Number(r.netSalary || r.netPay || 0), 0);

        if ($('#payrollMonthlyGross')) $('#payrollMonthlyGross').textContent = money(gross);
        if ($('#payrollTotalDeductions')) $('#payrollTotalDeductions').textContent = money(deductions);
        if ($('#payrollNetPay')) $('#payrollNetPay').textContent = money(net);
        if ($('#payrollEmployeeCount')) $('#payrollEmployeeCount').textContent = records.length;

        if (deptFilter) {
            const ds = [...new Set(records.map(r => r.department).filter(Boolean))].sort();
            deptFilter.innerHTML = '<option value="">All Departments</option>' + ds.map(d => `<option value="${esc(d)}">${esc(d)}</option>`).join('');
        }

        render();
    } catch (e) {
        toast(e.message);
        if (container) container.innerHTML = `<div class="empty-state">${esc(e.message)}</div>`;
    }

    if (search) search.addEventListener('input', render);
    if (deptFilter) deptFilter.addEventListener('change', render);
    if (statusFilter) statusFilter.addEventListener('change', render);
}

function openRunPayrollModal() {
    const modal = $('#runPayrollModal');
    if (modal) modal.style.display = 'flex';
}

function closeRunPayrollModal() {
    const modal = $('#runPayrollModal');
    if (modal) modal.style.display = 'none';
}

async function handleRunPayrollSubmit(e) {
    e.preventDefault();
    const year = Number($('#runPayrollYear').value);
    const month = Number($('#runPayrollMonth').value);
    try {
        const res = await api('/api/payroll/run', {
            method: 'POST',
            body: JSON.stringify({ year, month })
        });
        closeRunPayrollModal();
        toast(`Monthly payroll executed: ${res.length} payslips processed!`);
        await payrollPage();
    } catch (err) { toast(err.message); }
}

/* ==========================================================================
   REPORTS & ANALYTICS
   ========================================================================== */
async function reportsPage() {
    try {
        const summary = await api('/api/reports/summary');

        if ($('#repTotalEmployees')) $('#repTotalEmployees').textContent = summary.totalEmployees;
        if ($('#repActiveCount')) $('#repActiveCount').textContent = summary.activeCount;
        if ($('#repActiveRatio')) $('#repActiveRatio').textContent = summary.totalEmployees ? `${Math.round(summary.activeCount / summary.totalEmployees * 100)}% active workforce` : '—';
        if ($('#repAnnualPayroll')) $('#repAnnualPayroll').textContent = money(summary.totalAnnualSalary);
        if ($('#repAvgSalary')) $('#repAvgSalary').textContent = money(summary.avgAnnualSalary);

        // Headcount by department chart
        const deptEntries = Object.entries(summary.byDepartment || {}).sort((a, b) => b[1] - a[1]);
        const deptMax = Math.max(...deptEntries.map(([, c]) => c), 1);
        const deptChart = $('#repDeptChart');
        if (deptChart) {
            deptChart.innerHTML = deptEntries.length ? deptEntries.map(([d, c]) => `
                <div class="bar-row">
                    <div class="bar-label">${esc(d)}</div>
                    <div class="bar-track"><div class="bar-fill" style="width:${(c / deptMax) * 100}%"></div></div>
                    <div class="bar-value">${c}</div>
                </div>`).join('') : '<div class="empty-state">No department distribution data.</div>';
        }

        // Employment types chart
        const typeEntries = Object.entries(summary.byEmploymentType || {}).sort((a, b) => b[1] - a[1]);
        const typeMax = Math.max(...typeEntries.map(([, c]) => c), 1);
        const typeChart = $('#repTypeChart');
        if (typeChart) {
            typeChart.innerHTML = typeEntries.length ? typeEntries.map(([t, c]) => `
                <div class="bar-row">
                    <div class="bar-label">${esc(t)}</div>
                    <div class="bar-track"><div class="bar-fill" style="width:${(c / typeMax) * 100}%;background:linear-gradient(90deg,#16a34a,#22c55e);"></div></div>
                    <div class="bar-value">${c}</div>
                </div>`).join('') : '<div class="empty-state">No employment type data.</div>';
        }

        // Department salary budget chart
        const salEntries = Object.entries(summary.departmentExpense || {}).sort((a, b) => Number(b[1]) - Number(a[1]));
        const salMax = Math.max(...salEntries.map(([, s]) => Number(s)), 1);
        const salChart = $('#repSalaryDeptChart');
        if (salChart) {
            salChart.innerHTML = salEntries.length ? salEntries.map(([d, s]) => `
                <div class="bar-row">
                    <div class="bar-label">${esc(d)}</div>
                    <div class="bar-track"><div class="bar-fill" style="width:${(Number(s) / salMax) * 100}%;background:linear-gradient(90deg,#f59e0b,#fbbf24);"></div></div>
                    <div class="bar-value">${money(s)}</div>
                </div>`).join('') : '<div class="empty-state">No salary expenditure data.</div>';
        }

        // CSV export
        const exportBtn = $('#exportCsvBtn');
        if (exportBtn) {
            exportBtn.onclick = async () => {
                try {
                    const emps = await getEmployees();
                    const headers = ['ID', 'First Name', 'Last Name', 'Email', 'Phone', 'Department', 'Job Title', 'Employment Type', 'Joining Date', 'Annual Salary', 'Active Status'];
                    const rows = emps.map(e => [
                        e.id,
                        `"${(e.firstName || '').replace(/"/g, '""')}"`,
                        `"${(e.lastName || '').replace(/"/g, '""')}"`,
                        `"${(e.email || '').replace(/"/g, '""')}"`,
                        `"${(e.phone || '').replace(/"/g, '""')}"`,
                        `"${(e.department || '').replace(/"/g, '""')}"`,
                        `"${(e.jobTitle || '').replace(/"/g, '""')}"`,
                        `"${(e.employmentType || '').replace(/"/g, '""')}"`,
                        e.joiningDate || '',
                        e.salary || 0,
                        e.active ? 'Active' : 'Inactive'
                    ]);

                    const csvContent = 'data:text/csv;charset=utf-8,' + [headers.join(','), ...rows.map(r => r.join(','))].join('\n');
                    const encodedUri = encodeURI(csvContent);
                    const link = document.createElement('a');
                    link.setAttribute('href', encodedUri);
                    link.setAttribute('download', `hrms_workforce_report_${new Date().toISOString().split('T')[0]}.csv`);
                    document.body.appendChild(link);
                    link.click();
                    link.remove();
                    toast('Workforce report exported successfully!');
                } catch (e) { toast('Export failed: ' + e.message); }
            };
        }
    } catch (e) { toast(e.message); }
}

/* ==========================================================================
   ANNOUNCEMENTS
   ========================================================================== */
let allAnnouncements = [];

async function announcementsPage() {
    const feed = $('#announcementsFeed');
    const search = $('#announcementSearch');
    const priorityFilter = $('#announcementFilterPriority');
    const activeOnly = $('#activeOnlyCheckbox');

    const render = () => {
        const q = (search?.value || '').trim().toLowerCase();
        const prio = priorityFilter?.value || '';
        const act = activeOnly?.checked;

        const filtered = allAnnouncements.filter(a => {
            const matchQ = !q || a.title.toLowerCase().includes(q) || (a.content || '').toLowerCase().includes(q);
            const matchP = !prio || a.priority === prio;
            const matchA = !act || a.active;
            return matchQ && matchP && matchA;
        });

        if ($('#totalAnnouncements')) $('#totalAnnouncements').textContent = allAnnouncements.length;
        if ($('#activeAnnouncementsCount')) $('#activeAnnouncementsCount').textContent = allAnnouncements.filter(a => a.active).length;
        if ($('#pinnedCount')) $('#pinnedCount').textContent = allAnnouncements.filter(a => a.pinned).length;

        if (!feed) return;
        if (!filtered.length) {
            feed.innerHTML = '<div class="empty-state">No announcements match your search.</div>';
            return;
        }

        // Sort: Pinned first, then by date descending
        const sorted = [...filtered].sort((a, b) => (b.pinned ? 1 : 0) - (a.pinned ? 1 : 0));

        feed.innerHTML = sorted.map(a => {
            const prioClass = a.priority === 'CRITICAL' ? 'red' : a.priority === 'HIGH' ? 'orange' : 'blue';
            return `
                <article class="panel" style="padding:20px;border-radius:14px;border-left:4px solid var(--${prioClass === 'red' ? 'danger' : prioClass === 'orange' ? 'warning' : 'primary'});">
                    <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:10px;">
                        <div>
                            <div style="display:flex;align-items:center;gap:8px;margin-bottom:4px;">
                                ${a.pinned ? '<span title="Pinned announcement" style="font-size:16px;">📌</span>' : ''}
                                <h3 style="margin:0;font-size:16px;">${esc(a.title)}</h3>
                                <span class="badge ${prioClass}">${esc(a.priority)}</span>
                                ${!a.active ? '<span class="badge gray">Archived</span>' : ''}
                            </div>
                            <small style="color:var(--muted);">Posted by <b>${esc(a.author || 'HR Operations')}</b> • ${date(a.createdAt ? a.createdAt.split('T')[0] : '')}</small>
                        </div>
                        ${currentUser.role !== 'EMPLOYEE' ? `
                            <div style="display:flex;gap:6px;">
                                <button class="btn secondary" style="padding:4px 8px;font-size:11px;" onclick="editAnnouncement(${a.id})">✎ Edit</button>
                                ${currentUser.role === 'ADMIN' ? `<button class="btn secondary" style="padding:4px 8px;font-size:11px;color:var(--danger);" onclick="deleteAnnouncement(${a.id})">×</button>` : ''}
                            </div>
                        ` : ''}
                    </div>
                    <p style="margin:0;font-size:13px;line-height:1.6;color:var(--text);white-space:pre-wrap;">${esc(a.content)}</p>
                </article>
            `;
        }).join('');
    };

    try {
        allAnnouncements = await api('/api/announcements');
        render();
    } catch (e) { toast(e.message); }

    if (search) search.addEventListener('input', render);
    if (priorityFilter) priorityFilter.addEventListener('change', render);
    if (activeOnly) activeOnly.addEventListener('change', render);
}

function openNewAnnouncementModal() {
    $('#announcementId').value = '';
    $('#modalAnnouncementTitle').textContent = 'Post New Announcement';
    $('#announcementForm').reset();
    $('#formActive').checked = true;
    $('#announcementModal').style.display = 'flex';
}

function closeAnnouncementModal() {
    $('#announcementModal').style.display = 'none';
}

function editAnnouncement(id) {
    const a = allAnnouncements.find(x => x.id === id);
    if (!a) return;
    $('#announcementId').value = a.id;
    $('#modalAnnouncementTitle').textContent = 'Edit Announcement';
    $('#formTitle').value = a.title || '';
    $('#formPriority').value = a.priority || 'NORMAL';
    $('#formTargetDept').value = a.targetDepartment || '';
    $('#formContent').value = a.content || '';
    $('#formPinned').checked = !!a.pinned;
    $('#formActive').checked = a.active !== false;
    $('#announcementModal').style.display = 'flex';
}

async function handleAnnouncementSubmit(e) {
    e.preventDefault();
    const id = $('#announcementId').value;
    const body = {
        title: $('#formTitle').value.trim(),
        priority: $('#formPriority').value,
        targetDepartment: $('#formTargetDept').value.trim(),
        content: $('#formContent').value.trim(),
        pinned: $('#formPinned').checked,
        active: $('#formActive').checked
    };

    try {
        if (id) {
            await api(`/api/announcements/${id}`, { method: 'PUT', body: JSON.stringify(body) });
            toast('Announcement updated');
        } else {
            await api('/api/announcements', { method: 'POST', body: JSON.stringify(body) });
            toast('Announcement published');
        }
        closeAnnouncementModal();
        await announcementsPage();
    } catch (err) { toast(err.message); }
}

async function deleteAnnouncement(id) {
    if (!confirm('Are you sure you want to delete this announcement?')) return;
    try {
        await api(`/api/announcements/${id}`, { method: 'DELETE' });
        toast('Announcement deleted');
        await announcementsPage();
    } catch (e) { toast(e.message); }
}

/* ==========================================================================
   HOLIDAYS
   ========================================================================== */
let allHolidays = [];

async function holidaysPage() {
    const container = $('#holidayTableContainer');
    const search = $('#holidaySearch');
    const yearSelect = $('#holidayYearSelect');

    let currentPage = 1;
    const pageSize = 10;

    const render = () => {
        const q = (search?.value || '').trim().toLowerCase();
        const filtered = allHolidays.filter(h => !q || h.name.toLowerCase().includes(q) || (h.description || '').toLowerCase().includes(q));

        if ($('#totalHolidaysCount')) $('#totalHolidaysCount').textContent = allHolidays.length;

        const todayStr = new Date().toISOString().split('T')[0];
        const upcoming = allHolidays.filter(h => h.date >= todayStr).sort((a, b) => a.date.localeCompare(b.date));
        if ($('#upcomingHolidaysCount')) $('#upcomingHolidaysCount').textContent = upcoming.length;

        if (upcoming.length) {
            const next = upcoming[0];
            if ($('#nextHolidayName')) $('#nextHolidayName').textContent = next.name;
            if ($('#nextHolidayDate')) $('#nextHolidayDate').textContent = date(next.date);
            const daysAway = Math.ceil((new Date(next.date) - new Date(todayStr)) / (1000 * 60 * 60 * 24));
            if ($('#nextHolidayCountdown')) $('#nextHolidayCountdown').textContent = daysAway === 0 ? 'Today!' : `${daysAway} days`;
        }

        if (!container) return;
        if (!filtered.length) {
            container.innerHTML = '<div class="empty-state">No observed holidays recorded for this year.</div>';
            return;
        }

        const totalPages = Math.ceil(filtered.length / pageSize) || 1;
        if (currentPage > totalPages) currentPage = totalPages;
        const startIdx = (currentPage - 1) * pageSize;
        const pageItems = filtered.slice(startIdx, startIdx + pageSize);

        container.innerHTML = `<div class="table-wrap"><table class="employee-table">
            <thead>
                <tr>
                    <th>Date</th>
                    <th>Day</th>
                    <th>Holiday Name</th>
                    <th>Type</th>
                    <th>Details</th>
                    ${currentUser.role !== 'EMPLOYEE' ? '<th>Actions</th>' : ''}
                </tr>
            </thead>
            <tbody>
                ${pageItems.map(h => {
                    const d = new Date(h.date + 'T00:00:00');
                    const dayName = d.toLocaleDateString('en-US', { weekday: 'long' });
                    const isPast = h.date < todayStr;
                    return `
                        <tr style="${isPast ? 'opacity:0.6;' : ''}">
                            <td><b>${date(h.date)}</b></td>
                            <td>${dayName}</td>
                            <td><strong>${esc(h.name)}</strong></td>
                            <td><span class="badge ${h.type === 'PUBLIC' ? 'green' : 'orange'}">${esc(h.type || 'PUBLIC')}</span></td>
                            <td>${esc(h.description || '—')}</td>
                            ${currentUser.role !== 'EMPLOYEE' ? `
                                <td>
                                    <div style="display:flex;gap:6px;">
                                        <button class="btn secondary" style="padding:4px 8px;font-size:11px;" onclick="editHoliday(${h.id})">✎</button>
                                        ${currentUser.role === 'ADMIN' ? `<button class="btn secondary danger" style="padding:4px 8px;font-size:11px;" onclick="deleteHoliday(${h.id})">×</button>` : ''}
                                    </div>
                                </td>
                            ` : ''}
                        </tr>
                    `;
                }).join('')}
            </tbody>
        </table></div><div id="holidayPagination"></div>`;

        renderPaginationBar({
            container: $('#holidayPagination'),
            totalItems: filtered.length,
            page: currentPage,
            pageSize: pageSize,
            onPageChange: (newPage) => {
                currentPage = newPage;
                render();
            }
        });
    };

    try {
        const yr = yearSelect?.value || new Date().getFullYear();
        allHolidays = await api(`/api/holidays?year=${yr}`);
        allHolidays.sort((a, b) => a.date.localeCompare(b.date));
        render();
    } catch (e) { toast(e.message); }

    if (search) search.addEventListener('input', () => { currentPage = 1; render(); });
    if (yearSelect) yearSelect.addEventListener('change', () => holidaysPage());
}

function openNewHolidayModal() {
    $('#holidayId').value = '';
    $('#modalHolidayTitle').textContent = 'Add Holiday';
    $('#holidayForm').reset();
    $('#holidayModal').style.display = 'flex';
}

function closeHolidayModal() {
    $('#holidayModal').style.display = 'none';
}

function editHoliday(id) {
    const h = allHolidays.find(x => x.id === id);
    if (!h) return;
    $('#holidayId').value = h.id;
    $('#modalHolidayTitle').textContent = 'Edit Holiday';
    $('#formHolidayName').value = h.name || '';
    $('#formHolidayDate').value = h.date || '';
    $('#formHolidayType').value = h.type || 'PUBLIC';
    $('#formHolidayDesc').value = h.description || '';
    $('#holidayModal').style.display = 'flex';
}

async function handleHolidaySubmit(e) {
    e.preventDefault();
    const id = $('#holidayId').value;
    const body = {
        name: $('#formHolidayName').value.trim(),
        date: $('#formHolidayDate').value,
        type: $('#formHolidayType').value,
        description: $('#formHolidayDesc').value.trim()
    };

    try {
        if (id) {
            await api(`/api/holidays/${id}`, { method: 'PUT', body: JSON.stringify(body) });
            toast('Holiday updated');
        } else {
            await api('/api/holidays', { method: 'POST', body: JSON.stringify(body) });
            toast('Holiday added');
        }
        closeHolidayModal();
        await holidaysPage();
    } catch (err) { toast(err.message); }
}

async function deleteHoliday(id) {
    if (!confirm('Are you sure you want to delete this holiday?')) return;
    try {
        await api(`/api/holidays/${id}`, { method: 'DELETE' });
        toast('Holiday removed');
        await holidaysPage();
    } catch (e) { toast(e.message); }
}

/* ==========================================================================
   PERFORMANCE REVIEWS
   ========================================================================== */
let allReviews = [];

async function performancePage() {
    const container = $('#reviewTableContainer');
    const search = $('#reviewSearch');
    const statusFilter = $('#reviewStatusFilter');

    let currentPage = 1;
    const pageSize = 8;

    const render = () => {
        const q = (search?.value || '').trim().toLowerCase();
        const stat = statusFilter?.value || '';

        const filtered = allReviews.filter(r => {
            const matchQ = !q || (r.employeeName || '').toLowerCase().includes(q) || (r.reviewPeriod || '').toLowerCase().includes(q);
            const matchS = !stat || r.status === stat;
            return matchQ && matchS;
        });

        if ($('#totalReviewsCount')) $('#totalReviewsCount').textContent = allReviews.length;
        if ($('#completedReviewsCount')) $('#completedReviewsCount').textContent = allReviews.filter(r => r.status === 'ACKNOWLEDGED').length;
        if ($('#pendingReviewsCount')) $('#pendingReviewsCount').textContent = allReviews.filter(r => r.status === 'SUBMITTED').length;

        const ratings = allReviews.map(r => Number(r.rating || 0)).filter(n => n > 0);
        const avg = ratings.length ? (ratings.reduce((a, b) => a + b, 0) / ratings.length).toFixed(1) : '—';
        if ($('#avgRatingScore')) $('#avgRatingScore').textContent = avg;

        if (!container) return;
        if (!filtered.length) {
            container.innerHTML = '<div class="empty-state">No performance appraisals match your search.</div>';
            return;
        }

        const totalPages = Math.ceil(filtered.length / pageSize) || 1;
        if (currentPage > totalPages) currentPage = totalPages;
        const startIdx = (currentPage - 1) * pageSize;
        const pageItems = filtered.slice(startIdx, startIdx + pageSize);

        container.innerHTML = `<div class="table-wrap"><table class="employee-table">
            <thead>
                <tr>
                    <th>Employee</th>
                    <th>Review Period</th>
                    <th>Score / Rating</th>
                    <th>Status</th>
                    <th>Reviewer</th>
                    <th>Feedback Summary</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                ${pageItems.map(r => {
                    const stars = '★'.repeat(Math.round(r.rating || 0)) + '☆'.repeat(Math.max(0, 5 - Math.round(r.rating || 0)));
                    return `
                        <tr>
                            <td><strong>${esc(r.employeeName)}</strong></td>
                            <td><span class="badge blue">${esc(r.reviewPeriod)}</span></td>
                            <td><b style="color:var(--warning);font-size:14px;">${stars}</b> (${r.rating || '—'})</td>
                            <td><span class="badge ${r.status === 'ACKNOWLEDGED' ? 'green' : r.status === 'SUBMITTED' ? 'purple' : 'gray'}">${esc(r.status || 'DRAFT')}</span></td>
                            <td>${esc(r.reviewerName || 'Supervisor')}</td>
                            <td><small style="color:var(--muted);">${esc(r.managerComments || '—')}</small></td>
                            <td>
                                <div style="display:flex;gap:6px;">
                                    ${currentUser.role !== 'EMPLOYEE' && r.status === 'DRAFT' ? `<button class="btn primary" style="padding:4px 8px;font-size:11px;" onclick="submitReview(${r.id})">Submit</button>` : ''}
                                    ${r.status === 'SUBMITTED' ? `<button class="btn secondary" style="padding:4px 8px;font-size:11px;color:var(--success);" onclick="acknowledgeReview(${r.id})">Acknowledge</button>` : ''}
                                    ${currentUser.role === 'ADMIN' ? `<button class="btn secondary danger" style="padding:4px 8px;font-size:11px;" onclick="deleteReview(${r.id})">×</button>` : ''}
                                </div>
                            </td>
                        </tr>
                    `;
                }).join('')}
            </tbody>
        </table></div><div id="reviewPagination"></div>`;

        renderPaginationBar({
            container: $('#reviewPagination'),
            totalItems: filtered.length,
            page: currentPage,
            pageSize: pageSize,
            onPageChange: (newPage) => {
                currentPage = newPage;
                render();
            }
        });
    };

    try {
        allReviews = await api('/api/performance-reviews');
        render();
    } catch (e) { toast(e.message); }

    if (search) search.addEventListener('input', () => { currentPage = 1; render(); });
    if (statusFilter) statusFilter.addEventListener('change', () => { currentPage = 1; render(); });
}

async function openNewReviewModal() {
    try {
        const emps = await getEmployees();
        const sel = $('#formReviewEmployee');
        if (sel) {
            sel.innerHTML = '<option value="">Select Employee</option>' + emps.map(e => `
                <option value="${e.id}" data-name="${esc(e.firstName + ' ' + e.lastName)}">${esc(e.firstName + ' ' + e.lastName)} (${esc(e.department || 'General')})</option>
            `).join('');
        }
        $('#reviewId').value = '';
        $('#modalReviewTitle').textContent = 'Create Performance Review';
        $('#reviewForm').reset();
        $('#reviewModal').style.display = 'flex';
    } catch (e) { toast(e.message); }
}

function closeReviewModal() {
    $('#reviewModal').style.display = 'none';
}

async function handleReviewSubmit(e) {
    e.preventDefault();
    const sel = $('#formReviewEmployee');
    const opt = sel.options[sel.selectedIndex];
    const empId = Number(sel.value);
    const empName = opt ? opt.dataset.name : '';

    const body = {
        employeeId: empId,
        employeeName: empName,
        reviewPeriod: $('#formReviewPeriod').value.trim(),
        rating: Number($('#formReviewRating').value),
        reviewerName: $('#formReviewerName').value.trim(),
        goals: $('#formReviewGoals').value.trim(),
        managerComments: $('#formReviewComments').value.trim(),
        status: 'DRAFT'
    };

    try {
        await api('/api/performance-reviews', { method: 'POST', body: JSON.stringify(body) });
        closeReviewModal();
        toast('Performance appraisal created');
        await performancePage();
    } catch (err) { toast(err.message); }
}

async function submitReview(id) {
    try {
        await api(`/api/performance-reviews/${id}/submit`, { method: 'POST' });
        toast('Review submitted for employee acknowledgement');
        await performancePage();
    } catch (e) { toast(e.message); }
}

async function acknowledgeReview(id) {
    try {
        await api(`/api/performance-reviews/${id}/acknowledge`, { method: 'POST' });
        toast('Review acknowledged successfully');
        await performancePage();
    } catch (e) { toast(e.message); }
}

async function deleteReview(id) {
    if (!confirm('Are you sure you want to delete this appraisal?')) return;
    try {
        await api(`/api/performance-reviews/${id}`, { method: 'DELETE' });
        toast('Review deleted');
        await performancePage();
    } catch (e) { toast(e.message); }
}

/* ==========================================================================
   USER MANAGEMENT (ADMIN)
   ========================================================================== */
let allUsers = [];

async function userManagementPage() {
    const container = $('#userTableContainer');
    const search = $('#userSearch');
    const roleFilter = $('#userRoleFilter');

    let currentPage = 1;
    const pageSize = 10;

    const render = () => {
        const q = (search?.value || '').trim().toLowerCase();
        const role = roleFilter?.value || '';

        const filtered = allUsers.filter(u => {
            const matchQ = !q || u.username.toLowerCase().includes(q) || (u.role || '').toLowerCase().includes(q);
            const matchR = !role || u.role === role;
            return matchQ && matchR;
        });

        if ($('#totalUsersCount')) $('#totalUsersCount').textContent = allUsers.length;
        if ($('#adminCount')) $('#adminCount').textContent = allUsers.filter(u => u.role === 'ROLE_ADMIN' || u.role === 'ADMIN').length;
        if ($('#hrCount')) $('#hrCount').textContent = allUsers.filter(u => u.role === 'ROLE_HR' || u.role === 'HR').length;
        if ($('#employeeUserCount')) $('#employeeUserCount').textContent = allUsers.filter(u => u.role === 'ROLE_EMPLOYEE' || u.role === 'EMPLOYEE').length;

        if (!container) return;
        if (!filtered.length) {
            container.innerHTML = '<div class="empty-state">No users match your search.</div>';
            return;
        }

        const totalPages = Math.ceil(filtered.length / pageSize) || 1;
        if (currentPage > totalPages) currentPage = totalPages;
        const startIdx = (currentPage - 1) * pageSize;
        const pageItems = filtered.slice(startIdx, startIdx + pageSize);

        container.innerHTML = `<div class="table-wrap"><table class="employee-table">
            <thead>
                <tr>
                    <th>Username</th>
                    <th>System Role</th>
                    <th>Linked Employee ID</th>
                    <th>Account Status</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                ${pageItems.map(u => {
                    const roleLabel = u.role.replace('ROLE_', '');
                    const roleBadge = roleLabel === 'ADMIN' ? 'red' : roleLabel === 'HR' ? 'purple' : 'blue';
                    return `
                        <tr>
                            <td><strong>${esc(u.username)}</strong></td>
                            <td><span class="badge ${roleBadge}">${esc(roleLabel)}</span></td>
                            <td>${u.employeeId ? `#EMP-${u.employeeId}` : '<span style="color:var(--muted)">Standalone</span>'}</td>
                            <td><span class="badge ${u.enabled ? 'green' : 'gray'}">${u.enabled ? 'Active' : 'Disabled'}</span></td>
                            <td>
                                <div style="display:flex;gap:6px;">
                                    <button class="btn secondary" style="padding:4px 8px;font-size:11px;" onclick="openResetPasswordModal(${u.id}, '${esc(u.username)}')">🔑 Reset</button>
                                    <button class="btn secondary danger" style="padding:4px 8px;font-size:11px;" onclick="deleteUser(${u.id})">×</button>
                                </div>
                            </td>
                        </tr>
                    `;
                }).join('')}
            </tbody>
        </table></div><div id="userPagination"></div>`;

        renderPaginationBar({
            container: $('#userPagination'),
            totalItems: filtered.length,
            page: currentPage,
            pageSize: pageSize,
            onPageChange: (newPage) => {
                currentPage = newPage;
                render();
            }
        });
    };

    try {
        allUsers = await api('/api/users');
        render();
    } catch (e) { toast(e.message); }

    if (search) search.addEventListener('input', () => { currentPage = 1; render(); });
    if (roleFilter) roleFilter.addEventListener('change', () => { currentPage = 1; render(); });
}

async function openCreateUserModal() {
    try {
        const emps = await getEmployees();
        const sel = $('#createEmployeeId');
        if (sel) {
            sel.innerHTML = '<option value="">No linked employee (Standalone Admin/HR)</option>' + emps.map(e => `
                <option value="${e.id}">${esc(e.firstName + ' ' + e.lastName)} (#${e.id} • ${esc(e.department || 'General')})</option>
            `).join('');
        }
        $('#createUserForm').reset();
        $('#createUserModal').style.display = 'flex';
    } catch (e) { toast(e.message); }
}

function closeCreateUserModal() {
    $('#createUserModal').style.display = 'none';
}

async function handleCreateUserSubmit(e) {
    e.preventDefault();
    const empVal = $('#createEmployeeId').value;
    const body = {
        username: $('#createUsername').value.trim(),
        password: $('#createPassword').value,
        role: $('#createRole').value,
        employeeId: empVal ? Number(empVal) : null
    };

    try {
        await api('/api/users', { method: 'POST', body: JSON.stringify(body) });
        closeCreateUserModal();
        toast('User account created successfully');
        await userManagementPage();
    } catch (err) { toast(err.message); }
}

function openResetPasswordModal(id, username) {
    $('#resetUserId').value = id;
    $('#resetUsernameDisplay').textContent = `Resetting password for: ${username}`;
    $('#resetPasswordForm').reset();
    $('#resetPasswordModal').style.display = 'flex';
}

function closeResetPasswordModal() {
    $('#resetPasswordModal').style.display = 'none';
}

async function handleResetPasswordSubmit(e) {
    e.preventDefault();
    const id = $('#resetUserId').value;
    const pwd = $('#resetNewPassword').value;

    try {
        await api(`/api/users/${id}/reset-password`, {
            method: 'POST',
            body: JSON.stringify({ password: pwd })
        });
        closeResetPasswordModal();
        toast('User password updated successfully');
    } catch (err) { toast(err.message); }
}

async function deleteUser(id) {
    if (!confirm('Are you sure you want to delete this user account?')) return;
    try {
        await api(`/api/users/${id}`, { method: 'DELETE' });
        toast('User deleted');
        await userManagementPage();
    } catch (e) { toast(e.message); }
}

/* ==========================================================================
   AUDIT LOG TRAIL
   ========================================================================== */
let allAuditLogs = [];

async function auditLogPage() {
    const container = $('#auditTableContainer');
    const search = $('#auditSearch');
    const entityFilter = $('#auditEntityFilter');
    const actionFilter = $('#auditActionFilter');

    let currentPage = 1;
    const pageSize = 12;

    const render = () => {
        const q = (search?.value || '').trim().toLowerCase();
        const ent = entityFilter?.value || '';
        const act = actionFilter?.value || '';

        const filtered = allAuditLogs.filter(l => {
            const matchQ = !q || (l.description || '').toLowerCase().includes(q) || (l.performedBy || '').toLowerCase().includes(q);
            const matchE = !ent || l.entityType === ent;
            const matchA = !act || l.action === act;
            return matchQ && matchE && matchA;
        });

        if ($('#totalAuditEvents')) $('#totalAuditEvents').textContent = allAuditLogs.length;
        if ($('#auditCreatesCount')) $('#auditCreatesCount').textContent = allAuditLogs.filter(l => l.action === 'CREATE').length;
        if ($('#auditUpdatesCount')) $('#auditUpdatesCount').textContent = allAuditLogs.filter(l => l.action === 'UPDATE').length;
        const users = new Set(allAuditLogs.map(l => l.performedBy).filter(Boolean));
        if ($('#auditOperatorsCount')) $('#auditOperatorsCount').textContent = users.size;

        if (!container) return;
        if (!filtered.length) {
            container.innerHTML = '<div class="empty-state">No audit log events match your filters.</div>';
            return;
        }

        const totalPages = Math.ceil(filtered.length / pageSize) || 1;
        if (currentPage > totalPages) currentPage = totalPages;
        const startIdx = (currentPage - 1) * pageSize;
        const pageItems = filtered.slice(startIdx, startIdx + pageSize);

        container.innerHTML = `<div class="table-wrap"><table class="employee-table">
            <thead>
                <tr>
                    <th>Timestamp</th>
                    <th>Action</th>
                    <th>Entity</th>
                    <th>Entity ID</th>
                    <th>Description</th>
                    <th>Actor</th>
                </tr>
            </thead>
            <tbody>
                ${pageItems.map(l => {
                    const actBadge = l.action === 'CREATE' ? 'green' : l.action === 'DELETE' ? 'red' : l.action === 'UPDATE' ? 'blue' : 'purple';
                    return `
                        <tr>
                            <td><small style="color:var(--muted)">${l.performedAt ? l.performedAt.replace('T', ' ').substring(0, 19) : '—'}</small></td>
                            <td><span class="badge ${actBadge}">${esc(l.action)}</span></td>
                            <td><b>${esc(l.entityType)}</b></td>
                            <td>${l.entityId ? `#${l.entityId}` : '—'}</td>
                            <td>${esc(l.description)}</td>
                            <td><span class="badge gray">@${esc(l.performedBy || 'system')}</span></td>
                        </tr>
                    `;
                }).join('')}
            </tbody>
        </table></div><div id="auditPagination"></div>`;

        renderPaginationBar({
            container: $('#auditPagination'),
            totalItems: filtered.length,
            page: currentPage,
            pageSize: pageSize,
            onPageChange: (newPage) => {
                currentPage = newPage;
                render();
            }
        });
    };

    try {
        allAuditLogs = await api('/api/audit-logs');
        render();
    } catch (e) { toast(e.message); }

    if (search) search.addEventListener('input', () => { currentPage = 1; render(); });
    if (entityFilter) entityFilter.addEventListener('change', () => { currentPage = 1; render(); });
    if (actionFilter) actionFilter.addEventListener('change', () => { currentPage = 1; render(); });
}

function loadAuditLogs() {
    auditLogPage();
}

/* ==========================================================================
   SETTINGS & SECURITY
   ========================================================================== */
function settingsPage() {
    if ($('#profileUsernameVal')) $('#profileUsernameVal').textContent = currentUser.username || 'User';
    if ($('#profileRoleBadge')) $('#profileRoleBadge').textContent = (currentUser.role || 'EMPLOYEE').replace('ROLE_', '');
    if ($('#profileEmpIdVal')) $('#profileEmpIdVal').textContent = currentUser.employeeId ? `#EMP-${currentUser.employeeId}` : 'N/A';
    if ($('#logoutCsrf')) $('#logoutCsrf').value = csrfToken();
}

async function handleChangePasswordSubmit(e) {
    e.preventDefault();
    const curr = $('#settingsCurrentPassword').value;
    const next = $('#settingsNewPassword').value;
    const conf = $('#settingsConfirmPassword').value;
    const alertBox = $('#passwordAlert');

    if (next !== conf) {
        if (alertBox) {
            alertBox.style.display = 'block';
            alertBox.style.background = 'rgba(239, 68, 68, 0.1)';
            alertBox.style.color = '#ef4444';
            alertBox.textContent = 'New passwords do not match.';
        }
        return;
    }

    try {
        await api('/api/settings/change-password', {
            method: 'POST',
            body: JSON.stringify({ currentPassword: curr, newPassword: next })
        });
        if (alertBox) {
            alertBox.style.display = 'block';
            alertBox.style.background = 'rgba(16, 185, 129, 0.1)';
            alertBox.style.color = '#10b981';
            alertBox.textContent = 'Password changed successfully!';
        }
        $('#changePasswordForm').reset();
        toast('Password updated!');
    } catch (err) {
        if (alertBox) {
            alertBox.style.display = 'block';
            alertBox.style.background = 'rgba(239, 68, 68, 0.1)';
            alertBox.style.color = '#ef4444';
            alertBox.textContent = err.message;
        }
    }
}

/* ==========================================================================
   GLOBAL SEARCH IN TOPBAR
   ========================================================================== */
function setupGlobalSearch() {
    const input = $('#globalSearchInput');
    const results = $('#globalSearchResults');
    if (!input || !results) return;

    let timeout;
    input.addEventListener('input', () => {
        clearTimeout(timeout);
        const q = input.value.trim();
        if (q.length < 2) {
            results.style.display = 'none';
            return;
        }

        timeout = setTimeout(async () => {
            try {
                const res = await api(`/api/search?q=${encodeURIComponent(q)}`);
                const emps = res.employees || [];
                const depts = res.departments || [];
                const ann = res.announcements || [];

                if (!emps.length && !depts.length && !ann.length) {
                    results.innerHTML = '<div style="font-size:12px;color:var(--muted);padding:8px;">No matching results found.</div>';
                    results.style.display = 'block';
                    return;
                }

                let html = '';
                if (emps.length) {
                    html += '<div style="font-size:10px;text-transform:uppercase;color:var(--muted);font-weight:700;margin-bottom:6px;">Employees</div>';
                    emps.forEach(e => {
                        html += `
                            <a href="/employee-profile?id=${e.id}" style="display:flex;align-items:center;gap:8px;padding:6px 8px;border-radius:6px;text-decoration:none;color:var(--text);font-size:13px;" class="search-item">
                                <span style="font-size:14px;">👤</span>
                                <div><b>${esc(e.firstName)} ${esc(e.lastName)}</b> <small style="color:var(--muted)">(${esc(e.department || '—')})</small></div>
                            </a>
                        `;
                    });
                }
                if (depts.length) {
                    html += '<div style="font-size:10px;text-transform:uppercase;color:var(--muted);font-weight:700;margin:8px 0 6px;">Departments</div>';
                    depts.forEach(d => {
                        html += `
                            <a href="/departments" style="display:flex;align-items:center;gap:8px;padding:6px 8px;border-radius:6px;text-decoration:none;color:var(--text);font-size:13px;" class="search-item">
                                <span style="font-size:14px;">▣</span>
                                <div><b>${esc(d.name)}</b> <small style="color:var(--muted)">${esc(d.headOfDepartment || '')}</small></div>
                            </a>
                        `;
                    });
                }
                if (ann.length) {
                    html += '<div style="font-size:10px;text-transform:uppercase;color:var(--muted);font-weight:700;margin:8px 0 6px;">Announcements</div>';
                    ann.forEach(a => {
                        html += `
                            <a href="/announcements" style="display:flex;align-items:center;gap:8px;padding:6px 8px;border-radius:6px;text-decoration:none;color:var(--text);font-size:13px;" class="search-item">
                                <span style="font-size:14px;">📢</span>
                                <div><b>${esc(a.title)}</b></div>
                            </a>
                        `;
                    });
                }

                results.innerHTML = html;
                results.style.display = 'block';
            } catch (e) { console.error('Global search error', e); }
        }, 300);
    });

    document.addEventListener('click', (ev) => {
        if (!input.contains(ev.target) && !results.contains(ev.target)) {
            results.style.display = 'none';
        }
    });
}

/* ==========================================================================
   INITIALIZATION
   ========================================================================== */
document.addEventListener('DOMContentLoaded', async () => {
    setupTheme();
    await setupUser();
    setupGlobalSearch();

    const p = document.body.dataset.page;
    if (p === 'dashboard') dashboard();
    if (p === 'employees') employeesPage();
    if (p === 'form') formPage();
    if (p === 'profile') profilePage();
    if (p === 'departments') { departmentsPage(); bindDepartmentForm(); }
    if (p === 'attendance') attendancePage();
    if (p === 'leave') leavePage();
    if (p === 'payroll') payrollPage();
    if (p === 'reports') reportsPage();
    if (p === 'announcements') announcementsPage();
    if (p === 'holidays') holidaysPage();
    if (p === 'performance') performancePage();
    if (p === 'user-management') userManagementPage();
    if (p === 'audit-log') auditLogPage();
    if (p === 'settings') settingsPage();
});

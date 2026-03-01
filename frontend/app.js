const API = '';
const LS = 'taskgen_v1';

let pollTimer = null;
let elapsedTimer = null;
let currentTaskId = null;
let tasksData = [];
let deletedIds = new Set();
let lastDeletedId = null;
let undoTimer = null;
let retryFn = null;
let pollPaused = false;
let elapsedSec = 0;
let autosaveTimer = null;
let confirmed = false;

const $ = id => document.getElementById(id);
const show = id => $(id).classList.remove('hidden');
const hide = id => $(id).classList.add('hidden');

/* ── localStorage ── */
function saveState() {
    const state = {
        screen: currentScreen(),
        taskId: currentTaskId,
        inputText: $('input-text').value,
        tasksData,
        deletedIds: [...deletedIds],
        projectName: $('project-name').textContent,
        confirmed,
        cardEdits: currentTaskId && screen('tasks-section') ? collectAllEdits() : null
    };
    try { localStorage.setItem(LS, JSON.stringify(state)); } catch(e) {}
}

function loadState() {
    try {
        const raw = localStorage.getItem(LS);
        if (!raw) return null;
        return JSON.parse(raw);
    } catch(e) { return null; }
}

function clearStorage() {
    if (!confirm('Clear all saved data and start over?')) return;
    localStorage.removeItem(LS);
    hardReset();
    toast('data cleared', 'warn');
}

function currentScreen() {
    for (const s of ['generate-section','status-section','error-section','tasks-section']) {
        if (!$(s).classList.contains('hidden')) return s;
    }
    return 'generate-section';
}

function saveDraft() {
    try { localStorage.setItem(LS + '_draft', $('input-text').value); } catch(e) {}
}

function loadDraft() {
    try { return localStorage.getItem(LS + '_draft') || ''; } catch(e) { return ''; }
}

/* ── restore on load ── */
window.addEventListener('load', () => {
    const state = loadState();

    if (!state) {
        const draft = loadDraft();
        if (draft) {
            $('input-text').value = draft;
            show('draft-hint');
        }
        return;
    }

    if (state.screen === 'tasks-section' && state.tasksData?.length) {
        currentTaskId = state.taskId;
        tasksData = state.tasksData;
        deletedIds = new Set(state.deletedIds || []);
        confirmed = state.confirmed || false;
        $('project-name').textContent = state.projectName || '';
        $('task-count').textContent = ` (${tasksData.length - deletedIds.size} tasks)`;
        $('input-text').value = state.inputText || '';
        hideAll();
        show('tasks-section');
        renderList(state.cardEdits);
        if (confirmed) markConfirmed();
        toast('session restored', 'ok');
        return;
    }

    if (state.screen === 'status-section' && state.taskId) {
        currentTaskId = state.taskId;
        $('input-text').value = state.inputText || '';
        hideAll();
        show('status-section');
        $('status-text').textContent = 'resuming poll...';
        startElapsed();
        startPoll();
        toast('polling resumed', 'warn');
        return;
    }

    const draft = state.inputText || loadDraft();
    if (draft) {
        $('input-text').value = draft;
        show('draft-hint');
    }
});

/* ── online/offline ── */
window.addEventListener('online', () => {
    hide('offline-badge');
    if (pollPaused) resumePoll();
});
window.addEventListener('offline', () => {
    show('offline-badge');
});

/* ── toast ── */
function toast(msg, type = '') {
    const el = document.createElement('div');
    el.className = 'toast' + (type ? ' toast-' + type : '');
    el.textContent = msg;
    $('toast-container').appendChild(el);
    requestAnimationFrame(() => { requestAnimationFrame(() => { el.classList.add('show'); }); });
    setTimeout(() => {
        el.classList.remove('show');
        setTimeout(() => el.remove(), 250);
    }, 3000);
}

/* ── navigation ── */
function hideAll() {
    ['generate-section','status-section','error-section','tasks-section'].forEach(hide);
}

function showError(msg, canRetry) {
    clearTimers();
    hideAll();
    $('error-msg').textContent = msg;
    if (canRetry && retryFn) show('btn-retry');
    else hide('btn-retry');
    show('error-section');
    saveState();
}

function retryAction() {
    if (retryFn) {
        hide('error-section');
        retryFn();
    }
}

function goBack() {
    retryFn = null;
    if (tasksData.length && currentTaskId) {
        hideAll();
        show('tasks-section');
        saveState();
    } else {
        hardReset();
    }
}

function hardReset() {
    clearTimers();
    currentTaskId = null;
    tasksData = [];
    deletedIds = new Set();
    lastDeletedId = null;
    retryFn = null;
    confirmed = false;
    $('input-text').value = '';
    $('project-name').textContent = '';
    $('task-count').textContent = '';
    $('btn-confirm').disabled = false;
    $('btn-confirm').textContent = '[ confirm ]';
    $('tasks-list').innerHTML = '';
    hide('undo-bar');
    hideAll();
    show('generate-section');
}

function confirmReset() {
    if (!confirm('Start a new generation? Current tasks will remain in memory until you generate a new one.')) return;
    clearTimers();
    currentTaskId = null;
    confirmed = false;
    $('btn-confirm').disabled = false;
    $('btn-confirm').textContent = '[ confirm ]';
    $('input-text').value = '';
    $('tasks-list').innerHTML = '';
    hideAll();
    show('generate-section');
    localStorage.removeItem(LS);
    toast('ready for new generation');
}

function clearTimers() {
    clearInterval(pollTimer);
    clearInterval(elapsedTimer);
    clearTimeout(autosaveTimer);
    pollTimer = null;
    elapsedTimer = null;
}

/* ── generate ── */
async function generate() {
    const text = $('input-text').value.trim();
    if (!text) { toast('enter project description', 'warn'); return; }

    $('btn-generate').disabled = true;
    retryFn = generate;

    try {
        const res = await fetch(`${API}/v1/api/generation/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ text })
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        currentTaskId = data.taskId;
        tasksData = [];
        deletedIds = new Set();
        confirmed = false;
        hideAll();
        show('status-section');
        $('status-text').textContent = 'waiting for generation...';
        hide('btn-resume');
        elapsedSec = 0;
        $('progress-fill').style.width = '5%';
        startElapsed();
        startPoll();
        saveState();
    } catch (e) {
        $('btn-generate').disabled = false;
        showError(`generation request failed: ${e.message}`, true);
    }
}

/* ── polling ── */
function startPoll() {
    pollPaused = false;
    hide('btn-resume');
    if (pollTimer) clearInterval(pollTimer);
    pollTimer = setInterval(poll, 2500);
}

function resumePoll() {
    hideAll();
    show('status-section');
    $('status-text').textContent = 'resuming...';
    startPoll();
    toast('polling resumed');
}

function cancelGeneration() {
    clearTimers();
    goBack();
    toast('generation cancelled', 'warn');
}

function startElapsed() {
    if (elapsedTimer) clearInterval(elapsedTimer);
    const start = Date.now() - (elapsedSec * 1000);
    elapsedTimer = setInterval(() => {
        elapsedSec = Math.floor((Date.now() - start) / 1000);
        $('elapsed-time').textContent = `${elapsedSec}s`;
        const pct = Math.min(5 + elapsedSec * 1.5, 90);
        $('progress-fill').style.width = pct + '%';
    }, 1000);
}

async function poll() {
    if (pollPaused || !currentTaskId) return;
    if (!navigator.onLine) {
        pollPaused = true;
        $('status-text').textContent = 'offline — waiting for connection...';
        show('btn-resume');
        return;
    }

    try {
        const res = await fetch(`${API}/v1/api/generation/${currentTaskId}`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();

        if (data.status === 'ACTIVE') {
            saveState();
            return;
        }
        clearInterval(pollTimer);
        clearInterval(elapsedTimer);
        pollTimer = null;

        if (data.status === 'FAILED') {
            retryFn = null;
            showError('generation failed on server side', false);
            return;
        }
        if (data.status === 'COMPLETE') {
            $('progress-fill').style.width = '100%';
            setTimeout(() => renderTasks(data.generatedTasks), 300);
        }
    } catch (e) {
        clearInterval(pollTimer);
        pollPaused = true;
        show('btn-resume');
        $('status-text').textContent = 'network error — ';
        retryFn = resumePoll;
        toast(`poll error: ${e.message}`, 'err');
    }
}

/* ── render tasks ── */
function renderTasks(data) {
    tasksData = data.tasks.map((t, i) => ({ ...t, _id: i }));
    $('project-name').textContent = data.projectName || '';
    $('task-count').textContent = ` (${tasksData.length} tasks)`;
    hideAll();
    show('tasks-section');
    renderList();
    saveState();
    toast('tasks ready', 'ok');
}

function renderList(edits) {
    const list = $('tasks-list');
    list.innerHTML = '';
    tasksData.forEach(task => {
        if (deletedIds.has(task._id)) return;
        const edit = edits ? edits[task._id] : null;
        list.appendChild(makeCard(edit ? { ...task, ...edit } : task));
    });
    updateTaskCount();
}

function updateTaskCount() {
    const visible = tasksData.length - deletedIds.size;
    $('task-count').textContent = ` (${visible} task${visible !== 1 ? 's' : ''})`;
}

/* ── card ── */
function makeCard(task) {
    const card = document.createElement('div');
    card.className = 'task-card';
    card.dataset.id = task._id;

    const comments = (task.comments || []).join('\n');
    const tagsHtml = (task.tags || []).map(t => tagChip(t)).join('');

    card.innerHTML = `
    <div class="field-label">name</div>
    <input type="text" class="f-name" value="${esc(task.name)}">
    <div class="field-label">description</div>
    <textarea class="f-desc" rows="3">${esc(task.description)}</textarea>
    <div class="field-label">comments</div>
    <textarea class="f-comments" rows="2">${esc(comments)}</textarea>
    <div class="field-label">tags</div>
    <div class="tag-list">${tagsHtml}</div>
    <div class="tag-input-row">
      <input type="text" placeholder="add tag..." class="tag-inp">
      <button onclick="addTag(this)">[ + ]</button>
    </div>
    <div class="task-actions">
      <button class="btn-delete" onclick="deleteCard(this)">[ delete ]</button>
    </div>`;

    const scheduleCardSave = () => scheduleAutosave(card);
    card.querySelectorAll('.f-name,.f-desc,.f-comments').forEach(el => {
        el.addEventListener('input', () => { markModified(card); scheduleCardSave(); });
    });

    const tagInp = card.querySelector('.tag-inp');
    tagInp.addEventListener('keydown', e => { if (e.key === 'Enter') { e.preventDefault(); addTag(tagInp.nextElementSibling); } });

    return card;
}

function markModified(card) {
    card.classList.add('modified');
}

function tagChip(t) {
    return `<span class="tag">${esc(t)}<button class="remove-tag" onclick="this.parentElement.remove();scheduleAutosave(this.closest('.task-card'))">×</button></span>`;
}

function esc(s) {
    return (s || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function addTag(btn) {
    const card = btn.closest('.task-card');
    const inp = card.querySelector('.tag-inp');
    const val = inp.value.trim();
    if (!val) return;
    const span = document.createElement('span');
    span.className = 'tag';
    span.innerHTML = `${esc(val)}<button class="remove-tag" onclick="this.parentElement.remove();scheduleAutosave(this.closest('.task-card'))">×</button>`;
    card.querySelector('.tag-list').appendChild(span);
    inp.value = '';
    markModified(card);
    scheduleAutosave(card);
}

function deleteCard(btn) {
    const card = btn.closest('.task-card');
    const id = parseInt(card.dataset.id);
    deletedIds.add(id);
    lastDeletedId = id;
    card.remove();
    updateTaskCount();
    show('undo-bar');
    if (undoTimer) clearTimeout(undoTimer);
    undoTimer = setTimeout(() => { hide('undo-bar'); lastDeletedId = null; }, 8000);
    scheduleAutosave();
}

function undoDelete() {
    if (lastDeletedId === null) return;
    deletedIds.delete(lastDeletedId);
    const task = tasksData.find(t => t._id === lastDeletedId);
    lastDeletedId = null;
    hide('undo-bar');
    if (task) {
        const list = $('tasks-list');
        const insertBefore = [...list.querySelectorAll('.task-card')].find(c => parseInt(c.dataset.id) > task._id);
        const newCard = makeCard(task);
        if (insertBefore) list.insertBefore(newCard, insertBefore);
        else list.appendChild(newCard);
        updateTaskCount();
        scheduleAutosave();
        toast('task restored');
    }
}

/* ── autosave ── */
function scheduleAutosave() {
    if (autosaveTimer) clearTimeout(autosaveTimer);
    $('autosave-indicator').textContent = 'unsaved changes';
    $('autosave-indicator').style.opacity = '1';
    autosaveTimer = setTimeout(() => {
        saveState();
        $('autosave-indicator').textContent = 'saved';
        setTimeout(() => { $('autosave-indicator').style.opacity = '0.4'; }, 1000);
    }, 600);
}

function collectAllEdits() {
    const edits = {};
    $('tasks-list').querySelectorAll('.task-card').forEach(card => {
        const id = parseInt(card.dataset.id);
        const tags = [...card.querySelectorAll('.tag-list .tag')].map(el => el.childNodes[0].textContent.trim());
        const commentsRaw = card.querySelector('.f-comments').value.trim();
        edits[id] = {
            name: card.querySelector('.f-name').value,
            description: card.querySelector('.f-desc').value,
            comments: commentsRaw ? commentsRaw.split('\n').map(s => s.trim()).filter(Boolean) : [],
            tags
        };
    });
    return edits;
}

/* ── confirm ── */
function collectCard(card) {
    const id = parseInt(card.dataset.id);
    const original = tasksData.find(t => t._id === id);
    const tags = [...card.querySelectorAll('.tag-list .tag')].map(el => el.childNodes[0].textContent.trim());
    const commentsRaw = card.querySelector('.f-comments').value.trim();
    const comments = commentsRaw ? commentsRaw.split('\n').map(s => s.trim()).filter(Boolean) : [];
    const updated = {
        name: card.querySelector('.f-name').value,
        description: card.querySelector('.f-desc').value,
        comments,
        tags
    };
    const orig = { name: original.name, description: original.description, comments: original.comments || [], tags: original.tags || [] };
    return { status: JSON.stringify(updated) !== JSON.stringify(orig) ? 'UPDATE' : 'APPROVE', taskDTO: updated };
}

async function confirmTasks() {
    if (confirmed) return;
    const cards = [...$('tasks-list').querySelectorAll('.task-card')];
    const confirmTasks = [];

    cards.forEach(card => {
        const r = collectCard(card);
        confirmTasks.push({ status: r.status, taskDTO: r.taskDTO });
    });

    deletedIds.forEach(id => {
        const t = tasksData.find(x => x._id === id);
        if (t) confirmTasks.push({ status: 'DELETE', taskDTO: { name: t.name, description: t.description, comments: t.comments || [], tags: t.tags || [] } });
    });

    $('btn-confirm').disabled = true;
    $('btn-confirm').textContent = '[ sending... ]';
    retryFn = confirmTasks;

    try {
        const res = await fetch(`${API}/v1/api/generation/${currentTaskId}/confirm`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ confirmTasks })
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();

        if (data.success) {
            confirmed = true;
            saveState();
            markConfirmed();
            const pname = data.tasks?.projectName || $('project-name').textContent;
            const div = document.createElement('div');
            div.className = 'confirm-result';
            div.innerHTML = `<span class="ok">✓ confirmed successfully.</span>  project: ${esc(pname)}`;
            $('tasks-section').appendChild(div);
            toast('confirmed!', 'ok');
            localStorage.removeItem(LS);
        } else {
            $('btn-confirm').disabled = false;
            $('btn-confirm').textContent = '[ confirm ]';
            toast('server returned success=false', 'err');
        }
    } catch (e) {
        $('btn-confirm').disabled = false;
        $('btn-confirm').textContent = '[ confirm ]';
        toast(`confirm failed: ${e.message}`, 'err');
        retryFn = confirmTasks;
    }
}

function markConfirmed() {
    $('btn-confirm').textContent = '[ confirmed ]';
    $('btn-confirm').disabled = true;
    $('tasks-list').querySelectorAll('.task-card').forEach(c => {
        c.querySelectorAll('input,textarea,button').forEach(el => el.disabled = true);
    });
}
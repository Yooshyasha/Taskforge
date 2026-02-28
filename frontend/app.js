const API = '';
let pollTimer = null;
let currentTaskId = null;
let tasksData = [];
let deletedIds = new Set();

const $ = id => document.getElementById(id);

function show(id) { $(id).classList.remove('hidden'); }
function hide(id) { $(id).classList.add('hidden'); }

function showError(msg) {
    hide('generate-section');
    hide('status-section');
    hide('tasks-section');
    $('error-msg').textContent = msg;
    show('error-section');
}

function reset() {
    clearInterval(pollTimer);
    currentTaskId = null;
    tasksData = [];
    deletedIds = new Set();
    $('input-text').value = '';
    hide('error-section');
    hide('status-section');
    hide('tasks-section');
    show('generate-section');
}

async function generate() {
    const text = $('input-text').value.trim();
    if (!text) return;
    $('btn-generate').disabled = true;

    try {
        const res = await fetch(`${API}/v1/api/generation/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ text })
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        currentTaskId = data.taskId;
        hide('generate-section');
        show('status-section');
        $('status-text').textContent = 'waiting for generation...';
        pollTimer = setInterval(poll, 2500);
    } catch (e) {
        $('btn-generate').disabled = false;
        showError(`generation failed: ${e.message}`);
    }
}

async function poll() {
    try {
        const res = await fetch(`${API}/v1/api/generation/${currentTaskId}`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();

        if (data.status === 'ACTIVE') return;
        clearInterval(pollTimer);

        if (data.status === 'FAILED') {
            showError('generation failed on server side');
            return;
        }
        if (data.status === 'COMPLETE') {
            renderTasks(data.generatedTasks);
        }
    } catch (e) {
        clearInterval(pollTimer);
        showError(`poll error: ${e.message}`);
    }
}

function renderTasks(data) {
    tasksData = data.tasks.map((t, i) => ({ ...t, _id: i }));
    $('project-name').textContent = data.projectName || '';
    hide('status-section');
    show('tasks-section');
    renderList();
}

function renderList() {
    const list = $('tasks-list');
    list.innerHTML = '';
    tasksData.forEach(task => {
        if (deletedIds.has(task._id)) return;
        list.appendChild(makeCard(task));
    });
}

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

    card.querySelectorAll('.remove-tag').forEach(btn => {
        btn.addEventListener('click', function() { this.parentElement.remove(); });
    });

    return card;
}

function tagChip(t) {
    return `<span class="tag">${esc(t)}<button class="remove-tag" onclick="this.parentElement.remove()">×</button></span>`;
}

function esc(s) {
    return (s || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function addTag(btn) {
    const row = btn.closest('.task-card');
    const inp = row.querySelector('.tag-inp');
    const val = inp.value.trim();
    if (!val) return;
    const tagList = row.querySelector('.tag-list');
    const span = document.createElement('span');
    span.className = 'tag';
    span.innerHTML = `${esc(val)}<button class="remove-tag" onclick="this.parentElement.remove()">×</button>`;
    tagList.appendChild(span);
    inp.value = '';
}

function deleteCard(btn) {
    const card = btn.closest('.task-card');
    const id = parseInt(card.dataset.id);
    deletedIds.add(id);
    card.remove();
}

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
    const changed = JSON.stringify(updated) !== JSON.stringify({ name: original.name, description: original.description, comments: original.comments || [], tags: original.tags || [] });
    return { status: changed ? 'UPDATE' : 'APPROVE', taskDTO: updated, _id: id };
}

async function confirm() {
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
    try {
        const res = await fetch(`${API}/v1/api/generation/${currentTaskId}/confirm`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ confirmTasks })
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        if (data.success) {
            const div = document.createElement('div');
            div.className = 'confirm-result';
            div.innerHTML = `<span class="ok">confirmed successfully.</span> project: ${esc(data.tasks?.projectName || '')}`;
            $('tasks-section').appendChild(div);
            $('btn-confirm').textContent = '[ confirmed ]';
        } else {
            showError('confirm returned success=false');
        }
    } catch (e) {
        $('btn-confirm').disabled = false;
        showError(`confirm error: ${e.message}`);
    }
}
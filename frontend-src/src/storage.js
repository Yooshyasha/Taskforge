const KEY = 'taskgen_v2'
const DRAFT_KEY = 'taskgen_v2_draft'
const LANGUAGE_KEY = 'taskgen_language'
const TASK_DEPTH_KEY = 'taskgen_task_depth'

export function saveState(state) {
  try {
    localStorage.setItem(KEY, JSON.stringify(state))
  } catch {}
}

export function loadState() {
  try {
    const raw = localStorage.getItem(KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function clearState() {
  localStorage.removeItem(KEY)
  localStorage.removeItem(DRAFT_KEY)
}

export function saveDraft(text) {
  try {
    if (text) localStorage.setItem(DRAFT_KEY, text)
    else localStorage.removeItem(DRAFT_KEY)
  } catch {}
}

export function loadDraft() {
  try {
    return localStorage.getItem(DRAFT_KEY) || ''
  } catch {
    return ''
  }
}

export function saveLanguage(language) {
  try {
    localStorage.setItem(LANGUAGE_KEY, language)
  } catch {}
}

export function loadLanguage() {
  try {
    return localStorage.getItem(LANGUAGE_KEY) || null
  } catch {
    return null
  }
}

export function saveTaskDepth(taskDepth) {
  try {
    localStorage.setItem(TASK_DEPTH_KEY, taskDepth)
  } catch {}
}

export function loadTaskDepth() {
  try {
    return localStorage.getItem(TASK_DEPTH_KEY) || null
  } catch {
    return null
  }
}

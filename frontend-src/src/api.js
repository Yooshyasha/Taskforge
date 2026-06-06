const BASE = ''

async function request(url, options = {}) {
  const res = await fetch(`${BASE}${url}`, options)
  if (!res.ok) {
    let message = `HTTP ${res.status}`
    try {
      const body = await res.json()
      message = body.message || body.error || message
    } catch {}
    throw new Error(message)
  }
  return res.json()
}

export async function fetchProjects() {
  const data = await request('/v1/api/vikunja/projects')
  // ResponseGetProjects wraps a list: { projects: [...] }
  return data.projects ?? data ?? []
}

export async function startGeneration(text, projectId, language, taskDepth) {
  const body = { text, language, taskDepth }
  if (projectId != null) body.projectId = projectId
  return request('/v1/api/generation', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
}

export async function pollGeneration(taskId) {
  return request(`/v1/api/generation/${taskId}`)
}

export async function sendAnswer(taskId, answer) {
  return request('/v1/api/generation/answer', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ taskId, answer }),
  })
}

export async function confirmTasks(taskId, confirmPayload) {
  return request(`/v1/api/generation/${taskId}/confirm`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ confirmTasks: confirmPayload }),
  })
}

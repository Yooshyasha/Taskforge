import { useRef, useEffect } from 'react'
import { AiDialog } from './AiDialog'

const LANGUAGES = [
  { value: 'en', label: 'English' },
  { value: 'ru', label: 'Русский' },
  { value: 'de', label: 'Deutsch' },
  { value: 'fr', label: 'Français' },
  { value: 'es', label: 'Español' },
]

const TASK_DEPTHS = [
  { value: 'VERTICAL_SINCE', label: 'vertical' },
  { value: 'GRAIN',          label: 'grain' },
]

export function InputPanel({
  collapsed,
  onToggleCollapse,
  project,
  inputText,
  onInputChange,
  language,
  onLanguageChange,
  taskDepth,
  onTaskDepthChange,
  onGenerate,
  genStatus,
  elapsed,
  progress,
  dialog,
  onSendAnswer,
  aiThinking,
  onEditBrief,
}) {
  const textareaRef = useRef(null)

  // Auto-resize textarea
  useEffect(() => {
    const el = textareaRef.current
    if (!el) return
    el.style.height = 'auto'
    el.style.height = Math.min(el.scrollHeight, window.innerHeight * 0.55) + 'px'
  }, [inputText])

  const isNew        = project === 'new' || project == null
  const projectName  = isNew ? null : project?.name
  const isGenerating = genStatus === 'generating' || genStatus === 'polling'
  const isQuestion   = genStatus === 'question'
  const isComplete   = genStatus === 'complete'
  const isBusy       = isGenerating || isQuestion
  const hasDialog    = dialog.length > 0

  function handleKeyDown(e) {
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
      e.preventDefault()
      onGenerate()
    }
  }

  if (collapsed) {
    return (
      <div className="input-panel input-panel--collapsed">
        <div className="input-panel__sidebar">
          <button
            className="input-panel__sidebar-expand"
            onClick={onToggleCollapse}
            title="expand input panel"
          >
            ›
          </button>
          <span className="input-panel__sidebar-label">
            {projectName ?? 'new project'}
          </span>
        </div>
      </div>
    )
  }

  return (
    <div className="input-panel">
      {/* Collapse handle */}
      <button
        className="input-panel__collapse-btn"
        onClick={onToggleCollapse}
        title="collapse panel"
      >
        ‹
      </button>

      <div className="input-panel__content">
        {/* Top bar: project context */}
        <div className="input-panel__top">
          {isNew ? (
            <span className="input-panel__new-badge">+ new project</span>
          ) : (
            <div className="input-panel__project-badge">
              <span className="input-panel__project-dot" />
              <span>{projectName}</span>
            </div>
          )}

          {isQuestion && (
            <span className="input-panel__question-badge">
              <span className="input-panel__question-dot" />
              AI is asking
            </span>
          )}
        </div>

        {/* Body: textarea OR dialog */}
        <div className="input-panel__body">
          {hasDialog ? (
            <AiDialog
              dialog={dialog}
              onSend={onSendAnswer}
              canSend={isQuestion}
              thinking={aiThinking}
            />
          ) : (
            <>
              <div className="input-panel__label">
                {isNew ? 'project description' : 'describe changes'}
              </div>
              <textarea
                ref={textareaRef}
                className="input-panel__textarea"
                placeholder={
                  isNew
                    ? 'describe the project you want to build...'
                    : 'describe what you want to add, change or remove...'
                }
                value={inputText}
                onChange={e => onInputChange(e.target.value)}
                onKeyDown={handleKeyDown}
                disabled={isGenerating}
              />
              <span className="input-panel__hint">ctrl+enter to generate</span>
            </>
          )}
        </div>

        {/* Generation status bar */}
        {(isGenerating || isComplete) && (
          <div className="input-panel__status">
            {isGenerating && (
              <>
                <div className="status-row">
                  <span className="spinner" />
                  <span className="status-text">
                    {genStatus === 'generating' ? 'starting...' : 'generating tasks...'}
                  </span>
                  {elapsed > 0 && (
                    <span className="status-elapsed">{elapsed}s</span>
                  )}
                </div>
                <div className="progress-bar">
                  <div
                    className="progress-fill"
                    style={{ width: `${progress}%` }}
                  />
                </div>
              </>
            )}
            {isComplete && (
              <div className="status-row">
                <span className="input-panel__complete">✓ generation complete</span>
              </div>
            )}
          </div>
        )}

        {/* Footer actions */}
        <div className="input-panel__footer">
          {hasDialog && !isBusy ? (
            <button className="btn btn--ghost" onClick={onEditBrief}>
              ↻ edit brief
            </button>
          ) : (
            <>
              <button
                className="btn btn--primary"
                onClick={onGenerate}
                disabled={isBusy || !inputText.trim()}
              >
                {isGenerating
                  ? '⟳ generating...'
                  : isQuestion
                  ? '⟳ waiting...'
                  : isComplete
                  ? 'regenerate'
                  : 'generate'}
              </button>

              <label className="input-panel__select" title="language">
                <span className="input-panel__select-icon">🌐</span>
                <select
                  value={language}
                  onChange={e => onLanguageChange(e.target.value)}
                  disabled={isBusy}
                >
                  {LANGUAGES.map(l => (
                    <option key={l.value} value={l.value}>{l.label}</option>
                  ))}
                </select>
              </label>

              <label className="input-panel__select" title="task depth">
                <span className="input-panel__select-icon">≣</span>
                <select
                  value={taskDepth}
                  onChange={e => onTaskDepthChange(e.target.value)}
                  disabled={isBusy}
                >
                  {TASK_DEPTHS.map(d => (
                    <option key={d.value} value={d.value}>{d.label}</option>
                  ))}
                </select>
              </label>
            </>
          )}
        </div>
      </div>
    </div>
  )
}

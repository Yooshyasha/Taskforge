import { useRef, useEffect, useState, useCallback } from 'react'
import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import remarkBreaks from 'remark-breaks'

const MD_PLUGINS = [remarkGfm, remarkBreaks]
const MD_COMPONENTS = {
  a: ({ node, ...props }) => (
    <a {...props} target="_blank" rel="noopener noreferrer" />
  ),
}

function Markdown({ children }) {
  return (
    <ReactMarkdown remarkPlugins={MD_PLUGINS} components={MD_COMPONENTS}>
      {children}
    </ReactMarkdown>
  )
}

export function AiDialog({ dialog, onSend, canSend, thinking }) {
  const [answerText, setAnswerText] = useState('')
  const historyRef  = useRef(null)
  const textareaRef = useRef(null)

  useEffect(() => {
    const el = historyRef.current
    if (el) el.scrollTop = el.scrollHeight
  }, [dialog, thinking])

  useEffect(() => {
    if (canSend) textareaRef.current?.focus()
  }, [canSend])

  useEffect(() => {
    const el = textareaRef.current
    if (!el) return
    // Resizing the textarea changes the history's available height, which can
    // shift its scroll position. Preserve the history scroll so typing never
    // jerks the chat.
    const history = historyRef.current
    const prevScrollTop = history ? history.scrollTop : 0
    el.style.height = 'auto'
    el.style.height = Math.min(el.scrollHeight, 160) + 'px'
    if (history) history.scrollTop = prevScrollTop
  }, [answerText])

  const handleSend = useCallback(() => {
    const trimmed = answerText.trim()
    if (!trimmed) return
    onSend(trimmed)
    setAnswerText('')
  }, [answerText, onSend])

  function handleKeyDown(e) {
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
      e.preventDefault()
      handleSend()
    }
  }

  return (
    <div className="ai-dialog">
      <div className="ai-dialog__history" ref={historyRef}>
        {dialog.map((msg, i) => (
          <div key={i} className={`ai-dialog__bubble ai-dialog__bubble--${msg.role}`}>
            <span className="ai-dialog__bubble-label">
              {msg.role === 'ai' ? 'AI' : 'you'}
            </span>
            <div className="ai-dialog__bubble-text">
              <Markdown>{msg.text}</Markdown>
            </div>
          </div>
        ))}

        {thinking && (
          <div className="ai-dialog__bubble ai-dialog__bubble--ai">
            <span className="ai-dialog__bubble-label">AI</span>
            <div className="ai-dialog__bubble-text ai-dialog__bubble-text--thinking">
              <span className="ai-dialog__typing" aria-label="AI is thinking">
                <span /><span /><span />
              </span>
            </div>
          </div>
        )}
      </div>

      {canSend && (
        <div className="ai-dialog__input-area">
          <textarea
            ref={textareaRef}
            className="ai-dialog__textarea"
            placeholder="your answer..."
            value={answerText}
            onChange={e => setAnswerText(e.target.value)}
            onKeyDown={handleKeyDown}
          />
          <div className="ai-dialog__send-row">
            <span className="ai-dialog__hint">ctrl+enter to send</span>
            <button
              className="btn btn--primary btn--sm"
              onClick={handleSend}
              disabled={!answerText.trim()}
            >
              send →
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

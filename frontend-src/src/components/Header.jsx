import { useState, useEffect } from 'react'

export function Header({ projectName, onBack, onClearData }) {
  const [online, setOnline] = useState(navigator.onLine)

  useEffect(() => {
    const up   = () => setOnline(true)
    const down = () => setOnline(false)
    window.addEventListener('online', up)
    window.addEventListener('offline', down)
    return () => {
      window.removeEventListener('online', up)
      window.removeEventListener('offline', down)
    }
  }, [])

  return (
    <header className="header">
      <span className="header__logo">trof</span>
      <span className="header__version">v1.1.0</span>

      {projectName && (
        <>
          <span className="header__sep">/</span>
          <span className="header__project">{projectName}</span>
        </>
      )}

      {!online && <span className="offline-badge">offline</span>}

      <div className="header__spacer" />

      <div className="header__actions">
        {onBack && (
          <button className="btn btn--ghost btn--sm header__back" onClick={onBack}>
            ← projects
          </button>
        )}
        <button className="btn btn--ghost btn--sm" onClick={onClearData}>
          clear data
        </button>
      </div>
    </header>
  )
}

const MOD = typeof navigator !== 'undefined' && /Mac|iP/.test(navigator.platform)
  ? '⌘'
  : 'ctrl'

function Hotkey({ keys, label }) {
  return (
    <span className="hotkey">
      {keys.map((k, i) => (
        <kbd key={i} className="hotkey__key">{k}</kbd>
      ))}
      <span className="hotkey__label">{label}</span>
    </span>
  )
}

export function HotkeyBar({
  view,
  genStatus,
  hasDialog,
  hasGenerated,
  confirmed,
  inputCollapsed,
}) {
  const items = []

  if (view === 'projects') {
    items.push({ keys: ['↵'], label: 'open project' })
    items.push({ keys: ['tab'], label: 'navigate' })
  } else {
    const isQuestion = genStatus === 'question'

    if (isQuestion) {
      items.push({ keys: [MOD, '↵'], label: 'send answer' })
    } else if (!hasDialog) {
      items.push({ keys: [MOD, '↵'], label: 'generate' })
    }

    if (hasGenerated && !confirmed) {
      items.push({ keys: [MOD, 'S'], label: 'confirm all' })
    }

    items.push({
      keys: [MOD, 'B'],
      label: inputCollapsed ? 'expand panel' : 'collapse panel',
    })
  }

  return (
    <div className="hotkey-bar">
      {items.map((it, i) => (
        <Hotkey key={i} keys={it.keys} label={it.label} />
      ))}
    </div>
  )
}

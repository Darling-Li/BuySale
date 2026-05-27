const blockedKeyCombos = new Set(['i', 'j', 'c'])

export function installConsoleGuard() {
  if (import.meta.env.DEV || import.meta.env.VITE_CONSOLE_GUARD === 'false') {
    return
  }

  document.addEventListener('contextmenu', preventDefault, true)
  window.addEventListener('keydown', handleKeydown, true)
  window.setInterval(checkDevtoolsSize, 1200)
}

function handleKeydown(event) {
  const key = event.key.toLowerCase()
  const blocked = event.key === 'F12'
    || (event.ctrlKey && event.shiftKey && blockedKeyCombos.has(key))
    || (event.metaKey && event.altKey && blockedKeyCombos.has(key))
    || (event.ctrlKey && ['u', 's'].includes(key))

  if (blocked) {
    preventDefault(event)
    lockScreen()
  }
}

function checkDevtoolsSize() {
  const widthGap = Math.abs(window.outerWidth - window.innerWidth)
  const heightGap = Math.abs(window.outerHeight - window.innerHeight)
  if (widthGap > 180 || heightGap > 180) {
    lockScreen()
  }
}

function preventDefault(event) {
  event.preventDefault()
  event.stopPropagation()
}

function lockScreen() {
  if (document.querySelector('.console-guard-overlay')) {
    return
  }

  const overlay = document.createElement('div')
  overlay.className = 'console-guard-overlay'
  overlay.textContent = '访问环境异常，请关闭调试工具后刷新页面。'
  document.body.appendChild(overlay)
}

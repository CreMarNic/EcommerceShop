import { useEffect, useState } from 'react'
import './App.css'

function App() {
  const [email, setEmail] = useState('user@example.com')
  const [password, setPassword] = useState('user123')
  const [currentUser, setCurrentUser] = useState(null)
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)

  async function loadCurrentUser() {
    const response = await fetch('/api/auth/me', {
      credentials: 'include',
    })

    if (!response.ok) {
      setCurrentUser(null)
      return
    }

    setCurrentUser(await response.json())
  }

  useEffect(() => {
    loadCurrentUser()
  }, [])

  async function login(event) {
    event.preventDefault()
    setLoading(true)
    setMessage('')

    const body = new URLSearchParams()
    body.set('username', email)
    body.set('password', password)

    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body,
      credentials: 'include',
    })

    const data = await response.json().catch(() => ({}))
    setMessage(data.message ?? (response.ok ? 'Login successful' : 'Login failed'))

    if (response.ok) {
      await loadCurrentUser()
    }

    setLoading(false)
  }

  async function logout() {
    setLoading(true)
    setMessage('')

    const response = await fetch('/api/auth/logout', {
      method: 'POST',
      credentials: 'include',
    })

    const data = await response.json().catch(() => ({}))
    setMessage(data.message ?? 'Logout complete')
    setCurrentUser(null)
    setLoading(false)
  }

  return (
    <main className="auth-page">
      <section className="auth-panel" aria-labelledby="auth-title">
        <div className="brand-row">
          <div className="brand-mark" aria-hidden="true">ES</div>
          <div>
            <h1 id="auth-title">Ecommerce Shop</h1>
            <p>Sign in to manage your cart, orders, and checkout.</p>
          </div>
        </div>

        {currentUser ? (
          <div className="session-box">
            <div>
              <span className="label">Signed in as</span>
              <strong>{currentUser.username}</strong>
            </div>
            <div className="roles">
              {currentUser.roles.map((role) => (
                <span key={role}>{role.replace('ROLE_', '')}</span>
              ))}
            </div>
            <button type="button" onClick={logout} disabled={loading}>
              Log out
            </button>
          </div>
        ) : (
          <form className="login-form" onSubmit={login}>
            <label>
              Email
              <input
                type="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                autoComplete="username"
                required
              />
            </label>
            <label>
              Password
              <input
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                autoComplete="current-password"
                required
              />
            </label>
            <button type="submit" disabled={loading}>
              Log in
            </button>
          </form>
        )}

        {!currentUser && (
          <>
            <div className="divider"><span>or</span></div>
            <div className="oauth-actions">
              <a href="/oauth2/authorization/google">Continue with Google</a>
              <a href="/oauth2/authorization/github">Continue with GitHub</a>
            </div>
          </>
        )}

        {message && <p className="message" role="status">{message}</p>}
      </section>
    </main>
  )
}

export default App

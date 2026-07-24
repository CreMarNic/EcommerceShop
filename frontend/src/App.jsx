import { useState } from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { clearAuth, getStoredUser, hasStoredAuth } from './api/ecommerceApi'
import { HomePage } from './components/HomePage'
import { CheckoutPage } from './components/CheckoutPage'
import { OrdersPage } from './components/OrdersPage'
import { SignInPage } from './components/SignInPage'
import { TrackingPage } from './components/TrackingPage'

function App() {
  const [authenticated, setAuthenticated] = useState(hasStoredAuth())
  const [user, setUser] = useState(getStoredUser())

  function handleAuthenticated(authenticatedUser) {
    setUser(authenticatedUser)
    setAuthenticated(true)
  }

  function handleLogout() {
    clearAuth()
    setUser(null)
    setAuthenticated(false)
  }

  if (!authenticated) {
    return <SignInPage onAuthenticated={handleAuthenticated} />
  }

  return (
      <BrowserRouter>
        <Routes>
            <Route path="/" element={<HomePage user={user} onLogout={handleLogout} />} />
            <Route path="/checkout" element={<CheckoutPage user={user} onLogout={handleLogout} />} />
            <Route path="/orders" element={<OrdersPage user={user} onLogout={handleLogout} />} />
            <Route path="/tracking" element={<TrackingPage user={user} onLogout={handleLogout} />} />
        </Routes>
      </BrowserRouter>
  )
}

export default App

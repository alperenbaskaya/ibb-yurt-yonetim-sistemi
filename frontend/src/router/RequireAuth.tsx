import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { AuthInitializationScreen } from '../components/common/AuthInitializationScreen'
import { useAuth } from '../features/auth/useAuth'

export function RequireAuth() {
  const location = useLocation()
  const {
    isAuthenticated,
    isInitializing,
    initializationError,
    retryInitialization,
  } = useAuth()

  if (isInitializing) {
    return <AuthInitializationScreen />
  }

  if (initializationError) {
    return (
      <AuthInitializationScreen
        errorMessage={initializationError}
        onRetry={() => void retryInitialization()}
      />
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  return <Outlet />
}

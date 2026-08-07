import {
  createContext,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from 'react'
import axios from 'axios'
import { getCurrentUser, loginRequest } from '../../api/authApi'
import { subscribeToAuthenticationFailure } from '../../api/authEvents'
import type { CurrentUserResponse, LoginRequest } from '../../types/auth'
import { getApiErrorMessage } from '../../utils/apiError'
import {
  getAccessToken,
  removeAccessToken,
  setAccessToken,
} from '../../utils/tokenStorage'

interface AuthContextValue {
  user: CurrentUserResponse | null
  isAuthenticated: boolean
  isInitializing: boolean
  initializationError: string | null
  login: (credentials: LoginRequest) => Promise<void>
  logout: () => void
  retryInitialization: () => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | undefined>(
  undefined,
)

interface AuthProviderProps {
  children: ReactNode
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<CurrentUserResponse | null>(null)
  const [isInitializing, setIsInitializing] = useState(true)
  const [initializationError, setInitializationError] = useState<string | null>(
    null,
  )
  const initializationStarted = useRef(false)

  const hydrateCurrentUser = useCallback(async (): Promise<void> => {
    if (!getAccessToken()) {
      setUser(null)
      setInitializationError(null)
      setIsInitializing(false)
      return
    }

    setIsInitializing(true)
    setInitializationError(null)

    try {
      const currentUser = await getCurrentUser()
      setUser(currentUser)
    } catch (error: unknown) {
      setUser(null)

      if (!axios.isAxiosError(error) || error.response?.status !== 401) {
        setInitializationError(
          getApiErrorMessage(
            error,
            'Oturum bilgileri alınamadı. Lütfen tekrar deneyin.',
          ),
        )
      }
    } finally {
      setIsInitializing(false)
    }
  }, [])

  useEffect(() => {
    const unsubscribe = subscribeToAuthenticationFailure(() => {
      setUser(null)
      setInitializationError(null)
      setIsInitializing(false)
    })

    if (!initializationStarted.current) {
      initializationStarted.current = true
      void hydrateCurrentUser()
    }

    return unsubscribe
  }, [hydrateCurrentUser])

  const login = useCallback(async (credentials: LoginRequest): Promise<void> => {
    setInitializationError(null)

    const loginResponse = await loginRequest(credentials)
    setAccessToken(loginResponse.accessToken)

    try {
      const currentUser = await getCurrentUser()
      setUser(currentUser)
    } catch (error: unknown) {
      removeAccessToken()
      setUser(null)
      throw error
    }
  }, [])

  const logout = useCallback((): void => {
    removeAccessToken()
    setUser(null)
    setInitializationError(null)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: user !== null,
      isInitializing,
      initializationError,
      login,
      logout,
      retryInitialization: hydrateCurrentUser,
    }),
    [
      user,
      isInitializing,
      initializationError,
      login,
      logout,
      hydrateCurrentUser,
    ],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

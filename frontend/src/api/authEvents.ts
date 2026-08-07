type AuthenticationFailureListener = () => void

const authenticationFailureListeners =
  new Set<AuthenticationFailureListener>()

export function notifyAuthenticationFailure(): void {
  authenticationFailureListeners.forEach((listener) => listener())
}

export function subscribeToAuthenticationFailure(
  listener: AuthenticationFailureListener,
): () => void {
  authenticationFailureListeners.add(listener)

  return () => {
    authenticationFailureListeners.delete(listener)
  }
}

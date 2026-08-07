const ACCESS_TOKEN_KEY = 'ibb-yurtlar-access-token'

export function getAccessToken(): string | null {
  return sessionStorage.getItem(ACCESS_TOKEN_KEY)
}

export function setAccessToken(accessToken: string): void {
  sessionStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
}

export function removeAccessToken(): void {
  sessionStorage.removeItem(ACCESS_TOKEN_KEY)
}

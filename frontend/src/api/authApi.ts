import { httpClient } from './httpClient'
import type {
  CurrentUserResponse,
  LoginRequest,
  LoginResponse,
} from '../types/auth'

export async function loginRequest(
  credentials: LoginRequest,
): Promise<LoginResponse> {
  const response = await httpClient.post<LoginResponse>(
    '/auth/login',
    credentials,
  )

  return response.data
}

export async function getCurrentUser(): Promise<CurrentUserResponse> {
  const response = await httpClient.get<CurrentUserResponse>('/auth/me')

  return response.data
}

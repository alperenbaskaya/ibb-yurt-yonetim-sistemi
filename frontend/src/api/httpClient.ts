import axios from 'axios'
import { notifyAuthenticationFailure } from './authEvents'
import {
  getAccessToken,
  removeAccessToken,
} from '../utils/tokenStorage'

export const httpClient = axios.create({
  baseURL: '/api',
  headers: {
    Accept: 'application/json',
  },
})

httpClient.interceptors.request.use((config) => {
  const accessToken = getAccessToken()

  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }

  return config
})

httpClient.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      const accessToken = getAccessToken()

      if (accessToken) {
        removeAccessToken()
        notifyAuthenticationFailure()
      }
    }

    return Promise.reject(error)
  },
)

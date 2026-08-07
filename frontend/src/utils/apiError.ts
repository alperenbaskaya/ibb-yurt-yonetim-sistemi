import axios from 'axios'
import type { ApiError } from '../types/api'

export function getApiErrorMessage(
  error: unknown,
  fallbackMessage: string,
): string {
  if (!axios.isAxiosError<ApiError>(error)) {
    return fallbackMessage
  }

  if (!error.response) {
    return 'Sunucuya ulaşılamıyor. Lütfen bağlantınızı kontrol edip tekrar deneyin.'
  }

  const message = error.response.data?.message

  return typeof message === 'string' && message.trim().length > 0
    ? message
    : fallbackMessage
}

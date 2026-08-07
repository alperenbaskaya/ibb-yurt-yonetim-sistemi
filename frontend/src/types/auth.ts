export type Role = 'ADMIN' | 'REVIEWER' | 'STUDENT'

export type AdminScope = 'GLOBAL' | 'DORMITORY'

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  tokenType: 'Bearer'
  userId: number
  firstName: string
  lastName: string
  email: string
  role: Role
  adminScope: AdminScope | null
  dormitoryId: number | null
  dormitoryName: string | null
}

export interface CurrentUserResponse {
  userId: number
  firstName: string
  lastName: string
  email: string
  role: Role
  adminScope: AdminScope | null
  active: boolean
  dormitoryId: number | null
  dormitoryName: string | null
}

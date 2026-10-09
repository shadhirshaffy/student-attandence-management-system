import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useAuthStore } from './authStore'
import api from '../services/api'

vi.mock('../services/api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

describe('authStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('stores the token and user after a successful login', async () => {
    api.post.mockResolvedValue({
      data: {
        token: 'jwt-token',
        userId: 12,
        name: 'Admin User',
        email: 'admin@example.com',
        role: 'ADMIN',
      },
    })

    const auth = useAuthStore()
    const user = await auth.login({ email: 'admin@example.com', password: 'secret' })

    expect(api.post).toHaveBeenCalledWith('/auth/login', {
      email: 'admin@example.com',
      password: 'secret',
    })
    expect(user.role).toBe('ADMIN')
    expect(auth.isAuthenticated).toBe(true)
    expect(localStorage.getItem('auth_token')).toBe('jwt-token')
    expect(JSON.parse(localStorage.getItem('auth_user'))).toMatchObject({ email: 'admin@example.com' })
  })

  it('clears auth state when session restore fails', async () => {
    localStorage.setItem('auth_token', 'expired-token')
    api.get.mockRejectedValue(new Error('Unauthorized'))

    const auth = useAuthStore()
    const restored = await auth.restoreSession()

    expect(restored).toBe(false)
    expect(auth.isAuthenticated).toBe(false)
    expect(localStorage.getItem('auth_token')).toBeNull()
  })

  it('removes local session data on logout', () => {
    localStorage.setItem('auth_token', 'jwt-token')
    localStorage.setItem('auth_user', JSON.stringify({ role: 'STUDENT' }))

    const auth = useAuthStore()
    auth.token = 'jwt-token'
    auth.user = { role: 'STUDENT' }

    auth.logout()

    expect(auth.token).toBeNull()
    expect(auth.user).toBeNull()
    expect(localStorage.getItem('auth_token')).toBeNull()
    expect(localStorage.getItem('auth_user')).toBeNull()
  })
})

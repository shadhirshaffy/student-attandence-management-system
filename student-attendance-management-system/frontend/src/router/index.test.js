import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import router from './index'
import { useAuthStore } from '../stores/authStore'

vi.mock('../services/api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

describe('router guards', () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
    await router.replace('/')
  })

  it('redirects unauthenticated users to login with the intended route', async () => {
    await router.push('/admin/users')
    await router.isReady()

    expect(router.currentRoute.value.path).toBe('/login')
    expect(router.currentRoute.value.query.redirect).toBe('/admin/users')
  })

  it('keeps authenticated users out of another role dashboard', async () => {
    const auth = useAuthStore()
    auth.token = 'student-token'
    auth.user = { role: 'STUDENT', email: 'student@example.com' }

    await router.push('/admin')
    await router.isReady()

    expect(router.currentRoute.value.path).toBe('/student')
  })

  it('redirects logged-in users away from the guest login screen', async () => {
    const auth = useAuthStore()
    auth.token = 'lecturer-token'
    auth.user = { role: 'LECTURER', email: 'lecturer@example.com' }

    await router.push('/login')
    await router.isReady()

    expect(router.currentRoute.value.path).toBe('/lecturer')
  })
})

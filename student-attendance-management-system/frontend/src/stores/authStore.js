import { defineStore } from 'pinia'
import api from '../services/api'

const tokenKey = 'auth_token'
const userKey = 'auth_user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(tokenKey),
    user: JSON.parse(localStorage.getItem(userKey) || 'null'),
    loading: false,
    error: '',
  }),

  getters: {
    isAuthenticated: (state) => Boolean(state.token && state.user),
    role: (state) => state.user?.role,
  },

  actions: {
    async login(credentials) {
      this.loading = true
      this.error = ''

      try {
        const { data } = await api.post('/auth/login', credentials)
        this.token = data.token
        this.user = {
          userId: data.userId,
          name: data.name,
          email: data.email,
          role: data.role,
        }
        localStorage.setItem(tokenKey, this.token)
        localStorage.setItem(userKey, JSON.stringify(this.user))
        return this.user
      } catch (error) {
        this.error = error.response?.data?.error || 'Login failed. Check your email and password.'
        throw error
      } finally {
        this.loading = false
      }
    },

    async restoreSession() {
      if (!this.token) {
        return false
      }

      try {
        const { data } = await api.get('/auth/me')
        this.user = data
        localStorage.setItem(userKey, JSON.stringify(data))
        return true
      } catch {
        this.logout()
        return false
      }
    },

    logout() {
      this.token = null
      this.user = null
      this.error = ''
      localStorage.removeItem(tokenKey)
      localStorage.removeItem(userKey)
    },
  },
})

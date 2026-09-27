import { defineStore } from 'pinia'
import api from '../services/api'

function message(error) {
  return error.response?.data?.error || error.response?.data?.message || 'Something went wrong.'
}

export const useSessionsStore = defineStore('sessions', {
  state: () => ({
    sessions: [],
    currentSession: null,
    loading: false,
    saving: false,
    error: '',
    success: '',
  }),

  actions: {
    async fetchMySessions() {
      this.loading = true
      this.error = ''
      try {
        const { data } = await api.get('/lecturers/me/sessions')
        this.sessions = data
        return data
      } catch (error) {
        this.error = message(error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchSession(id) {
      this.loading = true
      this.error = ''
      try {
        const { data } = await api.get(`/sessions/${id}`)
        this.currentSession = data
        return data
      } catch (error) {
        this.error = message(error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async createSession(payload) {
      this.saving = true
      this.error = ''
      this.success = ''
      try {
        const { data } = await api.post('/sessions', payload)
        this.success = 'Session created successfully.'
        return data
      } catch (error) {
        this.error = message(error)
        throw error
      } finally {
        this.saving = false
      }
    },

    async transitionSession(id, action) {
      this.saving = true
      this.error = ''
      this.success = ''
      try {
        const { data } = await api.patch(`/sessions/${id}/${action}`)
        this.currentSession = data
        this.sessions = this.sessions.map((session) => (session.id === data.id ? data : session))
        this.success = `Session ${action}d successfully.`
        return data
      } catch (error) {
        this.error = message(error)
        throw error
      } finally {
        this.saving = false
      }
    },

    clearMessages() {
      this.error = ''
      this.success = ''
    },
  },
})

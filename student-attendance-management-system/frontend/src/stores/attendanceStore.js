import { defineStore } from 'pinia'
import api from '../services/api'

function message(error) {
  return error.response?.data?.error || error.response?.data?.message || 'Something went wrong.'
}

export const useAttendanceStore = defineStore('attendance', {
  state: () => ({
    manualAttendance: null,
    loading: false,
    saving: false,
    error: '',
    success: '',
  }),

  actions: {
    async fetchManualAttendance(sessionId) {
      this.loading = true
      this.error = ''
      try {
        const { data } = await api.get(`/sessions/${sessionId}/attendance/manual`)
        this.manualAttendance = data
        return data
      } catch (error) {
        this.error = message(error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async saveManualAttendance(sessionId, records) {
      this.saving = true
      this.error = ''
      this.success = ''
      try {
        const { data } = await api.put(`/sessions/${sessionId}/attendance/manual`, { records })
        this.manualAttendance = data
        this.success = 'Attendance saved successfully.'
        return data
      } catch (error) {
        this.error = message(error)
        throw error
      } finally {
        this.saving = false
      }
    },

    async submitQrAttendance(token) {
      this.saving = true
      this.error = ''
      this.success = ''
      try {
        const { data } = await api.post('/attendance/qr', { token })
        this.success = 'Attendance recorded successfully.'
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

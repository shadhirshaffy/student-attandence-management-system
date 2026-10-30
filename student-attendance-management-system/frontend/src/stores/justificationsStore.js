import { defineStore } from 'pinia'
import api from '../services/api'

export const useJustificationsStore = defineStore('justifications', {
  state: () => ({ requests: [], loading: false, saving: false, error: '', success: '' }),
  actions: {
    async fetchRequests() {
      this.loading = true
      this.error = ''
      try {
        const { data } = await api.get('/lecturers/me/absence-justifications')
        this.requests = data
      } catch (error) {
        this.error = error.response?.data?.message || error.response?.data?.error || 'Could not load absence requests.'
        throw error
      } finally { this.loading = false }
    },
    async review(id, status, reviewNote) {
      if (this.saving) return
      this.saving = true
      this.error = ''
      this.success = ''
      try {
        const { data } = await api.patch(`/lecturers/me/absence-justifications/${id}`, { status, reviewNote })
        this.requests = this.requests.map((item) => item.id === id ? data : item)
        this.success = `Absence request ${status === 'APPROVED' ? 'approved' : 'rejected'} successfully.`
        return data
      } catch (error) {
        this.error = error.response?.data?.message || error.response?.data?.error || 'Could not save the review.'
        throw error
      } finally { this.saving = false }
    },
  },
})

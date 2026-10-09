import { defineStore } from 'pinia'
import api from '../services/api'

function message(error) {
  return error.response?.data?.error || error.response?.data?.message || 'Something went wrong.'
}

export const useStatisticsStore = defineStore('statistics', {
  state: () => ({
    studentSummary: null,
    lecturerSummary: null,
    adminSummary: null,
    loading: false,
    error: '',
  }),

  actions: {
    async fetchStudentSummary() {
      this.loading = true
      this.error = ''
      try {
        const { data } = await api.get('/students/me/attendance-summary')
        this.studentSummary = data
        return data
      } catch (error) {
        this.error = message(error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchLecturerSummary() {
      this.loading = true
      this.error = ''
      try {
        const { data } = await api.get('/lecturers/me/attendance-summary')
        this.lecturerSummary = data
        return data
      } catch (error) {
        this.error = message(error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchAdminSummary() {
      this.loading = true
      this.error = ''
      try {
        const { data } = await api.get('/admin/attendance-summary')
        this.adminSummary = data
        return data
      } catch (error) {
        this.error = message(error)
        throw error
      } finally {
        this.loading = false
      }
    },
  },
})

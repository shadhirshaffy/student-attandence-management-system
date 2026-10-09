import { defineStore } from 'pinia'
import api from '../services/api'

function message(error) {
  return error.response?.data?.error || error.response?.data?.message || 'Something went wrong.'
}

function params(filters) {
  return Object.fromEntries(
    Object.entries(filters).filter(([, value]) => value !== '' && value !== null && value !== undefined),
  )
}

export const useReportsStore = defineStore('reports', {
  state: () => ({
    report: null,
    loading: false,
    exporting: false,
    error: '',
  }),

  actions: {
    async generate(filters) {
      this.loading = true
      this.error = ''
      try {
        const { data } = await api.get('/reports/attendance', { params: params(filters) })
        this.report = data
        return data
      } catch (error) {
        this.error = message(error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async exportReport(format, filters) {
      this.exporting = true
      this.error = ''
      try {
        const response = await api.get(`/reports/attendance/${format}`, {
          params: params(filters),
          responseType: 'blob',
        })
        const filename = filenameFromHeader(response.headers['content-disposition']) || `attendance-report.${format}`
        const href = URL.createObjectURL(response.data)
        const link = document.createElement('a')
        link.href = href
        link.download = filename
        document.body.appendChild(link)
        link.click()
        link.remove()
        URL.revokeObjectURL(href)
      } catch (error) {
        this.error = message(error)
        throw error
      } finally {
        this.exporting = false
      }
    },
  },
})

function filenameFromHeader(header) {
  if (!header) return ''
  const match = /filename="?([^"]+)"?/i.exec(header)
  return match?.[1] || ''
}

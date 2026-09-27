import { defineStore } from 'pinia'
import api from '../services/api'

function errorMessage(error) {
  return error.response?.data?.error || error.response?.data?.message || 'Something went wrong.'
}

export const useAdminUsersStore = defineStore('adminUsers', {
  state: () => ({
    users: [],
    loading: false,
    saving: false,
    error: '',
    success: '',
  }),

  getters: {
    students: (state) => state.users.filter((user) => user.role === 'STUDENT'),
    lecturers: (state) => state.users.filter((user) => user.role === 'LECTURER'),
    admins: (state) => state.users.filter((user) => user.role === 'ADMIN'),
  },

  actions: {
    async fetchUsers() {
      this.loading = true
      this.error = ''
      try {
        const { data } = await api.get('/admin/users')
        this.users = data
      } catch (error) {
        this.error = errorMessage(error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async createUser(payload) {
      this.saving = true
      this.error = ''
      this.success = ''
      try {
        const { data } = await api.post('/admin/users', payload)
        this.users = [data, ...this.users]
        this.success = 'User created successfully.'
        return data
      } catch (error) {
        this.error = errorMessage(error)
        throw error
      } finally {
        this.saving = false
      }
    },

    async updateUser(id, payload) {
      this.saving = true
      this.error = ''
      this.success = ''
      try {
        const { data } = await api.put(`/admin/users/${id}`, payload)
        this.users = this.users.map((user) => (user.id === data.id ? data : user))
        this.success = 'User updated successfully.'
        return data
      } catch (error) {
        this.error = errorMessage(error)
        throw error
      } finally {
        this.saving = false
      }
    },

    async setActive(id, active) {
      this.saving = true
      this.error = ''
      this.success = ''
      try {
        const { data } = await api.patch(`/admin/users/${id}/status`, { active })
        this.users = this.users.map((user) => (user.id === data.id ? data : user))
        this.success = active ? 'User activated successfully.' : 'User deactivated successfully.'
        return data
      } catch (error) {
        this.error = errorMessage(error)
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

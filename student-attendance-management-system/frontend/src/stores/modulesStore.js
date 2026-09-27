import { defineStore } from 'pinia'
import api from '../services/api'

function message(error) {
  return error.response?.data?.error || 'Something went wrong.'
}

export const useModulesStore = defineStore('modules', {
  state: () => ({
    modules: [],
    myModules: [],
    moduleStudents: {},
    studentModules: {},
    loading: false,
    saving: false,
    error: '',
    success: '',
  }),

  actions: {
    async fetchModules() {
      this.loading = true
      this.error = ''
      try {
        const { data } = await api.get('/modules')
        this.modules = data
      } catch (error) {
        this.error = message(error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchLecturerModules() {
      this.loading = true
      this.error = ''
      try {
        const { data } = await api.get('/lecturers/me/modules')
        this.myModules = data
      } catch (error) {
        this.error = message(error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchMyStudentModules() {
      this.loading = true
      this.error = ''
      try {
        const { data } = await api.get('/students/me/modules')
        this.myModules = data
      } catch (error) {
        this.error = message(error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async createModule(payload) {
      this.saving = true
      this.error = ''
      this.success = ''
      try {
        const { data } = await api.post('/modules', payload)
        this.modules = [data, ...this.modules]
        this.success = 'Module created successfully.'
        return data
      } catch (error) {
        this.error = message(error)
        throw error
      } finally {
        this.saving = false
      }
    },

    async updateModule(id, payload) {
      this.saving = true
      this.error = ''
      this.success = ''
      try {
        const { data } = await api.put(`/modules/${id}`, payload)
        this.modules = this.modules.map((module) => (module.id === data.id ? data : module))
        this.success = 'Module updated successfully.'
        return data
      } catch (error) {
        this.error = message(error)
        throw error
      } finally {
        this.saving = false
      }
    },

    async setModuleActive(id, active) {
      this.saving = true
      this.error = ''
      this.success = ''
      try {
        const { data } = await api.patch(`/modules/${id}/status`, { active })
        this.modules = this.modules.map((module) => (module.id === data.id ? data : module))
        this.success = active ? 'Module activated successfully.' : 'Module deactivated successfully.'
        return data
      } catch (error) {
        this.error = message(error)
        throw error
      } finally {
        this.saving = false
      }
    },

    async enrollStudent(payload) {
      this.saving = true
      this.error = ''
      this.success = ''
      try {
        const { data } = await api.post('/enrollments', payload)
        this.success = 'Student enrolled successfully.'
        await this.fetchModuleStudents(payload.moduleId)
        return data
      } catch (error) {
        this.error = message(error)
        throw error
      } finally {
        this.saving = false
      }
    },

    async removeEnrollment(enrollmentId, moduleId) {
      this.saving = true
      this.error = ''
      this.success = ''
      try {
        await api.delete(`/enrollments/${enrollmentId}`)
        this.success = 'Enrollment removed successfully.'
        await this.fetchModuleStudents(moduleId)
      } catch (error) {
        this.error = message(error)
        throw error
      } finally {
        this.saving = false
      }
    },

    async fetchModuleStudents(moduleId) {
      const { data } = await api.get(`/modules/${moduleId}/students`)
      this.moduleStudents = { ...this.moduleStudents, [moduleId]: data }
      return data
    },

    async fetchModulesForStudent(studentId) {
      const { data } = await api.get(`/students/${studentId}/modules`)
      this.studentModules = { ...this.studentModules, [studentId]: data }
      return data
    },

    clearMessages() {
      this.error = ''
      this.success = ''
    },
  },
})

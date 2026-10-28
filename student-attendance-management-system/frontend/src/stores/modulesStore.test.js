import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, expect, it, vi } from 'vitest'
import { useModulesStore } from './modulesStore'
import api from '../services/api'

vi.mock('../services/api', () => ({ default: { delete: vi.fn() } }))
beforeEach(() => { setActivePinia(createPinia()); vi.clearAllMocks() })

it('removes a module from the list only after the API confirms deletion', async () => {
  const store = useModulesStore()
  store.modules = [{ id: 1 }, { id: 2 }]
  store.moduleStudents = { 1: [] }
  let finish
  api.delete.mockReturnValue(new Promise((resolve) => { finish = resolve }))
  const deletion = store.deleteModule(1)
  expect(store.modules).toHaveLength(2)
  expect(store.saving).toBe(true)
  finish({ status: 204 })
  await deletion
  expect(api.delete).toHaveBeenCalledWith('/modules/1')
  expect(store.modules).toEqual([{ id: 2 }])
  expect(store.moduleStudents[1]).toBeUndefined()
  expect(store.success).toBe('Module deleted successfully.')
  expect(store.saving).toBe(false)
})

it('keeps a referenced module and displays the server error', async () => {
  const store = useModulesStore()
  store.modules = [{ id: 1 }]
  const error = { response: { data: { error: 'Module has sessions.' } } }
  api.delete.mockRejectedValue(error)
  await expect(store.deleteModule(1)).rejects.toEqual(error)
  expect(store.modules).toEqual([{ id: 1 }])
  expect(store.error).toBe('Module has sessions.')
  expect(store.success).toBe('')
  expect(store.saving).toBe(false)
})

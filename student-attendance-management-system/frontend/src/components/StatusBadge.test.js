import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import StatusBadge from './StatusBadge.vue'

describe('StatusBadge', () => {
  it('renders the provided label when one is supplied', () => {
    const wrapper = mount(StatusBadge, {
      props: {
        value: 'ACTIVE',
        label: 'Enabled',
      },
    })

    expect(wrapper.text()).toBe('Enabled')
    expect(wrapper.classes()).toContain('text-emerald-700')
  })

  it('falls back to the raw value for unknown statuses', () => {
    const wrapper = mount(StatusBadge, {
      props: {
        value: 'CUSTOM',
      },
    })

    expect(wrapper.text()).toBe('CUSTOM')
    expect(wrapper.classes()).toContain('text-slate-700')
  })
})

import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import EmptyState from './EmptyState.vue'

describe('EmptyState', () => {
  it('renders the title, message, symbol, and optional action slot', () => {
    const wrapper = mount(EmptyState, {
      props: {
        title: 'No sessions',
        message: 'Create a session to begin taking attendance.',
        symbol: '+',
      },
      slots: {
        default: '<button>Create session</button>',
      },
    })

    expect(wrapper.text()).toContain('+')
    expect(wrapper.text()).toContain('No sessions')
    expect(wrapper.text()).toContain('Create a session to begin taking attendance.')
    expect(wrapper.find('button').text()).toBe('Create session')
  })

  it('does not render a message paragraph when no message is provided', () => {
    const wrapper = mount(EmptyState, {
      props: {
        title: 'Nothing here',
      },
    })

    expect(wrapper.find('p').exists()).toBe(false)
  })
})

/**
 * @jest-environment jsdom
 */
import React from 'react'
import { render, screen } from '@testing-library/react'
import '@testing-library/jest-dom'
import { Alert } from '.'

test('loads and displays greeting', async () => {
  render(<Alert widgetConfig={{ message: 'Alert' }} />)

  expect(screen.getAllByText('Alert').length).toBe(1)
})

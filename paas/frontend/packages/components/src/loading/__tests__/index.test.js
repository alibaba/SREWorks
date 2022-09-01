/**
 * @jest-environment jsdom
 */
import React from 'react'
import { render, screen } from '@testing-library/react'
import '@testing-library/jest-dom'
import { Loading } from '..'

test('loads and displays greeting', async () => {
  render(<Loading platformName="Test" />)

  expect(screen.getAllByText('Test').length).toBe(2)
})

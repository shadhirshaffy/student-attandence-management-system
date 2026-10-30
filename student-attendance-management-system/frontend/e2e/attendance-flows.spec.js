import { expect, test } from '@playwright/test'

const users = {
  admin: {
    token: 'admin-token',
    userId: 1,
    name: 'System Admin',
    email: 'admin@example.com',
    role: 'ADMIN',
  },
  lecturer: {
    token: 'lecturer-token',
    userId: 2,
    name: 'Demo Lecturer',
    email: 'lecturer@example.com',
    role: 'LECTURER',
  },
  student: {
    token: 'student-token',
    userId: 3,
    name: 'Demo Student',
    email: 'student@example.com',
    role: 'STUDENT',
  },
}

async function mockApi(page) {
  await page.route('**/api/auth/login', async (route) => {
    const body = route.request().postDataJSON()
    const user = Object.values(users).find((candidate) => candidate.email === body.email)

    if (!user) {
      await route.fulfill({
        status: 401,
        contentType: 'application/json',
        body: JSON.stringify({ error: 'Invalid credentials.' }),
      })
      return
    }

    await route.fulfill({
      contentType: 'application/json',
      body: JSON.stringify(user),
    })
  })

  await page.route('**/api/auth/me', async (route) => {
    const authorization = route.request().headers().authorization || ''
    const user = Object.values(users).find((candidate) => authorization === `Bearer ${candidate.token}`)

    if (!user) {
      await route.fulfill({ status: 401, contentType: 'application/json', body: '{}' })
      return
    }

    await route.fulfill({
      contentType: 'application/json',
      body: JSON.stringify({
        userId: user.userId,
        name: user.name,
        email: user.email,
        role: user.role,
      }),
    })
  })

  await page.route('**/api/admin/users', async (route) => {
    await route.fulfill({
      contentType: 'application/json',
      body: JSON.stringify([
        { id: 1, name: 'System Admin', email: 'admin@example.com', role: 'ADMIN', active: true },
        { id: 2, name: 'Demo Lecturer', email: 'lecturer@example.com', role: 'LECTURER', active: true },
        { id: 3, name: 'Demo Student', email: 'student@example.com', role: 'STUDENT', active: true },
      ]),
    })
  })

  await page.route('**/api/admin/attendance-summary', async (route) => {
    await route.fulfill({
      contentType: 'application/json',
      body: JSON.stringify({
        completedSessions: 8,
        attendanceRecords: 35,
        averageAttendancePercentage: 87,
        lowAttendanceStudentModules: 2,
        lowAttendanceThreshold: 80,
      }),
    })
  })

  await page.route('**/api/students/me/attendance-summary', async (route) => {
    await route.fulfill({
      contentType: 'application/json',
      body: JSON.stringify({
        overallAttendancePercentage: 91,
        overallLowAttendance: false,
        enrolledModules: 2,
        lowAttendanceModules: 0,
        lowAttendanceThreshold: 80,
        modules: [
          {
            moduleId: 10,
            moduleCode: 'CS101',
            moduleName: 'Programming Fundamentals',
            attendancePercentage: 91,
            presentSessions: 10,
            absentSessions: 1,
            lowAttendance: false,
          },
        ],
      }),
    })
  })

  await page.route('**/api/lecturers/me/attendance-summary', async (route) => {
    await route.fulfill({
      contentType: 'application/json',
      body: JSON.stringify({
        assignedModules: 1,
        completedSessions: 3,
        averageAttendancePercentage: 84,
        lowAttendanceStudents: 1,
        modules: [
          {
            moduleId: 10,
            moduleCode: 'CS101',
            moduleName: 'Programming Fundamentals',
            enrolledStudents: 24,
            completedSessions: 3,
            averageAttendancePercentage: 84,
            lowAttendanceStudents: 1,
          },
        ],
      }),
    })
  })

  await page.route('**/api/lecturers/me/modules', async (route) => {
    await route.fulfill({
      contentType: 'application/json',
      body: JSON.stringify([
        {
          id: 10,
          moduleCode: 'CS101',
          moduleName: 'Programming Fundamentals',
          active: true,
        },
      ]),
    })
  })

  await page.route('**/api/sessions', async (route) => {
    await route.fulfill({
      contentType: 'application/json',
      body: JSON.stringify({
        id: 50,
        moduleId: 10,
        moduleCode: 'CS101',
        moduleName: 'Programming Fundamentals',
        sessionDate: '2026-10-10',
        startTime: '09:00',
        endTime: '10:00',
        room: 'Lab 1',
        status: 'SCHEDULED',
        locked: false,
      }),
    })
  })

  await page.route('**/api/sessions/50', async (route) => {
    await route.fulfill({
      contentType: 'application/json',
      body: JSON.stringify({
        id: 50,
        moduleId: 10,
        moduleCode: 'CS101',
        moduleName: 'Programming Fundamentals',
        sessionDate: '2026-10-10',
        startTime: '09:00',
        endTime: '10:00',
        room: 'Lab 1',
        status: 'SCHEDULED',
        locked: false,
      }),
    })
  })
}

async function login(page, role, email, password = 'Password@123') {
  await page.goto('/login')
  await page.getByLabel('Role').selectOption(role)
  await page.getByLabel('Email').fill(email)
  await page.getByRole('textbox', { name: 'Password' }).fill(password)
  await page.getByRole('button', { name: 'Sign in' }).click()
  await expect(page).toHaveURL(new RegExp(`/${role.toLowerCase()}$`))
}

test.beforeEach(async ({ page }) => {
  await mockApi(page)
})

test('admin can sign in and see dashboard statistics', async ({ page }) => {
  await login(page, 'ADMIN', 'admin@example.com')

  await expect(page).toHaveURL(/\/admin$/)
  await expect(page.getByRole('heading', { name: 'Admin dashboard' })).toBeVisible()
  await expect(page.getByText('Total users')).toBeVisible()
  await expect(page.getByText('Completed Sessions', { exact: true })).toBeVisible()
  await expect(page.getByText('Attendance reports')).toBeVisible()
})

test('student is redirected away from admin pages after login', async ({ page }) => {
  await login(page, 'STUDENT', 'student@example.com')
  await page.goto('/admin')

  await expect(page).toHaveURL(/\/student$/)
  await expect(page.getByRole('heading', { name: 'Student dashboard' })).toBeVisible()
  await expect(page.getByText('Programming Fundamentals')).toBeVisible()
})

test('lecturer can create an attendance session', async ({ page }) => {
  await login(page, 'LECTURER', 'lecturer@example.com')
  await page.goto('/lecturer/sessions/create')

  await page.getByLabel('Module').selectOption('10')
  await page.getByLabel('Date').fill('2026-10-10')
  await page.getByLabel('Start time').fill('09:00')
  await page.getByLabel('End time').fill('10:00')
  await page.getByLabel('Room').fill('Lab 1')
  await page.getByRole('button', { name: 'Create session' }).click()

  await expect(page).toHaveURL(/\/lecturer\/sessions\/50$/)
  await expect(page.getByText('Programming Fundamentals')).toBeVisible()
  await expect(page.getByText('SCHEDULED')).toBeVisible()
})


test('admin module CRUD supports cancellation, persistence, and protected deletion', async ({ page }) => {
  let modules = []
  await page.route('**/api/modules', async (route) => {
    if (route.request().method() === 'POST') {
      modules.push({ ...route.request().postDataJSON(), id: 101, lecturerName: 'Demo Lecturer' })
      await route.fulfill({ status: 201, json: modules[0] })
    } else {
      await route.fulfill({ json: modules })
    }
  })
  let blocked = false
  let deletes = 0
  await page.route('**/api/modules/101', async (route) => {
    if (route.request().method() === 'PUT') {
      modules[0] = { ...modules[0], ...route.request().postDataJSON() }
      await route.fulfill({ json: modules[0] })
    } else if (route.request().method() === 'DELETE') {
      deletes++
      if (blocked) {
        await route.fulfill({ status: 409, json: { error: 'Cannot delete a module with enrollments or sessions. Deactivate it instead.' } })
      } else {
        modules = []
        await route.fulfill({ status: 204 })
      }
    }
  })
  // Override the generic users fixture with profile IDs used by module forms.
  await page.route('**/api/admin/users', async (route) => {
    await route.fulfill({ json: [{ id: 2, profileId: 20, name: 'Demo Lecturer', employeeNumber: 'LEC001', role: 'LECTURER', active: true }] })
  })
  await login(page, 'ADMIN', 'admin@example.com')
  await page.goto('/admin/modules')
  await page.getByRole('button', { name: 'Create module' }).click()
  await page.getByLabel('Code', { exact: true }).fill('CRUD101')
  await page.getByLabel('Name', { exact: true }).fill('Test module')
  await page.getByRole('combobox', { name: 'Lecturer' }).selectOption('20')
  await page.getByRole('button', { name: 'Save', exact: true }).click()
  await expect(page.getByText('CRUD101 - Test module', { exact: true })).toBeVisible()
  await page.getByRole('button', { name: 'Edit', exact: true }).click()
  await page.getByLabel('Name', { exact: true }).fill('Updated module')
  await page.getByRole('button', { name: 'Save', exact: true }).click()
  await page.reload()
  await expect(page.getByText('CRUD101 - Updated module', { exact: true })).toBeVisible()
  await page.getByRole('button', { name: 'Delete', exact: true }).click()
  await page.getByRole('button', { name: 'Cancel', exact: true }).click()
  expect(deletes).toBe(0)
  blocked = true
  await page.getByRole('button', { name: 'Delete', exact: true }).click()
  await page.getByRole('button', { name: 'Delete module', exact: true }).click()
  await expect(page.getByText('Cannot delete a module with enrollments or sessions. Deactivate it instead.', { exact: true })).toBeVisible()
  await expect(page.getByText('CRUD101 - Updated module', { exact: true })).toBeVisible()
  blocked = false
  await page.getByRole('button', { name: 'Delete', exact: true }).click()
  await page.getByRole('button', { name: 'Delete module', exact: true }).click()
  await expect(page.getByText('Module deleted successfully.', { exact: true })).toBeVisible()
  await page.reload()
  await expect(page.getByText('No modules yet', { exact: true })).toBeVisible()
})


test('module catalogue search, filters, sorting, and mobile navigation work', async ({ page }) => {
  await page.route('**/api/modules', (route) => route.fulfill({ json: [
    { id: 1, moduleCode: 'CS101', moduleName: 'Zoology', lecturerName: 'Alex Silva', active: true },
    { id: 2, moduleCode: 'CS202', moduleName: 'Algorithms', lecturerName: 'Nila Perera', active: false },
  ] }))
  await login(page, 'ADMIN', 'admin@example.com')
  await page.screenshot({ path: 'test-results/admin-dashboard-desktop.png', fullPage: true })
  await page.goto('/admin/modules')
  const rows = page.locator('tbody tr')
  await expect(rows).toHaveCount(2)
  await page.getByRole('searchbox', { name: 'Search modules' }).fill('Nila')
  await expect(rows).toHaveCount(1)
  await expect(rows.first()).toContainText('Algorithms')
  await page.getByRole('button', { name: 'Reset filters', exact: true }).click()
  await page.getByRole('combobox', { name: 'Status', exact: true }).selectOption('active')
  await expect(rows).toHaveCount(1)
  await expect(rows.first()).toContainText('Zoology')
  await page.getByRole('combobox', { name: 'Status', exact: true }).selectOption('all')
  await page.getByRole('combobox', { name: 'Sort by' }).selectOption('name')
  await expect(rows.first()).toContainText('Algorithms')
  await page.getByRole('searchbox', { name: 'Search modules' }).fill('missing')
  await expect(page.getByRole('heading', { name: 'No matching modules' })).toBeVisible()
  await page.getByRole('searchbox', { name: 'Search modules' }).fill('')
  await page.screenshot({ path: 'test-results/admin-modules-desktop.png', fullPage: true })
  await page.setViewportSize({ width: 390, height: 844 })
  await expect(page.getByRole('button', { name: 'Create module' })).toBeVisible()
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true)
  await page.getByRole('button', { name: 'Open navigation' }).click()
  await page.getByRole('navigation').getByRole('link', { name: 'Dashboard', exact: true }).click()
  await expect(page.getByRole('heading', { name: 'Admin dashboard' })).toBeVisible()
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true)
  await page.screenshot({ path: 'test-results/admin-dashboard-mobile.png', fullPage: true })
})


for (const role of ['LECTURER', 'STUDENT']) {
  test(`${role.toLowerCase()} dashboard presents attendance and works on mobile`, async ({ page }) => {
    const prefix = role.toLowerCase()
    await login(page, role, `${prefix}@example.com`)
    const action = role === 'LECTURER' ? 'Create session' : 'Scan QR'
    await expect(page.getByRole('link', { name: action, exact: true }).last()).toBeVisible()
    await expect(page.getByRole('progressbar')).toBeVisible()
    await page.screenshot({ path: `test-results/${prefix}-dashboard-desktop.png`, fullPage: true })
    await page.setViewportSize({ width: 390, height: 844 })
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true)
    await page.screenshot({ path: `test-results/${prefix}-dashboard-mobile.png`, fullPage: true })
    await page.getByRole('button', { name: 'Open navigation' }).click()
    await page.getByRole('navigation').getByRole('link', { name: 'My Modules', exact: true }).click()
    await expect(page).toHaveURL(new RegExp(`/${prefix}/modules$`))
  })
}


test('lecturer can filter sessions and display a mobile-sized QR code', async ({ page }) => {
  await page.route('**/api/lecturers/me/sessions', (route) => route.fulfill({ json: [
    { id: 50, moduleCode: 'CS101', moduleName: 'Programming', sessionDate: '2026-10-30', startTime: '09:00', endTime: '10:00', room: 'Lab 1', status: 'ACTIVE' },
    { id: 51, moduleCode: 'CS202', moduleName: 'Databases', sessionDate: '2026-10-29', startTime: '09:00', endTime: '10:00', status: 'COMPLETED' },
  ] }))
  await page.route('**/api/sessions/50/qr', (route) => route.fulfill({ json: {
    moduleCode: 'CS101', moduleName: 'Programming Fundamentals', room: 'Lab 1', status: 'ACTIVE',
    tokenExpiry: '2026-10-30T12:00:00+05:30', attendanceUrl: 'http://localhost:5173/student/scan?token=demo',
  } }))
  await login(page, 'LECTURER', 'lecturer@example.com')
  await page.goto('/lecturer/sessions')
  await page.getByRole('searchbox', { name: 'Search sessions' }).fill('Lab 1')
  await expect(page.locator('tbody tr')).toHaveCount(1)
  await page.getByRole('combobox', { name: 'Session status' }).selectOption('COMPLETED')
  await expect(page.getByRole('heading', { name: 'No matching sessions' })).toBeVisible()
  await page.getByRole('button', { name: 'Reset filters' }).click()
  await expect(page.locator('tbody tr')).toHaveCount(2)
  await page.setViewportSize({ width: 390, height: 844 })
  await page.goto('/lecturer/sessions/50/qr')
  await expect(page.getByRole('img', { name: 'Attendance QR code' })).toBeVisible()
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true)
  await page.screenshot({ path: 'test-results/session-qr-mobile.png', fullPage: true })
})

test('student sees token rejection and can submit a replacement token', async ({ page }) => {
  await page.route('**/api/attendance/qr', async (route) => {
    const { token } = route.request().postDataJSON()
    if (token === 'expired') {
      await route.fulfill({ status: 400, json: { error: 'Attendance token has expired.' } })
    } else {
      expect(token).toBe('valid-token')
      await route.fulfill({ json: { moduleCode: 'CS101', moduleName: 'Programming Fundamentals', status: 'PRESENT', markingMethod: 'QR', markedAt: '2026-10-30T09:00:00' } })
    }
  })
  await login(page, 'STUDENT', 'student@example.com')
  await page.setViewportSize({ width: 390, height: 844 })
  await page.goto('/student/scan?token=expired')
  await expect(page.getByRole('alert')).toHaveText('Attendance token has expired.')
  await page.getByRole('textbox', { name: 'Attendance token' }).fill('http://localhost:5173/student/scan?token=valid-token')
  await page.getByRole('button', { name: 'Submit', exact: true }).click()
  await expect(page.getByText('Attendance recorded successfully', { exact: true })).toBeVisible()
  await expect(page.getByRole('link', { name: 'View attendance history' })).toBeVisible()
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true)
  await page.screenshot({ path: 'test-results/student-attendance-success-mobile.png', fullPage: true })
})


test('reports validate dates, show results, and download CSV', async ({ page }) => {
  await page.route('**/api/modules', (route) => route.fulfill({ json: [] }))
  await page.route('**/api/reports/attendance?**', (route) => route.fulfill({ json: { rows: [
    { studentId: 3, moduleId: 10, studentNumber: 'STU001', studentName: 'Demo Student', moduleCode: 'CS101', moduleName: 'Programming', sessionsHeld: 10, sessionsPresent: 9, sessionsAbsent: 1, attendancePercentage: 90 },
  ] } }))
  await page.route('**/api/reports/attendance/csv?**', (route) => route.fulfill({ contentType: 'text/csv', headers: { 'Content-Disposition': 'attachment; filename="attendance-report.csv"' }, body: 'student,percentage\nSTU001,90' }))
  await login(page, 'ADMIN', 'admin@example.com')
  await page.goto('/admin/reports')
  await page.getByLabel('From', { exact: true }).fill('2026-10-30')
  await page.getByLabel('To', { exact: true }).fill('2026-10-01')
  await page.getByRole('button', { name: 'Generate Report', exact: true }).click()
  await expect(page.getByText('The From date must be on or before the To date.')).toBeVisible()
  await page.getByLabel('To', { exact: true }).fill('2026-10-31')
  await page.getByRole('button', { name: 'Generate Report', exact: true }).click()
  await expect(page.getByText('STU001', { exact: true })).toBeVisible()
  await expect(page.getByText('1 student-module records')).toBeVisible()
  const download = page.waitForEvent('download')
  await page.getByRole('button', { name: 'Export CSV', exact: true }).click()
  expect((await download).suggestedFilename()).toBe('attendance-report.csv')
  await page.setViewportSize({ width: 390, height: 844 })
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true)
  await page.screenshot({ path: 'test-results/reports-mobile.png', fullPage: true })
  await page.getByRole('button', { name: 'Reset filters' }).click()
  await expect(page.getByRole('heading', { name: 'Generate a report' })).toBeVisible()
})

test('manual attendance preserves unmarked students and saves only changed selections', async ({ page }) => {
  const attendance = { moduleCode: 'CS101', moduleName: 'Programming', sessionStatus: 'ACTIVE', editable: true, students: [
    { studentId: 3, studentNumber: 'STU001', studentName: 'Demo Student', status: 'PRESENT', markingMethod: 'QR' },
    { studentId: 4, studentNumber: 'STU002', studentName: 'Other Student', status: null },
  ] }
  await page.route('**/api/sessions/50/attendance/manual', async (route) => {
    if (route.request().method() === 'PUT') {
      expect(route.request().postDataJSON()).toEqual({ records: [{ studentId: 4, status: 'PRESENT', reason: 'Scanner unavailable' }] })
      attendance.students[1].status = 'PRESENT'
    }
    await route.fulfill({ json: attendance })
  })
  await login(page, 'LECTURER', 'lecturer@example.com')
  await page.goto('/lecturer/sessions/50/attendance')
  await expect(page.getByText('1 unmarked', { exact: true })).toBeVisible()
  await expect(page.getByText('0 pending changes')).toBeVisible()
  const save = page.getByRole('button', { name: 'Save attendance', exact: true })
  await expect(save).toBeDisabled()
  await page.getByRole('button', { name: 'Mark Other Student STU002 present', exact: true }).click()
  await expect(page.getByText('1 pending changes')).toBeVisible()
  await expect(save).toBeDisabled()
  await page.getByLabel('Reason for manual changes').fill('Scanner unavailable')
  await save.click()
  await expect(page.getByText('Attendance saved successfully.')).toBeVisible()
  await expect(page.getByText('0 pending changes')).toBeVisible()
  await page.setViewportSize({ width: 390, height: 844 })
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true)
  await page.screenshot({ path: 'test-results/manual-attendance-mobile.png', fullPage: true })
})


test('student can find and enroll in an available module and retain it after reload', async ({ page }) => {
  const module = { id: 70, moduleCode: 'EC4201', moduleName: 'Web Development', lecturerName: 'Demo Lecturer', active: true }
  let enrolled = false
  await page.route('**/api/students/me/modules', (route) => route.fulfill({ json: enrolled ? [module] : [] }))
  await page.route('**/api/students/me/available-modules', (route) => route.fulfill({ json: enrolled ? [] : [module] }))
  await page.route('**/api/students/me/modules/70', async (route) => {
    expect(route.request().method()).toBe('POST')
    expect(route.request().postData()).toBe(null)
    enrolled = true
    await route.fulfill({ status: 201, json: { id: 1, moduleId: 70 } })
  })
  await login(page, 'STUDENT', 'student@example.com')
  await page.goto('/student/modules')
  await page.getByRole('searchbox', { name: 'Search available modules' }).fill('missing')
  await expect(page.getByText('No modules match your search.')).toBeVisible()
  await page.getByRole('searchbox', { name: 'Search available modules' }).fill('EC4201')
  await page.getByRole('button', { name: 'Enroll in EC4201', exact: true }).click()
  await expect(page.getByRole('status')).toHaveText('You are now enrolled in this module. You can submit attendance for active sessions.')
  await expect(page.getByRole('heading', { name: 'EC4201 - Web Development', exact: true })).toBeVisible()
  await page.reload()
  await expect(page.getByRole('heading', { name: 'EC4201 - Web Development', exact: true })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Enroll in EC4201', exact: true })).toHaveCount(0)
  await page.setViewportSize({ width: 390, height: 844 })
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true)
  await page.screenshot({ path: 'test-results/student-self-enrollment-mobile.png', fullPage: true })
})


test('lecturer reviews requests with cancellation, failure retry, and saved decisions', async ({ page }) => {
  const requests = [1, 2].map((id) => ({ id, sessionId: 50 + id, moduleCode: 'EC4201', moduleName: 'Digital Electronics', studentName: `Student ${id}`, studentNumber: `STU00${id}`, reason: 'Transport delay', status: 'PENDING', submittedAt: '2026-10-30T10:00:00' }))
  await page.route('**/api/lecturers/me/absence-justifications', (route) => route.fulfill({ json: requests }))
  let patches = 0
  await page.route('**/api/lecturers/me/absence-justifications/*', async (route) => {
    patches++
    if (patches === 1) { await route.fulfill({ status: 500, json: { error: 'Temporary review failure' } }); return }
    const id = Number(route.request().url().split('/').pop())
    Object.assign(requests.find((r) => r.id === id), route.request().postDataJSON(), { reviewedAt: '2026-10-30T12:00:00', reviewedByName: 'Demo Lecturer' })
    await route.fulfill({ json: requests.find((r) => r.id === id) })
  })
  await login(page, 'LECTURER', 'lecturer@example.com')
  await page.getByRole('link', { name: 'Absence Requests', exact: true }).click()
  await expect(page.getByRole('heading', { name: 'Student 1', exact: true })).toBeVisible()
  await page.getByRole('button', { name: 'Approve', exact: true }).first().click()
  await page.getByRole('button', { name: 'Cancel', exact: true }).click()
  expect(patches).toBe(0)
  await page.getByRole('button', { name: 'Approve', exact: true }).first().click()
  await page.getByLabel('Review note (optional)').fill('Reason accepted.')
  await page.getByRole('button', { name: 'Confirm approval' }).click()
  await expect(page.getByRole('dialog').getByText('Temporary review failure')).toBeVisible()
  await expect(page.getByLabel('Review note (optional)')).toHaveValue('Reason accepted.')
  await page.getByRole('button', { name: 'Confirm approval' }).click()
  await expect(page.getByText('Absence request approved successfully.')).toBeVisible()
  await page.getByRole('button', { name: 'Reject', exact: true }).click()
  await page.getByLabel('Review note (optional)').fill('Insufficient explanation.')
  await page.getByRole('button', { name: 'Confirm rejection' }).click()
  await expect(page.getByText('Absence request rejected successfully.')).toBeVisible()
  await page.getByRole('combobox', { name: 'Review status' }).selectOption('ALL')
  await expect(page.getByText('Reason accepted.', { exact: true })).toBeVisible()
  await expect(page.getByText('Insufficient explanation.', { exact: true })).toBeVisible()
  await page.reload()
  await page.getByRole('combobox', { name: 'Review status' }).selectOption('ALL')
  await expect(page.getByText('Reason accepted.', { exact: true })).toBeVisible()
  await page.setViewportSize({ width: 390, height: 844 })
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true)
  await page.screenshot({ path: 'test-results/absence-requests-mobile.png', fullPage: true })
})

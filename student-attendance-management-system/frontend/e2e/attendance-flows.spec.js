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
  await expect(page.getByText('Completed Sessions')).toBeVisible()
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

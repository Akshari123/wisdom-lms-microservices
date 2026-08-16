import { useEffect, useMemo, useState } from 'react';
import { api, getApiBaseUrl } from './api';

const tabs = [
  { id: 'dashboard', label: 'Dashboard' },
  { id: 'students', label: 'Students' },
  { id: 'teachers', label: 'Teachers' },
  { id: 'classes', label: 'Classes' },
  { id: 'payments', label: 'Payments' },
  { id: 'auth', label: 'Authentication' }
];

function formatMoney(value) {
  const amount = Number(value || 0);
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 2
  }).format(Number.isFinite(amount) ? amount : 0);
}

function formatDateTime(value) {
  if (!value) return 'Not set';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString();
}

function formatDate(value) {
  if (!value) return 'Not set';
  return value;
}

function toNumberOrNull(value) {
  if (value === '' || value === null || value === undefined) return null;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : null;
}

function buildStats(students, teachers, classes, payments) {
  const totalRevenue = payments.reduce((sum, payment) => {
    const amount = Number(payment.amount || 0);
    return sum + (Number.isFinite(amount) ? amount : 0);
  }, 0);

  return [
    { label: 'Students', value: students.length },
    { label: 'Teachers', value: teachers.length },
    { label: 'Classes', value: classes.length },
    { label: 'Payments', value: payments.length },
    { label: 'Revenue', value: formatMoney(totalRevenue) }
  ];
}

function useLoadState() {
  return useState({
    students: [],
    teachers: [],
    classes: [],
    payments: [],
    authPing: 'Unknown',
    loading: true,
    error: null
  });
}

function App() {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [state, setState] = useLoadState();
  const [globalMessage, setGlobalMessage] = useState('');

  const loadAll = async () => {
    setState((current) => ({ ...current, loading: true, error: null }));

    try {
      const [students, teachers, classes, payments, authPing] = await Promise.all([
        api.students.list(),
        api.teachers.list(),
        api.classes.list(),
        api.payments.history(),
        api.auth.test().catch(() => 'Auth service unavailable')
      ]);

      setState({
        students: Array.isArray(students) ? students : [],
        teachers: Array.isArray(teachers) ? teachers : [],
        classes: Array.isArray(classes) ? classes : [],
        payments: Array.isArray(payments) ? payments : [],
        authPing: typeof authPing === 'string' ? authPing : 'Auth service ready',
        loading: false,
        error: null
      });
    } catch (error) {
      setState((current) => ({
        ...current,
        loading: false,
        error: error.message || 'Failed to load dashboard data'
      }));
    }
  };

  useEffect(() => {
    loadAll();
  }, []);

  const stats = useMemo(
    () => buildStats(state.students, state.teachers, state.classes, state.payments),
    [state.students, state.teachers, state.classes, state.payments]
  );

  const actions = {
    async refresh(sectionMessage = 'Data refreshed') {
      await loadAll();
      setGlobalMessage(sectionMessage);
    },
    async saveStudent(student, id) {
      if (id) {
        await api.students.update(id, student);
        setGlobalMessage('Student updated');
      } else {
        await api.students.create(student);
        setGlobalMessage('Student created');
      }
      await loadAll();
    },
    async deleteStudent(id) {
      await api.students.remove(id);
      setGlobalMessage('Student deleted');
      await loadAll();
    },
    async saveTeacher(teacher, id) {
      if (id) {
        await api.teachers.update(id, teacher);
        setGlobalMessage('Teacher updated');
      } else {
        await api.teachers.create(teacher);
        setGlobalMessage('Teacher created');
      }
      await loadAll();
    },
    async deleteTeacher(id) {
      await api.teachers.remove(id);
      setGlobalMessage('Teacher deleted');
      await loadAll();
    },
    async saveClass(educationClass, id) {
      if (id) {
        await api.classes.update(id, educationClass);
        setGlobalMessage('Class updated');
      } else {
        await api.classes.create(educationClass);
        setGlobalMessage('Class created');
      }
      await loadAll();
    },
    async deleteClass(id) {
      await api.classes.remove(id);
      setGlobalMessage('Class deleted');
      await loadAll();
    },
    async processPayment(payment) {
      await api.payments.process(payment);
      setGlobalMessage('Payment processed');
      await loadAll();
    },
    async updatePaymentStatus(id, status) {
      await api.payments.updateStatus(id, { status });
      setGlobalMessage('Payment status updated');
      await loadAll();
    },
    async login(payload) {
      const result = await api.auth.login(payload);
      setGlobalMessage(typeof result === 'string' ? result : 'Logged in');
      await loadAll();
    },
    async register(payload) {
      const result = await api.auth.register(payload);
      setGlobalMessage(
        typeof result === 'string'
          ? result
          : `Registered ${result?.username || 'user'}`
      );
      await loadAll();
    }
  };

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand-block">
          <div className="brand-mark">W</div>
          <div>
            <div className="brand-title">Wisdom LMS</div>
            <div className="brand-subtitle">Microservices console</div>
          </div>
        </div>

        <nav className="nav-list">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              className={tab.id === activeTab ? 'nav-item active' : 'nav-item'}
              onClick={() => setActiveTab(tab.id)}
            >
              {tab.label}
            </button>
          ))}
        </nav>

        <div className="sidebar-card">
          <div className="sidebar-card-label">Gateway</div>
          <div className="sidebar-card-value">{getApiBaseUrl()}</div>
          <p>
            This frontend talks to the gateway, which forwards requests to the
            backend services with the required API key headers.
          </p>
        </div>
      </aside>

      <main className="main-panel">
        <header className="hero">
          <div>
            <p className="eyebrow">Education management</p>
            <h1>Run the full LMS from one browser.</h1>
            <p className="hero-copy">
              Manage students, teachers, classes, and payments through the live
              gateway-backed microservices.
            </p>
          </div>
          <div className="hero-actions">
            <button className="primary-button" onClick={() => actions.refresh()}>
              Refresh all data
            </button>
            <div className="status-chip">{state.authPing}</div>
          </div>
        </header>

        {globalMessage ? <div className="banner success">{globalMessage}</div> : null}
        {state.error ? <div className="banner error">{state.error}</div> : null}

        {activeTab === 'dashboard' ? (
          <DashboardSection
            stats={stats}
            students={state.students}
            teachers={state.teachers}
            classes={state.classes}
            payments={state.payments}
            loading={state.loading}
          />
        ) : null}

        {activeTab === 'students' ? (
          <StudentsSection
            loading={state.loading}
            students={state.students}
            onSave={actions.saveStudent}
            onDelete={actions.deleteStudent}
          />
        ) : null}

        {activeTab === 'teachers' ? (
          <TeachersSection
            loading={state.loading}
            teachers={state.teachers}
            onSave={actions.saveTeacher}
            onDelete={actions.deleteTeacher}
          />
        ) : null}

        {activeTab === 'classes' ? (
          <ClassesSection
            loading={state.loading}
            classes={state.classes}
            onSave={actions.saveClass}
            onDelete={actions.deleteClass}
          />
        ) : null}

        {activeTab === 'payments' ? (
          <PaymentsSection
            loading={state.loading}
            payments={state.payments}
            onProcess={actions.processPayment}
            onUpdateStatus={actions.updatePaymentStatus}
          />
        ) : null}

        {activeTab === 'auth' ? (
          <AuthSection loading={state.loading} onLogin={actions.login} onRegister={actions.register} />
        ) : null}
      </main>
    </div>
  );
}

function DashboardSection({ stats, students, teachers, classes, payments, loading }) {
  return (
    <section className="section-grid">
      <div className="stats-grid">
        {stats.map((stat) => (
          <article key={stat.label} className="stat-card">
            <span>{stat.label}</span>
            <strong>{stat.value}</strong>
          </article>
        ))}
      </div>

      <div className="panel">
        <div className="panel-header">
          <div>
            <h2>Latest records</h2>
            <p>Quick visibility into what is stored in each service.</p>
          </div>
        </div>
        {loading ? (
          <div className="loading-state">Loading dashboard data...</div>
        ) : (
          <div className="dashboard-columns">
            <MiniList title="Students" items={students.slice(0, 3)} renderItem={(student) => `${student.firstName || 'No first name'} ${student.lastName || ''} | ${student.email || 'No email'}`} />
            <MiniList title="Teachers" items={teachers.slice(0, 3)} renderItem={(teacher) => `${teacher.name || 'No name'} | ${teacher.subject || 'No subject'}`} />
            <MiniList title="Classes" items={classes.slice(0, 3)} renderItem={(item) => `${item.subject || 'No subject'} | ${formatDate(item.classDate)} ${item.classTime || ''}`} />
            <MiniList title="Payments" items={payments.slice(0, 3)} renderItem={(item) => `${item.paymentMethod || 'Method'} | ${formatMoney(item.amount)}`} />
          </div>
        )}
      </div>
    </section>
  );
}

function MiniList({ title, items, renderItem }) {
  return (
    <div className="mini-list">
      <h3>{title}</h3>
      {items.length === 0 ? <p className="empty-copy">No {title.toLowerCase()} yet.</p> : null}
      {items.map((item) => (
        <div key={item.id} className="mini-list-item">
          {renderItem(item)}
        </div>
      ))}
    </div>
  );
}

function StudentsSection({ loading, students, onSave, onDelete }) {
  const emptyForm = { firstName: '', lastName: '', email: '' };
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState('');
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  const submit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setFormError('');
    try {
      await onSave(form, editingId);
      setForm(emptyForm);
      setEditingId('');
    } catch (error) {
      setFormError(error.message || 'Failed to save student');
    } finally {
      setSaving(false);
    }
  };

  return (
    <section className="panel">
      <SectionHeader
        title="Students"
        description="Create, update, and delete student records."
      />

      <FormGrid onSubmit={submit} formError={formError}>
        <InputField label="First name" value={form.firstName} onChange={(value) => setForm({ ...form, firstName: value })} />
        <InputField label="Last name" value={form.lastName} onChange={(value) => setForm({ ...form, lastName: value })} />
        <InputField label="Email" type="email" value={form.email} onChange={(value) => setForm({ ...form, email: value })} />
        <div className="form-actions">
          <button className="primary-button" disabled={saving}>
            {editingId ? 'Update student' : 'Create student'}
          </button>
          <button
            type="button"
            className="secondary-button"
            onClick={() => {
              setForm(emptyForm);
              setEditingId('');
              setFormError('');
            }}
          >
            Reset
          </button>
        </div>
      </FormGrid>

      <RecordTable
        loading={loading}
        emptyMessage="No students found."
        columns={['Name', 'Email', 'Actions']}
        rows={students}
        renderRow={(student) => (
          <>
            <td>{`${student.firstName || ''} ${student.lastName || ''}`.trim() || 'Unnamed student'}</td>
            <td>{student.email || 'No email'}</td>
            <td className="action-cell">
              <button
                className="text-button"
                onClick={() => {
                  setEditingId(student.id);
                  setForm({
                    firstName: student.firstName || '',
                    lastName: student.lastName || '',
                    email: student.email || ''
                  });
                }}
              >
                Edit
              </button>
              <button className="text-button danger" onClick={() => onDelete(student.id)}>
                Delete
              </button>
            </td>
          </>
        )}
      />
    </section>
  );
}

function TeachersSection({ loading, teachers, onSave, onDelete }) {
  const emptyForm = { name: '', email: '', phone: '', subject: '' };
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState('');
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  const submit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setFormError('');
    try {
      await onSave(form, editingId);
      setForm(emptyForm);
      setEditingId('');
    } catch (error) {
      setFormError(error.message || 'Failed to save teacher');
    } finally {
      setSaving(false);
    }
  };

  return (
    <section className="panel">
      <SectionHeader
        title="Teachers"
        description="Maintain teacher records and their assigned subjects."
      />

      <FormGrid onSubmit={submit} formError={formError}>
        <InputField label="Name" value={form.name} onChange={(value) => setForm({ ...form, name: value })} />
        <InputField label="Email" type="email" value={form.email} onChange={(value) => setForm({ ...form, email: value })} />
        <InputField label="Phone" value={form.phone} onChange={(value) => setForm({ ...form, phone: value })} />
        <InputField label="Subject" value={form.subject} onChange={(value) => setForm({ ...form, subject: value })} />
        <div className="form-actions">
          <button className="primary-button" disabled={saving}>
            {editingId ? 'Update teacher' : 'Create teacher'}
          </button>
          <button
            type="button"
            className="secondary-button"
            onClick={() => {
              setForm(emptyForm);
              setEditingId('');
              setFormError('');
            }}
          >
            Reset
          </button>
        </div>
      </FormGrid>

      <RecordTable
        loading={loading}
        emptyMessage="No teachers found."
        columns={['Name', 'Subject', 'Actions']}
        rows={teachers}
        renderRow={(teacher) => (
          <>
            <td>{teacher.name || 'Unnamed teacher'}</td>
            <td>{teacher.subject || 'No subject'}</td>
            <td className="action-cell">
              <button
                className="text-button"
                onClick={() => {
                  setEditingId(teacher.id);
                  setForm({
                    name: teacher.name || '',
                    email: teacher.email || '',
                    phone: teacher.phone || '',
                    subject: teacher.subject || ''
                  });
                }}
              >
                Edit
              </button>
              <button className="text-button danger" onClick={() => onDelete(teacher.id)}>
                Delete
              </button>
            </td>
          </>
        )}
      />
    </section>
  );
}

function ClassesSection({ loading, classes, onSave, onDelete }) {
  const emptyForm = {
    subject: '',
    teacherId: '',
    year: '',
    classDate: '',
    classTime: ''
  };
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState('');
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  const submit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setFormError('');
    try {
      await onSave(
        {
          subject: form.subject,
          teacherId: toNumberOrNull(form.teacherId),
          year: form.year,
          classDate: form.classDate || null,
          classTime: form.classTime || null
        },
        editingId
      );
      setForm(emptyForm);
      setEditingId('');
    } catch (error) {
      setFormError(error.message || 'Failed to save class');
    } finally {
      setSaving(false);
    }
  };

  return (
    <section className="panel">
      <SectionHeader
        title="Classes"
        description="Track scheduled classes, teachers, and time slots."
      />

      <FormGrid onSubmit={submit} formError={formError}>
        <InputField label="Subject" value={form.subject} onChange={(value) => setForm({ ...form, subject: value })} />
        <InputField label="Teacher ID" value={form.teacherId} onChange={(value) => setForm({ ...form, teacherId: value })} />
        <InputField label="Year" value={form.year} onChange={(value) => setForm({ ...form, year: value })} />
        <InputField label="Class date" type="date" value={form.classDate} onChange={(value) => setForm({ ...form, classDate: value })} />
        <InputField label="Class time" type="time" value={form.classTime} onChange={(value) => setForm({ ...form, classTime: value })} />
        <div className="form-actions">
          <button className="primary-button" disabled={saving}>
            {editingId ? 'Update class' : 'Create class'}
          </button>
          <button
            type="button"
            className="secondary-button"
            onClick={() => {
              setForm(emptyForm);
              setEditingId('');
              setFormError('');
            }}
          >
            Reset
          </button>
        </div>
      </FormGrid>

      <RecordTable
        loading={loading}
        emptyMessage="No classes found."
        columns={['Subject', 'Teacher ID', 'Schedule', 'Actions']}
        rows={classes}
        renderRow={(item) => (
          <>
            <td>{item.subject || 'No subject'}</td>
            <td>{item.teacherId ?? 'Not set'}</td>
            <td>{`${formatDate(item.classDate)} ${item.classTime || ''}`.trim()}</td>
            <td className="action-cell">
              <button
                className="text-button"
                onClick={() => {
                  setEditingId(item.id);
                  setForm({
                    subject: item.subject || '',
                    teacherId: item.teacherId ?? '',
                    year: item.year || '',
                    classDate: item.classDate || '',
                    classTime: item.classTime || ''
                  });
                }}
              >
                Edit
              </button>
              <button className="text-button danger" onClick={() => onDelete(item.id)}>
                Delete
              </button>
            </td>
          </>
        )}
      />
    </section>
  );
}

function PaymentsSection({ loading, payments, onProcess, onUpdateStatus }) {
  const emptyForm = { userId: '', orderId: '', amount: '', paymentMethod: '' };
  const [form, setForm] = useState(emptyForm);
  const [processing, setProcessing] = useState(false);
  const [formError, setFormError] = useState('');
  const [statusDrafts, setStatusDrafts] = useState({});

  const submit = async (event) => {
    event.preventDefault();
    setProcessing(true);
    setFormError('');
    try {
      await onProcess({
        userId: toNumberOrNull(form.userId),
        orderId: toNumberOrNull(form.orderId),
        amount: form.amount === '' ? null : Number(form.amount),
        paymentMethod: form.paymentMethod
      });
      setForm(emptyForm);
    } catch (error) {
      setFormError(error.message || 'Failed to process payment');
    } finally {
      setProcessing(false);
    }
  };

  return (
    <section className="panel">
      <SectionHeader
        title="Payments"
        description="Create payment records and update their status."
      />

      <FormGrid onSubmit={submit} formError={formError}>
        <InputField label="User ID" value={form.userId} onChange={(value) => setForm({ ...form, userId: value })} />
        <InputField label="Order ID" value={form.orderId} onChange={(value) => setForm({ ...form, orderId: value })} />
        <InputField label="Amount" type="number" step="0.01" value={form.amount} onChange={(value) => setForm({ ...form, amount: value })} />
        <InputField label="Payment method" value={form.paymentMethod} onChange={(value) => setForm({ ...form, paymentMethod: value })} />
        <div className="form-actions">
          <button className="primary-button" disabled={processing}>
            Process payment
          </button>
          <button
            type="button"
            className="secondary-button"
            onClick={() => {
              setForm(emptyForm);
              setFormError('');
            }}
          >
            Reset
          </button>
        </div>
      </FormGrid>

      <RecordTable
        loading={loading}
        emptyMessage="No payments found."
        columns={['User', 'Order', 'Amount', 'Status', 'Created', 'Actions']}
        rows={payments}
        renderRow={(payment) => (
          <>
            <td>{payment.userId ?? 'Not set'}</td>
            <td>{payment.orderId ?? 'Not set'}</td>
            <td>{formatMoney(payment.amount)}</td>
            <td>{payment.status || 'Unknown'}</td>
            <td>{formatDateTime(payment.createdAt)}</td>
            <td className="action-cell">
              <select
                className="status-select"
                value={statusDrafts[payment.id] || payment.status || 'PENDING'}
                onChange={(event) =>
                  setStatusDrafts({
                    ...statusDrafts,
                    [payment.id]: event.target.value
                  })
                }
              >
                <option value="PENDING">PENDING</option>
                <option value="COMPLETED">COMPLETED</option>
                <option value="FAILED">FAILED</option>
              </select>
              <button
                className="text-button"
                onClick={() => onUpdateStatus(payment.id, statusDrafts[payment.id] || payment.status || 'PENDING')}
              >
                Save
              </button>
            </td>
          </>
        )}
      />
    </section>
  );
}

function AuthSection({ loading, onLogin, onRegister }) {
  const loginEmpty = { username: '', password: '' };
  const registerEmpty = { username: '', password: '', role: 'STUDENT' };
  const [mode, setMode] = useState('login');
  const [loginForm, setLoginForm] = useState(loginEmpty);
  const [registerForm, setRegisterForm] = useState(registerEmpty);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState('');

  const submitLogin = async (event) => {
    event.preventDefault();
    setBusy(true);
    setMessage('');
    try {
      await onLogin(loginForm);
      setMessage('Login request completed.');
    } catch (error) {
      setMessage(error.message || 'Login failed');
    } finally {
      setBusy(false);
    }
  };

  const submitRegister = async (event) => {
    event.preventDefault();
    setBusy(true);
    setMessage('');
    try {
      await onRegister(registerForm);
      setMessage('Registration request completed.');
    } catch (error) {
      setMessage(error.message || 'Registration failed');
    } finally {
      setBusy(false);
    }
  };

  return (
    <section className="panel">
      <SectionHeader
        title="Authentication"
        description="Call the auth service to register or log in users."
      />

      <div className="toggle-row">
        <button className={mode === 'login' ? 'toggle active' : 'toggle'} onClick={() => setMode('login')}>
          Login
        </button>
        <button className={mode === 'register' ? 'toggle active' : 'toggle'} onClick={() => setMode('register')}>
          Register
        </button>
      </div>

      {mode === 'login' ? (
        <FormGrid onSubmit={submitLogin} formError="">
          <InputField label="Username" value={loginForm.username} onChange={(value) => setLoginForm({ ...loginForm, username: value })} />
          <InputField label="Password" type="password" value={loginForm.password} onChange={(value) => setLoginForm({ ...loginForm, password: value })} />
          <div className="form-actions">
            <button className="primary-button" disabled={busy || loading}>
              Log in
            </button>
          </div>
        </FormGrid>
      ) : (
        <FormGrid onSubmit={submitRegister} formError="">
          <InputField label="Username" value={registerForm.username} onChange={(value) => setRegisterForm({ ...registerForm, username: value })} />
          <InputField label="Password" type="password" value={registerForm.password} onChange={(value) => setRegisterForm({ ...registerForm, password: value })} />
          <label className="field">
            <span>Role</span>
            <select
              value={registerForm.role}
              onChange={(event) => setRegisterForm({ ...registerForm, role: event.target.value })}
            >
              <option value="STUDENT">STUDENT</option>
              <option value="TEACHER">TEACHER</option>
              <option value="ADMIN">ADMIN</option>
            </select>
          </label>
          <div className="form-actions">
            <button className="primary-button" disabled={busy || loading}>
              Register
            </button>
          </div>
        </FormGrid>
      )}

      {message ? <div className="banner info">{message}</div> : null}
      <p className="hint-copy">
        Auth responses are plain text in the current backend, so this panel shows the live response message directly.
      </p>
    </section>
  );
}

function SectionHeader({ title, description }) {
  return (
    <div className="panel-header">
      <div>
        <h2>{title}</h2>
        <p>{description}</p>
      </div>
    </div>
  );
}

function FormGrid({ children, onSubmit, formError }) {
  return (
    <form className="form-grid" onSubmit={onSubmit}>
      {children}
      {formError ? <div className="banner error">{formError}</div> : null}
    </form>
  );
}

function InputField({ label, value, onChange, type = 'text', step }) {
  return (
    <label className="field">
      <span>{label}</span>
      <input
        type={type}
        step={step}
        value={value}
        onChange={(event) => onChange(event.target.value)}
      />
    </label>
  );
}

function RecordTable({ loading, emptyMessage, columns, rows, renderRow }) {
  if (loading) {
    return <div className="loading-state">Loading data...</div>;
  }

  if (!rows.length) {
    return <div className="empty-state">{emptyMessage}</div>;
  }

  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            {columns.map((column) => (
              <th key={column}>{column}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={row.id}>{renderRow(row)}</tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default App;

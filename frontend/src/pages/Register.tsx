import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import { useAuth } from '../contexts/AuthContext';

interface FormErrors {
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  password?: string;
  confirmPassword?: string;
}

const Register: React.FC = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
  });
  const [errors, setErrors] = useState<FormErrors>({});
  const [apiError, setApiError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (field: keyof typeof formData) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData((prev) => ({ ...prev, [field]: e.target.value }));
    setErrors((prev) => ({ ...prev, [field]: undefined }));
  };

  const validate = (): boolean => {
    const newErrors: FormErrors = {};
    if (!formData.firstName.trim()) newErrors.firstName = 'First name is required';
    if (!formData.lastName.trim()) newErrors.lastName = 'Last name is required';
    if (!formData.email.trim()) newErrors.email = 'Email is required';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) newErrors.email = 'Enter a valid email';
    if (formData.phone && !/^\+?[\d\s-]{7,15}$/.test(formData.phone)) newErrors.phone = 'Enter a valid phone number';
    if (!formData.password) newErrors.password = 'Password is required';
    else if (formData.password.length < 8) newErrors.password = 'Password must be at least 8 characters';
    if (!formData.confirmPassword) newErrors.confirmPassword = 'Please confirm your password';
    else if (formData.password !== formData.confirmPassword) newErrors.confirmPassword = 'Passwords do not match';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setApiError('');
    if (!validate()) return;
    try {
      setLoading(true);
      await authService.register({
        firstName: formData.firstName.trim(),
        lastName: formData.lastName.trim(),
        email: formData.email.trim(),
        password: formData.password,
        phone: formData.phone.trim() || undefined,
      });
      await login(formData.email.trim(), formData.password);
      navigate('/', { replace: true });
    } catch (error: unknown) {
      const msg = error instanceof Error ? error.message : 'Registration failed. Email may already be in use.';
      setApiError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: 'calc(100vh - 70px)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '40px 24px',
      background: '#f8f8f8',
    }}>
      <div style={{
        width: '100%',
        maxWidth: '460px',
        background: 'white',
        borderRadius: '16px',
        padding: '40px',
        boxShadow: '0 4px 24px rgba(0,0,0,0.08)',
      }}>
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <div style={{ marginBottom: '8px' }}>
            <span style={{ fontSize: '28px', fontWeight: 800, color: '#1A1A2E' }}>Swift</span>
            <span style={{ fontSize: '28px', fontWeight: 800, color: '#FF6B35' }}>Bite</span>
          </div>
          <h2 style={{ fontSize: '22px', fontWeight: 700, color: '#1A1A2E', marginBottom: '6px' }}>Create your account</h2>
          <p style={{ fontSize: '14px', color: '#666' }}>Join SwiftBite and start ordering</p>
        </div>

        {apiError && (
          <div style={{ background: '#FDEDEC', border: '1px solid #F1948A', borderRadius: '8px', padding: '12px 16px', marginBottom: '20px', color: '#c0392b', fontSize: '14px' }}>
            {apiError}
          </div>
        )}

        <form onSubmit={handleSubmit} noValidate>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div className="form-group" style={{ margin: 0 }}>
              <label>First name</label>
              <input
                type="text"
                value={formData.firstName}
                onChange={handleChange('firstName')}
                className={errors.firstName ? 'error' : ''}
                placeholder="John"
                autoComplete="given-name"
              />
              {errors.firstName && <span className="form-error">{errors.firstName}</span>}
            </div>
            <div className="form-group" style={{ margin: 0 }}>
              <label>Last name</label>
              <input
                type="text"
                value={formData.lastName}
                onChange={handleChange('lastName')}
                className={errors.lastName ? 'error' : ''}
                placeholder="Doe"
                autoComplete="family-name"
              />
              {errors.lastName && <span className="form-error">{errors.lastName}</span>}
            </div>
          </div>
          <div style={{ marginBottom: '16px' }} />

          <div className="form-group">
            <label>Email address</label>
            <input
              type="email"
              value={formData.email}
              onChange={handleChange('email')}
              className={errors.email ? 'error' : ''}
              placeholder="you@example.com"
              autoComplete="email"
            />
            {errors.email && <span className="form-error">{errors.email}</span>}
          </div>

          <div className="form-group">
            <label>Phone number <span style={{ color: '#999', fontWeight: 400 }}>(optional)</span></label>
            <input
              type="tel"
              value={formData.phone}
              onChange={handleChange('phone')}
              className={errors.phone ? 'error' : ''}
              placeholder="+91 98765 43210"
              autoComplete="tel"
            />
            {errors.phone && <span className="form-error">{errors.phone}</span>}
          </div>

          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              value={formData.password}
              onChange={handleChange('password')}
              className={errors.password ? 'error' : ''}
              placeholder="Min. 8 characters"
              autoComplete="new-password"
            />
            {errors.password && <span className="form-error">{errors.password}</span>}
          </div>

          <div className="form-group">
            <label>Confirm password</label>
            <input
              type="password"
              value={formData.confirmPassword}
              onChange={handleChange('confirmPassword')}
              className={errors.confirmPassword ? 'error' : ''}
              placeholder="Re-enter your password"
              autoComplete="new-password"
            />
            {errors.confirmPassword && <span className="form-error">{errors.confirmPassword}</span>}
          </div>

          <button
            type="submit"
            className="btn-primary"
            disabled={loading}
            style={{ width: '100%', justifyContent: 'center', marginTop: '8px', padding: '14px' }}
          >
            {loading ? 'Creating account...' : 'Create Account'}
          </button>
        </form>

        <p style={{ textAlign: 'center', marginTop: '24px', fontSize: '14px', color: '#666' }}>
          Already have an account?{' '}
          <Link to="/login" style={{ color: '#FF6B35', fontWeight: 600, textDecoration: 'none' }}>
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
};

export default Register;

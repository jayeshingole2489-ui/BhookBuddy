import React, { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { authService } from '../services/authService';
import { useToast } from '../App';

const Profile: React.FC = () => {
  const { user, login } = useAuth();
  const { showToast } = useToast();

  const [formData, setFormData] = useState({
    firstName: user?.firstName ?? '',
    lastName: user?.lastName ?? '',
    phone: user?.phone ?? '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  const validate = (): boolean => {
    const errs: Record<string, string> = {};
    if (!formData.firstName.trim()) errs.firstName = 'First name is required';
    if (!formData.lastName.trim()) errs.lastName = 'Last name is required';
    if (formData.phone && !/^\+?[\d\s-]{7,15}$/.test(formData.phone)) errs.phone = 'Enter a valid phone number';
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    try {
      setSaving(true);
      await authService.updateProfile({
        firstName: formData.firstName.trim(),
        lastName: formData.lastName.trim(),
        phone: formData.phone.trim() || undefined,
      });
      showToast('Profile updated successfully!', 'success');
      // Refresh user in localStorage
      const stored = localStorage.getItem('user');
      if (stored) {
        const u = JSON.parse(stored);
        u.firstName = formData.firstName.trim();
        u.lastName = formData.lastName.trim();
        u.phone = formData.phone.trim() || undefined;
        localStorage.setItem('user', JSON.stringify(u));
      }
    } catch (error: unknown) {
      const msg = error instanceof Error ? error.message : 'Failed to update profile';
      showToast(msg, 'error');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div style={{ maxWidth: '560px', margin: '0 auto', padding: '40px 24px' }}>
      <h1 style={{ fontSize: '28px', fontWeight: 800, color: '#1A1A2E', marginBottom: '32px' }}>My Profile</h1>

      {/* Avatar + account info */}
      <div style={{
        background: 'linear-gradient(135deg, #1A1A2E, #FF6B35)',
        borderRadius: '16px',
        padding: '28px',
        color: 'white',
        marginBottom: '28px',
        display: 'flex',
        alignItems: 'center',
        gap: '20px',
      }}>
        <div style={{
          width: '72px', height: '72px', borderRadius: '50%',
          background: 'rgba(255,255,255,0.2)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: '30px', fontWeight: 700, flexShrink: 0,
        }}>
          {user?.firstName?.[0]?.toUpperCase() ?? 'U'}
        </div>
        <div>
          <h2 style={{ fontSize: '20px', fontWeight: 700, marginBottom: '4px' }}>
            {user?.firstName} {user?.lastName}
          </h2>
          <p style={{ fontSize: '14px', opacity: 0.85, marginBottom: '4px' }}>{user?.email}</p>
          <span style={{
            display: 'inline-block',
            background: 'rgba(255,255,255,0.2)',
            padding: '3px 10px', borderRadius: '12px', fontSize: '12px', fontWeight: 600,
          }}>
            {user?.role === 'ROLE_ADMIN' ? '🛡 Admin' : '👤 Customer'}
          </span>
        </div>
      </div>

      {/* Edit form */}
      <div style={{ background: 'white', borderRadius: '16px', padding: '28px', boxShadow: '0 2px 12px rgba(0,0,0,0.06)' }}>
        <h2 style={{ fontSize: '18px', fontWeight: 700, marginBottom: '24px', color: '#1A1A2E' }}>Edit Profile</h2>

        <form onSubmit={handleSave} noValidate>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div className="form-group" style={{ margin: 0 }}>
              <label>First name</label>
              <input
                type="text"
                value={formData.firstName}
                onChange={(e) => { setFormData((p) => ({ ...p, firstName: e.target.value })); setErrors((p) => ({ ...p, firstName: '' })); }}
                className={errors.firstName ? 'error' : ''}
                placeholder="John"
              />
              {errors.firstName && <span className="form-error">{errors.firstName}</span>}
            </div>
            <div className="form-group" style={{ margin: 0 }}>
              <label>Last name</label>
              <input
                type="text"
                value={formData.lastName}
                onChange={(e) => { setFormData((p) => ({ ...p, lastName: e.target.value })); setErrors((p) => ({ ...p, lastName: '' })); }}
                className={errors.lastName ? 'error' : ''}
                placeholder="Doe"
              />
              {errors.lastName && <span className="form-error">{errors.lastName}</span>}
            </div>
          </div>
          <div style={{ marginBottom: '16px' }} />
          <div className="form-group">
            <label>Email address</label>
            <input type="email" value={user?.email ?? ''} disabled style={{ background: '#f8f8f8', color: '#999', cursor: 'not-allowed' }} />
            <span style={{ fontSize: '12px', color: '#999' }}>Email cannot be changed</span>
          </div>
          <div className="form-group">
            <label>Phone number <span style={{ color: '#999', fontWeight: 400 }}>(optional)</span></label>
            <input
              type="tel"
              value={formData.phone}
              onChange={(e) => { setFormData((p) => ({ ...p, phone: e.target.value })); setErrors((p) => ({ ...p, phone: '' })); }}
              className={errors.phone ? 'error' : ''}
              placeholder="+91 98765 43210"
            />
            {errors.phone && <span className="form-error">{errors.phone}</span>}
          </div>

          <button
            type="submit"
            className="btn-primary"
            disabled={saving}
            style={{ width: '100%', justifyContent: 'center', padding: '14px', marginTop: '8px' }}
          >
            {saving ? 'Saving...' : 'Save Changes'}
          </button>
        </form>
      </div>

      <div style={{ marginTop: '20px', background: 'white', borderRadius: '16px', padding: '20px 24px', boxShadow: '0 2px 12px rgba(0,0,0,0.06)' }}>
        <p style={{ fontSize: '13px', color: '#999' }}>
          Member since {user?.createdAt ? new Date(user.createdAt).toLocaleDateString('en-IN', { month: 'long', year: 'numeric' }) : 'N/A'}
        </p>
      </div>
    </div>
  );
};

export default Profile;

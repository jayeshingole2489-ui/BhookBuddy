import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useToast } from '../App';
import { orderService } from '../services/orderService';
import { Address } from '../types';
import LoadingSpinner from '../components/common/LoadingSpinner';

const Addresses: React.FC = () => {
  const { user } = useAuth();
  const { showToast } = useToast();
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState<number | null>(null);
  const [form, setForm] = useState({ label: 'Home', street: '', city: '', state: '', zipCode: '', country: 'India', isDefault: false });
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (!user) return;
    orderService.getUserAddresses(user.id)
      .then(setAddresses)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [user]);

  const validateForm = (): boolean => {
    const errs: Record<string, string> = {};
    if (!form.street.trim()) errs.street = 'Street is required';
    if (!form.city.trim()) errs.city = 'City is required';
    if (!form.state.trim()) errs.state = 'State is required';
    if (!form.zipCode.trim()) errs.zipCode = 'ZIP code is required';
    setFormErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user || !validateForm()) return;
    try {
      setSaving(true);
      const newAddr = await orderService.addAddress(user.id, form);
      setAddresses((prev) => [...prev, newAddr]);
      setForm({ label: 'Home', street: '', city: '', state: '', zipCode: '', country: 'India', isDefault: false });
      setShowForm(false);
      showToast('Address added!', 'success');
    } catch {
      showToast('Failed to add address', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!user) return;
    try {
      setDeletingId(id);
      await orderService.deleteAddress(user.id, id);
      setAddresses((prev) => prev.filter((a) => a.id !== id));
      showToast('Address removed', 'success');
    } catch {
      showToast('Failed to delete address', 'error');
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <div style={{ maxWidth: '640px', margin: '0 auto', padding: '40px 24px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px', flexWrap: 'wrap', gap: '12px' }}>
        <div>
          <h1 style={{ fontSize: '28px', fontWeight: 800, color: '#1A1A2E' }}>My Addresses</h1>
          <p style={{ fontSize: '15px', color: '#666' }}>Manage your saved delivery addresses</p>
        </div>
        <button onClick={() => setShowForm(!showForm)} className="btn-primary" style={{ fontSize: '14px', padding: '10px 20px' }}>
          {showForm ? '✕ Cancel' : '+ Add Address'}
        </button>
      </div>

      {showForm && (
        <div style={{ background: 'white', borderRadius: '16px', padding: '24px', boxShadow: '0 2px 12px rgba(0,0,0,0.08)', marginBottom: '24px' }}>
          <h3 style={{ fontSize: '16px', fontWeight: 700, marginBottom: '20px', color: '#1A1A2E' }}>New Address</h3>
          <form onSubmit={handleAdd} noValidate>
            <div className="form-group">
              <label>Label</label>
              <select value={form.label} onChange={(e) => setForm((p) => ({ ...p, label: e.target.value }))}>
                <option>Home</option><option>Work</option><option>Other</option>
              </select>
            </div>
            <div className="form-group">
              <label>Street address</label>
              <input type="text" value={form.street} onChange={(e) => setForm((p) => ({ ...p, street: e.target.value }))} className={formErrors.street ? 'error' : ''} placeholder="123 Main Street, Apt 4B" />
              {formErrors.street && <span className="form-error">{formErrors.street}</span>}
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              <div className="form-group">
                <label>City</label>
                <input type="text" value={form.city} onChange={(e) => setForm((p) => ({ ...p, city: e.target.value }))} className={formErrors.city ? 'error' : ''} placeholder="Mumbai" />
                {formErrors.city && <span className="form-error">{formErrors.city}</span>}
              </div>
              <div className="form-group">
                <label>State</label>
                <input type="text" value={form.state} onChange={(e) => setForm((p) => ({ ...p, state: e.target.value }))} className={formErrors.state ? 'error' : ''} placeholder="Maharashtra" />
                {formErrors.state && <span className="form-error">{formErrors.state}</span>}
              </div>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              <div className="form-group">
                <label>ZIP code</label>
                <input type="text" value={form.zipCode} onChange={(e) => setForm((p) => ({ ...p, zipCode: e.target.value }))} className={formErrors.zipCode ? 'error' : ''} placeholder="400001" />
                {formErrors.zipCode && <span className="form-error">{formErrors.zipCode}</span>}
              </div>
              <div className="form-group">
                <label>Country</label>
                <input type="text" value={form.country} onChange={(e) => setForm((p) => ({ ...p, country: e.target.value }))} placeholder="India" />
              </div>
            </div>
            <label style={{ display: 'flex', alignItems: 'center', gap: '10px', cursor: 'pointer', marginBottom: '20px', fontSize: '14px' }}>
              <input type="checkbox" checked={form.isDefault} onChange={(e) => setForm((p) => ({ ...p, isDefault: e.target.checked }))} style={{ accentColor: '#FF6B35', width: '16px', height: '16px' }} />
              Set as default address
            </label>
            <button type="submit" className="btn-primary" disabled={saving} style={{ width: '100%', justifyContent: 'center', padding: '12px' }}>
              {saving ? 'Saving...' : 'Save Address'}
            </button>
          </form>
        </div>
      )}

      {loading ? <LoadingSpinner /> : addresses.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">📍</div>
          <h3>No addresses saved</h3>
          <p>Add a delivery address to speed up checkout.</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          {addresses.map((addr) => (
            <div key={addr.id} style={{
              background: 'white', borderRadius: '14px', padding: '18px 20px',
              boxShadow: '0 2px 10px rgba(0,0,0,0.06)',
              display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start',
              border: addr.isDefault ? '2px solid #FF6B35' : '2px solid transparent',
            }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
                  <span style={{ fontSize: '18px' }}>{addr.label === 'Home' ? '🏠' : addr.label === 'Work' ? '🏢' : '📍'}</span>
                  <span style={{ fontWeight: 700, fontSize: '15px', color: '#1A1A2E' }}>{addr.label}</span>
                  {addr.isDefault && (
                    <span style={{ background: '#FFF3EE', color: '#FF6B35', padding: '2px 8px', borderRadius: '10px', fontSize: '11px', fontWeight: 600 }}>Default</span>
                  )}
                </div>
                <p style={{ fontSize: '14px', color: '#666', lineHeight: 1.5 }}>
                  {addr.street}<br />
                  {addr.city}, {addr.state} {addr.zipCode}<br />
                  {addr.country}
                </p>
              </div>
              <button
                onClick={() => handleDelete(addr.id)}
                disabled={deletingId === addr.id}
                style={{
                  background: 'none', border: 'none', cursor: 'pointer',
                  color: '#e74c3c', fontSize: '18px', padding: '4px', flexShrink: 0,
                }}
                title="Delete address"
              >
                {deletingId === addr.id ? '...' : '🗑'}
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Addresses;

import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import AdminSidebar from '../../components/admin/AdminSidebar';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { restaurantService } from '../../services/restaurantService';
import { useToast } from '../../App';

interface FormData {
  name: string;
  description: string;
  cuisineType: string;
  address: string;
  city: string;
  phone: string;
  rating: string;
  deliveryFee: string;
  minOrderAmount: string;
  estimatedDeliveryMinutes: string;
  imageUrl: string;
}

const CUISINE_OPTIONS = ['Indian', 'Chinese', 'Italian', 'Pizza', 'Burgers', 'Biryani', 'Mexican', 'Thai', 'Continental', 'Fast Food', 'Desserts', 'Beverages'];

const RestaurantEditor: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const isEdit = !!id;

  const [form, setForm] = useState<FormData>({
    name: '', description: '', cuisineType: 'Indian', address: '', city: '',
    phone: '', rating: '4.0', deliveryFee: '40', minOrderAmount: '200',
    estimatedDeliveryMinutes: '30', imageUrl: '',
  });
  const [errors, setErrors] = useState<Partial<FormData>>({});
  const [loading, setLoading] = useState(isEdit);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!id) return;
    restaurantService.getById(Number(id))
      .then((r) => {
        setForm({
          name: r.name,
          description: r.description ?? '',
          cuisineType: r.cuisineType,
          address: r.address,
          city: r.city,
          phone: r.phone ?? '',
          rating: r.rating.toString(),
          deliveryFee: r.deliveryFee.toString(),
          minOrderAmount: r.minOrderAmount.toString(),
          estimatedDeliveryMinutes: r.estimatedDeliveryMinutes.toString(),
          imageUrl: r.imageUrl ?? '',
        });
      })
      .catch(() => showToast('Failed to load restaurant', 'error'))
      .finally(() => setLoading(false));
  }, [id]);

  const validate = (): boolean => {
    const errs: Partial<FormData> = {};
    if (!form.name.trim()) errs.name = 'Name is required';
    if (!form.cuisineType) errs.cuisineType = 'Cuisine type is required';
    if (!form.address.trim()) errs.address = 'Address is required';
    if (!form.city.trim()) errs.city = 'City is required';
    const rating = parseFloat(form.rating);
    if (isNaN(rating) || rating < 0 || rating > 5) errs.rating = 'Rating must be 0–5';
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleChange = (field: keyof FormData) => (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
  ) => {
    setForm((p) => ({ ...p, [field]: e.target.value }));
    setErrors((p) => ({ ...p, [field]: undefined }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    try {
      setSaving(true);
      const data = {
        name: form.name.trim(),
        description: form.description.trim() || undefined,
        cuisineType: form.cuisineType,
        address: form.address.trim(),
        city: form.city.trim(),
        phone: form.phone.trim() || undefined,
        rating: parseFloat(form.rating),
        deliveryFee: parseFloat(form.deliveryFee),
        minOrderAmount: parseFloat(form.minOrderAmount),
        estimatedDeliveryMinutes: parseInt(form.estimatedDeliveryMinutes),
        imageUrl: form.imageUrl.trim() || undefined,
      };
      if (isEdit) {
        await restaurantService.update(Number(id), data);
        showToast('Restaurant updated!', 'success');
      } else {
        await restaurantService.create(data);
        showToast('Restaurant created!', 'success');
      }
      navigate('/admin/restaurants');
    } catch (error: unknown) {
      const msg = error instanceof Error ? error.message : 'Failed to save restaurant';
      showToast(msg, 'error');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <LoadingSpinner fullPage />;

  return (
    <div style={{ display: 'flex', gap: '28px', maxWidth: '1200px', margin: '0 auto', padding: '32px 24px', alignItems: 'flex-start' }}>
      <AdminSidebar />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '28px' }}>
          <button onClick={() => navigate('/admin/restaurants')} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#FF6B35', fontSize: '16px', fontWeight: 600 }}>
            ← Back
          </button>
          <h1 style={{ fontSize: '24px', fontWeight: 800, color: '#1A1A2E' }}>
            {isEdit ? 'Edit Restaurant' : 'Add Restaurant'}
          </h1>
        </div>

        <div style={{ background: 'white', borderRadius: '16px', padding: '28px', boxShadow: '0 2px 12px rgba(0,0,0,0.06)' }}>
          <form onSubmit={handleSubmit} noValidate>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
              <div className="form-group">
                <label>Restaurant Name *</label>
                <input type="text" value={form.name} onChange={handleChange('name')} className={errors.name ? 'error' : ''} placeholder="e.g. Spice Garden" />
                {errors.name && <span className="form-error">{errors.name}</span>}
              </div>
              <div className="form-group">
                <label>Cuisine Type *</label>
                <select value={form.cuisineType} onChange={handleChange('cuisineType')} className={errors.cuisineType ? 'error' : ''}>
                  {CUISINE_OPTIONS.map((c) => <option key={c} value={c}>{c}</option>)}
                </select>
                {errors.cuisineType && <span className="form-error">{errors.cuisineType}</span>}
              </div>
            </div>

            <div className="form-group">
              <label>Description</label>
              <textarea value={form.description} onChange={handleChange('description')} placeholder="Brief description..." style={{ resize: 'vertical', minHeight: '80px' }} />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '20px' }}>
              <div className="form-group">
                <label>Street Address *</label>
                <input type="text" value={form.address} onChange={handleChange('address')} className={errors.address ? 'error' : ''} placeholder="123 Street, Area" />
                {errors.address && <span className="form-error">{errors.address}</span>}
              </div>
              <div className="form-group">
                <label>City *</label>
                <input type="text" value={form.city} onChange={handleChange('city')} className={errors.city ? 'error' : ''} placeholder="Mumbai" />
                {errors.city && <span className="form-error">{errors.city}</span>}
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
              <div className="form-group">
                <label>Phone</label>
                <input type="tel" value={form.phone} onChange={handleChange('phone')} placeholder="+91 98765 43210" />
              </div>
              <div className="form-group">
                <label>Rating (0–5)</label>
                <input type="number" value={form.rating} onChange={handleChange('rating')} min="0" max="5" step="0.1" className={errors.rating ? 'error' : ''} />
                {errors.rating && <span className="form-error">{errors.rating}</span>}
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '20px' }}>
              <div className="form-group">
                <label>Delivery Fee (₹)</label>
                <input type="number" value={form.deliveryFee} onChange={handleChange('deliveryFee')} min="0" step="5" />
              </div>
              <div className="form-group">
                <label>Min Order (₹)</label>
                <input type="number" value={form.minOrderAmount} onChange={handleChange('minOrderAmount')} min="0" step="10" />
              </div>
              <div className="form-group">
                <label>Est. Delivery (min)</label>
                <input type="number" value={form.estimatedDeliveryMinutes} onChange={handleChange('estimatedDeliveryMinutes')} min="10" max="120" step="5" />
              </div>
            </div>

            <div className="form-group">
              <label>Image URL</label>
              <input type="url" value={form.imageUrl} onChange={handleChange('imageUrl')} placeholder="https://..." />
            </div>

            <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end', marginTop: '8px' }}>
              <button type="button" onClick={() => navigate('/admin/restaurants')} className="btn-secondary" style={{ fontSize: '14px' }}>
                Cancel
              </button>
              <button type="submit" className="btn-primary" disabled={saving} style={{ fontSize: '14px', padding: '12px 28px' }}>
                {saving ? 'Saving...' : isEdit ? 'Update Restaurant' : 'Create Restaurant'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default RestaurantEditor;

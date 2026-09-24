import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import AdminSidebar from '../../components/admin/AdminSidebar';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { Restaurant, MenuItem } from '../../types';
import { restaurantService } from '../../services/restaurantService';
import { useToast } from '../../App';

interface MenuForm {
  name: string;
  description: string;
  category: string;
  price: string;
  vegetarian: boolean;
  available: boolean;
  imageUrl: string;
}

const EMPTY_FORM: MenuForm = {
  name: '', description: '', category: '', price: '', vegetarian: true, available: true, imageUrl: '',
};

const MenuManagement: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { showToast } = useToast();

  const [restaurant, setRestaurant] = useState<Restaurant | null>(null);
  const [menuItems, setMenuItems] = useState<MenuItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingItem, setEditingItem] = useState<MenuItem | null>(null);
  const [form, setForm] = useState<MenuForm>(EMPTY_FORM);
  const [formErrors, setFormErrors] = useState<Partial<MenuForm>>({});
  const [saving, setSaving] = useState(false);
  const [togglingId, setTogglingId] = useState<number | null>(null);

  useEffect(() => {
    if (!id) return;
    Promise.all([
      restaurantService.getById(Number(id)),
      restaurantService.getMenuItems(Number(id)),
    ])
      .then(([r, items]) => { setRestaurant(r); setMenuItems(items); })
      .catch(() => showToast('Failed to load data', 'error'))
      .finally(() => setLoading(false));
  }, [id]);

  const openAdd = () => {
    setEditingItem(null);
    setForm(EMPTY_FORM);
    setFormErrors({});
    setShowForm(true);
  };

  const openEdit = (item: MenuItem) => {
    setEditingItem(item);
    setForm({
      name: item.name,
      description: item.description ?? '',
      category: item.category,
      price: item.price.toString(),
      vegetarian: item.vegetarian,
      available: item.available,
      imageUrl: item.imageUrl ?? '',
    });
    setFormErrors({});
    setShowForm(true);
  };

  const validateForm = (): boolean => {
    const errs: Partial<MenuForm> = {};
    if (!form.name.trim()) errs.name = 'Name is required';
    if (!form.category.trim()) errs.category = 'Category is required';
    const price = parseFloat(form.price);
    if (isNaN(price) || price <= 0) errs.price = 'Enter a valid price';
    setFormErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!id || !validateForm()) return;
    try {
      setSaving(true);
      const data = {
        name: form.name.trim(),
        description: form.description.trim() || undefined,
        category: form.category.trim(),
        price: parseFloat(form.price),
        vegetarian: form.vegetarian,
        available: form.available,
        imageUrl: form.imageUrl.trim() || undefined,
      };
      if (editingItem) {
        const updated = await restaurantService.updateMenuItem(Number(id), editingItem.id, data);
        setMenuItems((prev) => prev.map((m) => m.id === editingItem.id ? updated : m));
        showToast('Item updated!', 'success');
      } else {
        const created = await restaurantService.addMenuItem(Number(id), data);
        setMenuItems((prev) => [...prev, created]);
        showToast('Item added!', 'success');
      }
      setShowForm(false);
    } catch {
      showToast('Failed to save item', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleToggle = async (item: MenuItem) => {
    if (!id) return;
    try {
      setTogglingId(item.id);
      const updated = await restaurantService.toggleMenuItemAvailability(Number(id), item.id);
      setMenuItems((prev) => prev.map((m) => m.id === item.id ? updated : m));
    } catch {
      showToast('Failed to update availability', 'error');
    } finally {
      setTogglingId(null);
    }
  };

  if (loading) return <LoadingSpinner fullPage />;

  return (
    <div style={{ display: 'flex', gap: '28px', maxWidth: '1200px', margin: '0 auto', padding: '32px 24px', alignItems: 'flex-start' }}>
      <AdminSidebar />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' }}>
          <button onClick={() => navigate('/admin/restaurants')} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#FF6B35', fontSize: '15px', fontWeight: 600 }}>
            ← Restaurants
          </button>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px', flexWrap: 'wrap', gap: '12px' }}>
          <div>
            <h1 style={{ fontSize: '22px', fontWeight: 800, color: '#1A1A2E' }}>Menu — {restaurant?.name}</h1>
            <p style={{ fontSize: '14px', color: '#666' }}>{menuItems.length} items</p>
          </div>
          <button onClick={openAdd} className="btn-primary" style={{ fontSize: '14px', padding: '10px 20px' }}>+ Add Item</button>
        </div>

        {/* Add/Edit form */}
        {showForm && (
          <div style={{ background: 'white', borderRadius: '16px', padding: '24px', boxShadow: '0 2px 16px rgba(0,0,0,0.1)', marginBottom: '24px', border: '2px solid #FF6B35' }}>
            <h3 style={{ fontSize: '16px', fontWeight: 700, marginBottom: '20px', color: '#1A1A2E' }}>{editingItem ? 'Edit Item' : 'Add Menu Item'}</h3>
            <form onSubmit={handleSave} noValidate>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <div className="form-group">
                  <label>Item Name *</label>
                  <input type="text" value={form.name} onChange={(e) => setForm((p) => ({ ...p, name: e.target.value }))} className={formErrors.name ? 'error' : ''} placeholder="e.g. Butter Chicken" />
                  {formErrors.name && <span className="form-error">{formErrors.name}</span>}
                </div>
                <div className="form-group">
                  <label>Category *</label>
                  <input type="text" value={form.category} onChange={(e) => setForm((p) => ({ ...p, category: e.target.value }))} className={formErrors.category ? 'error' : ''} placeholder="e.g. Main Course" />
                  {formErrors.category && <span className="form-error">{formErrors.category}</span>}
                </div>
              </div>
              <div className="form-group">
                <label>Description</label>
                <textarea value={form.description} onChange={(e) => setForm((p) => ({ ...p, description: e.target.value }))} placeholder="Short description..." style={{ resize: 'vertical', minHeight: '70px' }} />
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <div className="form-group">
                  <label>Price (₹) *</label>
                  <input type="number" value={form.price} onChange={(e) => setForm((p) => ({ ...p, price: e.target.value }))} className={formErrors.price ? 'error' : ''} min="0" step="5" placeholder="280" />
                  {formErrors.price && <span className="form-error">{formErrors.price}</span>}
                </div>
                <div className="form-group">
                  <label>Image URL</label>
                  <input type="url" value={form.imageUrl} onChange={(e) => setForm((p) => ({ ...p, imageUrl: e.target.value }))} placeholder="https://..." />
                </div>
              </div>
              <div style={{ display: 'flex', gap: '24px', marginBottom: '20px' }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', fontSize: '14px' }}>
                  <input type="checkbox" checked={form.vegetarian} onChange={(e) => setForm((p) => ({ ...p, vegetarian: e.target.checked }))} style={{ accentColor: '#2ecc71', width: '16px', height: '16px' }} />
                  🟢 Vegetarian
                </label>
                <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', fontSize: '14px' }}>
                  <input type="checkbox" checked={form.available} onChange={(e) => setForm((p) => ({ ...p, available: e.target.checked }))} style={{ accentColor: '#FF6B35', width: '16px', height: '16px' }} />
                  Available
                </label>
              </div>
              <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
                <button type="button" onClick={() => setShowForm(false)} className="btn-secondary" style={{ fontSize: '14px' }}>Cancel</button>
                <button type="submit" className="btn-primary" disabled={saving} style={{ fontSize: '14px' }}>
                  {saving ? 'Saving...' : editingItem ? 'Update' : 'Add Item'}
                </button>
              </div>
            </form>
          </div>
        )}

        {/* Table */}
        <div style={{ background: 'white', borderRadius: '16px', boxShadow: '0 2px 12px rgba(0,0,0,0.06)', overflow: 'hidden' }}>
          <div style={{ overflowX: 'auto' }}>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Category</th>
                  <th>Price</th>
                  <th>Type</th>
                  <th>Available</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {menuItems.length === 0 ? (
                  <tr><td colSpan={6} style={{ textAlign: 'center', padding: '40px', color: '#999' }}>No menu items yet</td></tr>
                ) : menuItems.map((item) => (
                  <tr key={item.id}>
                    <td style={{ fontWeight: 600 }}>{item.name}</td>
                    <td style={{ color: '#666' }}>{item.category}</td>
                    <td style={{ fontWeight: 600 }}>₹{item.price.toFixed(2)}</td>
                    <td>{item.vegetarian ? '🟢 Veg' : '🔴 Non-veg'}</td>
                    <td>
                      <span style={{
                        padding: '3px 10px', borderRadius: '12px', fontSize: '12px', fontWeight: 600,
                        background: item.available ? '#EAFAF1' : '#FDEDEC',
                        color: item.available ? '#1E8449' : '#C0392B',
                      }}>
                        {item.available ? 'Available' : 'Unavailable'}
                      </span>
                    </td>
                    <td>
                      <div style={{ display: 'flex', gap: '8px' }}>
                        <button onClick={() => openEdit(item)} className="btn-ghost" style={{ fontSize: '12px', padding: '5px 10px' }}>Edit</button>
                        <button
                          onClick={() => handleToggle(item)}
                          disabled={togglingId === item.id}
                          style={{
                            padding: '5px 10px', border: 'none', borderRadius: '6px', fontSize: '12px', fontWeight: 600, cursor: 'pointer',
                            background: item.available ? '#FDEDEC' : '#EAFAF1',
                            color: item.available ? '#C0392B' : '#1E8449',
                          }}
                        >
                          {togglingId === item.id ? '...' : item.available ? 'Disable' : 'Enable'}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default MenuManagement;

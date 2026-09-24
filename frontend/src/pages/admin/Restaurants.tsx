import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import AdminSidebar from '../../components/admin/AdminSidebar';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { Restaurant } from '../../types';
import { restaurantService } from '../../services/restaurantService';
import { useToast } from '../../App';

const AdminRestaurants: React.FC = () => {
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [loading, setLoading] = useState(true);
  const [togglingId, setTogglingId] = useState<number | null>(null);
  const navigate = useNavigate();
  const { showToast } = useToast();

  useEffect(() => {
    restaurantService.getAll()
      .then(setRestaurants)
      .catch(() => showToast('Failed to load restaurants', 'error'))
      .finally(() => setLoading(false));
  }, []);

  const handleToggle = async (id: number) => {
    try {
      setTogglingId(id);
      const updated = await restaurantService.toggleActive(id);
      setRestaurants((prev) => prev.map((r) => r.id === id ? updated : r));
      showToast('Restaurant status updated', 'success');
    } catch {
      showToast('Failed to update restaurant', 'error');
    } finally {
      setTogglingId(null);
    }
  };

  return (
    <div style={{ display: 'flex', gap: '28px', maxWidth: '1200px', margin: '0 auto', padding: '32px 24px', alignItems: 'flex-start' }}>
      <AdminSidebar />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px', flexWrap: 'wrap', gap: '12px' }}>
          <div>
            <h1 style={{ fontSize: '24px', fontWeight: 800, color: '#1A1A2E' }}>Restaurants</h1>
            <p style={{ fontSize: '14px', color: '#666' }}>{restaurants.length} total restaurants</p>
          </div>
          <button onClick={() => navigate('/admin/restaurants/new')} className="btn-primary" style={{ fontSize: '14px', padding: '10px 20px' }}>
            + Add Restaurant
          </button>
        </div>

        {loading ? <LoadingSpinner /> : (
          <div style={{ background: 'white', borderRadius: '16px', boxShadow: '0 2px 12px rgba(0,0,0,0.06)', overflow: 'hidden' }}>
            <div style={{ overflowX: 'auto' }}>
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Cuisine</th>
                    <th>City</th>
                    <th>Rating</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {restaurants.length === 0 ? (
                    <tr><td colSpan={6} style={{ textAlign: 'center', padding: '40px', color: '#999' }}>No restaurants found</td></tr>
                  ) : restaurants.map((r) => (
                    <tr key={r.id}>
                      <td style={{ fontWeight: 600 }}>{r.name}</td>
                      <td style={{ color: '#666' }}>{r.cuisineType}</td>
                      <td style={{ color: '#666' }}>{r.city}</td>
                      <td>
                        <span style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '13px' }}>
                          ⭐ {r.rating.toFixed(1)}
                        </span>
                      </td>
                      <td>
                        <span style={{
                          padding: '3px 10px', borderRadius: '12px', fontSize: '12px', fontWeight: 600,
                          background: r.active ? '#EAFAF1' : '#FDEDEC',
                          color: r.active ? '#1E8449' : '#C0392B',
                        }}>
                          {r.active ? 'Active' : 'Inactive'}
                        </span>
                      </td>
                      <td>
                        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                          <button onClick={() => navigate(`/admin/restaurants/${r.id}/edit`)} className="btn-ghost" style={{ fontSize: '12px', padding: '5px 10px' }}>Edit</button>
                          <button onClick={() => navigate(`/admin/restaurants/${r.id}/menu`)} style={{ padding: '5px 10px', border: 'none', borderRadius: '6px', background: '#EBF5FB', color: '#2980B9', fontSize: '12px', fontWeight: 600, cursor: 'pointer' }}>Menu</button>
                          <button
                            onClick={() => handleToggle(r.id)}
                            disabled={togglingId === r.id}
                            style={{
                              padding: '5px 10px', border: 'none', borderRadius: '6px', fontSize: '12px', fontWeight: 600, cursor: 'pointer',
                              background: r.active ? '#FDEDEC' : '#EAFAF1',
                              color: r.active ? '#C0392B' : '#1E8449',
                            }}
                          >
                            {togglingId === r.id ? '...' : r.active ? 'Deactivate' : 'Activate'}
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminRestaurants;

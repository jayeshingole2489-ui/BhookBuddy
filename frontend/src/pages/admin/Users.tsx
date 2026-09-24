import React, { useState, useEffect, useMemo } from 'react';
import AdminSidebar from '../../components/admin/AdminSidebar';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { User } from '../../types';
import api from '../../services/api';
import { useToast } from '../../App';

const AdminUsers: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [togglingId, setTogglingId] = useState<number | null>(null);
  const { showToast } = useToast();

  useEffect(() => {
    api.get<User[]>('/admin/users')
      .then((r) => setUsers(r.data))
      .catch(() => showToast('Failed to load users', 'error'))
      .finally(() => setLoading(false));
  }, []);

  const filtered = useMemo(() => {
    const q = searchQuery.toLowerCase();
    return users.filter((u) =>
      u.firstName.toLowerCase().includes(q) ||
      u.lastName.toLowerCase().includes(q) ||
      u.email.toLowerCase().includes(q)
    );
  }, [users, searchQuery]);

  const handleToggle = async (userId: number) => {
    try {
      setTogglingId(userId);
      const res = await api.patch<User>(`/admin/users/${userId}/toggle`);
      setUsers((prev) => prev.map((u) => u.id === userId ? res.data : u));
      showToast('User status updated', 'success');
    } catch {
      showToast('Failed to update user', 'error');
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
            <h1 style={{ fontSize: '24px', fontWeight: 800, color: '#1A1A2E' }}>Users</h1>
            <p style={{ fontSize: '14px', color: '#666' }}>{users.length} total users</p>
          </div>
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search by name or email..."
            style={{ padding: '10px 18px', border: '1.5px solid #E5E5E5', borderRadius: '24px', fontSize: '14px', outline: 'none', minWidth: '240px' }}
            onFocus={(e) => (e.target.style.borderColor = '#FF6B35')}
            onBlur={(e) => (e.target.style.borderColor = '#E5E5E5')}
          />
        </div>

        {loading ? <LoadingSpinner /> : (
          <div style={{ background: 'white', borderRadius: '16px', boxShadow: '0 2px 12px rgba(0,0,0,0.06)', overflow: 'hidden' }}>
            <div style={{ overflowX: 'auto' }}>
              <table className="data-table">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>Role</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filtered.length === 0 ? (
                    <tr><td colSpan={7} style={{ textAlign: 'center', padding: '40px', color: '#999' }}>No users found</td></tr>
                  ) : filtered.map((user) => (
                    <tr key={user.id}>
                      <td style={{ color: '#999', fontSize: '13px' }}>{user.id}</td>
                      <td style={{ fontWeight: 600 }}>{user.firstName} {user.lastName}</td>
                      <td style={{ color: '#666', fontSize: '13px' }}>{user.email}</td>
                      <td style={{ color: '#666', fontSize: '13px' }}>{user.phone ?? '—'}</td>
                      <td>
                        <span style={{
                          padding: '3px 10px', borderRadius: '12px', fontSize: '12px', fontWeight: 600,
                          background: user.role === 'ROLE_ADMIN' ? '#F4ECF7' : '#EBF5FB',
                          color: user.role === 'ROLE_ADMIN' ? '#8E44AD' : '#2980B9',
                        }}>
                          {user.role === 'ROLE_ADMIN' ? 'Admin' : 'User'}
                        </span>
                      </td>
                      <td>
                        <span style={{
                          padding: '3px 10px', borderRadius: '12px', fontSize: '12px', fontWeight: 600,
                          background: user.active ? '#EAFAF1' : '#FDEDEC',
                          color: user.active ? '#1E8449' : '#C0392B',
                        }}>
                          {user.active ? 'Active' : 'Inactive'}
                        </span>
                      </td>
                      <td>
                        <button
                          onClick={() => handleToggle(user.id)}
                          disabled={togglingId === user.id}
                          style={{
                            padding: '6px 14px', border: 'none', borderRadius: '6px', fontSize: '13px', fontWeight: 600, cursor: 'pointer',
                            background: user.active ? '#FDEDEC' : '#EAFAF1',
                            color: user.active ? '#C0392B' : '#1E8449',
                          }}
                        >
                          {togglingId === user.id ? '...' : user.active ? 'Deactivate' : 'Activate'}
                        </button>
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

export default AdminUsers;

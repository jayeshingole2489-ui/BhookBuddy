import React, { useState, useEffect } from 'react';
import AdminSidebar from '../../components/admin/AdminSidebar';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { Delivery, DeliveryStatus } from '../../types';
import { deliveryService } from '../../services/deliveryService';
import { useToast } from '../../App';

const DELIVERY_STATUSES: DeliveryStatus[] = ['WAITING_FOR_ASSIGNMENT', 'ASSIGNED', 'PICKED_UP', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED'];

const AdminDeliveries: React.FC = () => {
  const [deliveries, setDeliveries] = useState<Delivery[]>([]);
  const [loading, setLoading] = useState(true);
  const [updatingId, setUpdatingId] = useState<string | null>(null);
  const [assigningId, setAssigningId] = useState<string | null>(null);
  const [assignForm, setAssignForm] = useState({ name: '', phone: '' });
  const { showToast } = useToast();

  useEffect(() => {
    deliveryService.getAllDeliveries()
      .then(setDeliveries)
      .catch(() => showToast('Failed to load deliveries', 'error'))
      .finally(() => setLoading(false));
  }, []);

  const handleStatusUpdate = async (deliveryId: string, status: string) => {
    try {
      setUpdatingId(deliveryId);
      const updated = await deliveryService.updateStatus(deliveryId, status);
      setDeliveries((prev) => prev.map((d) => d.deliveryId === deliveryId ? updated : d));
      showToast('Status updated', 'success');
    } catch {
      showToast('Failed to update status', 'error');
    } finally {
      setUpdatingId(null);
    }
  };

  const handleAssign = async (deliveryId: string) => {
    if (!assignForm.name.trim() || !assignForm.phone.trim()) {
      showToast('Enter delivery person name and phone', 'error');
      return;
    }
    try {
      setUpdatingId(deliveryId);
      const updated = await deliveryService.assign(deliveryId, { deliveryPersonName: assignForm.name, deliveryPersonPhone: assignForm.phone });
      setDeliveries((prev) => prev.map((d) => d.deliveryId === deliveryId ? updated : d));
      setAssigningId(null);
      setAssignForm({ name: '', phone: '' });
      showToast('Delivery person assigned!', 'success');
    } catch {
      showToast('Failed to assign delivery person', 'error');
    } finally {
      setUpdatingId(null);
    }
  };

  return (
    <div style={{ display: 'flex', gap: '28px', maxWidth: '1200px', margin: '0 auto', padding: '32px 24px', alignItems: 'flex-start' }}>
      <AdminSidebar />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ marginBottom: '28px' }}>
          <h1 style={{ fontSize: '24px', fontWeight: 800, color: '#1A1A2E' }}>Deliveries</h1>
          <p style={{ fontSize: '14px', color: '#666' }}>{deliveries.length} total deliveries</p>
        </div>

        {loading ? <LoadingSpinner /> : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {deliveries.length === 0 ? (
              <div className="empty-state">
                <div className="empty-icon">🛵</div>
                <h3>No deliveries yet</h3>
              </div>
            ) : deliveries.map((delivery) => (
              <div key={delivery.deliveryId} style={{ background: 'white', borderRadius: '14px', padding: '20px', boxShadow: '0 2px 10px rgba(0,0,0,0.06)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '16px', flexWrap: 'wrap' }}>
                  <div style={{ flex: 1, minWidth: '200px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px', flexWrap: 'wrap' }}>
                      <span style={{ fontFamily: 'monospace', fontSize: '12px', color: '#999' }}>#{delivery.deliveryId.slice(0, 12)}...</span>
                      <span className={`status-badge status-${delivery.status}`}>{delivery.status.replace(/_/g, ' ')}</span>
                    </div>
                    <p style={{ fontSize: '13px', color: '#666', marginBottom: '4px' }}>📦 {delivery.restaurantAddress}</p>
                    <p style={{ fontSize: '13px', color: '#666', marginBottom: '4px' }}>📍 {delivery.deliveryAddress}</p>
                    {delivery.deliveryPersonName && (
                      <p style={{ fontSize: '13px', color: '#1A1A2E', fontWeight: 600 }}>
                        👤 {delivery.deliveryPersonName} · {delivery.deliveryPersonPhone}
                      </p>
                    )}
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', alignItems: 'flex-end' }}>
                    <select
                      value={delivery.status}
                      onChange={(e) => handleStatusUpdate(delivery.deliveryId, e.target.value)}
                      disabled={updatingId === delivery.deliveryId}
                      style={{ padding: '7px 12px', border: '1.5px solid #E5E5E5', borderRadius: '8px', fontSize: '13px', outline: 'none', background: 'white', cursor: 'pointer' }}
                    >
                      {DELIVERY_STATUSES.map((s) => <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>)}
                    </select>

                    {!delivery.deliveryPersonName && (
                      <button
                        onClick={() => setAssigningId(assigningId === delivery.deliveryId ? null : delivery.deliveryId)}
                        style={{ padding: '7px 14px', border: 'none', borderRadius: '8px', background: '#FFF3EE', color: '#FF6B35', fontSize: '13px', fontWeight: 600, cursor: 'pointer' }}
                      >
                        Assign Agent
                      </button>
                    )}
                  </div>
                </div>

                {assigningId === delivery.deliveryId && (
                  <div style={{ marginTop: '16px', paddingTop: '16px', borderTop: '1px solid #E5E5E5', display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
                    <input
                      type="text"
                      value={assignForm.name}
                      onChange={(e) => setAssignForm((p) => ({ ...p, name: e.target.value }))}
                      placeholder="Agent name"
                      style={{ flex: 1, minWidth: '160px', padding: '8px 14px', border: '1.5px solid #E5E5E5', borderRadius: '8px', fontSize: '14px', outline: 'none' }}
                    />
                    <input
                      type="tel"
                      value={assignForm.phone}
                      onChange={(e) => setAssignForm((p) => ({ ...p, phone: e.target.value }))}
                      placeholder="Phone number"
                      style={{ flex: 1, minWidth: '140px', padding: '8px 14px', border: '1.5px solid #E5E5E5', borderRadius: '8px', fontSize: '14px', outline: 'none' }}
                    />
                    <button
                      onClick={() => handleAssign(delivery.deliveryId)}
                      disabled={updatingId === delivery.deliveryId}
                      className="btn-primary"
                      style={{ fontSize: '13px', padding: '8px 18px' }}
                    >
                      {updatingId === delivery.deliveryId ? '...' : 'Assign'}
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminDeliveries;

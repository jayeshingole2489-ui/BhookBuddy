import React, { useState, useEffect, useMemo } from 'react';
import AdminSidebar from '../../components/admin/AdminSidebar';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { Order, OrderStatus } from '../../types';
import { orderService } from '../../services/orderService';
import { useToast } from '../../App';

const ALL_STATUSES: OrderStatus[] = ['CREATED', 'PAYMENT_PENDING', 'CONFIRMED', 'PREPARING', 'READY_FOR_PICKUP', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED', 'PAYMENT_FAILED'];

const AdminOrders: React.FC = () => {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [updatingId, setUpdatingId] = useState<string | null>(null);
  const { showToast } = useToast();

  useEffect(() => {
    orderService.getAllOrders()
      .then((data) => setOrders(data.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())))
      .catch(() => showToast('Failed to load orders', 'error'))
      .finally(() => setLoading(false));
  }, []);

  const filtered = useMemo(() => {
    if (statusFilter === 'ALL') return orders;
    return orders.filter((o) => o.status === statusFilter);
  }, [orders, statusFilter]);

  const handleStatusUpdate = async (orderId: string, newStatus: string) => {
    try {
      setUpdatingId(orderId);
      const updated = await orderService.updateStatus(orderId, newStatus);
      setOrders((prev) => prev.map((o) => o.orderId === orderId ? updated : o));
      showToast('Order status updated', 'success');
    } catch {
      showToast('Failed to update status', 'error');
    } finally {
      setUpdatingId(null);
    }
  };

  return (
    <div style={{ display: 'flex', gap: '28px', maxWidth: '1200px', margin: '0 auto', padding: '32px 24px', alignItems: 'flex-start' }}>
      <AdminSidebar />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px', flexWrap: 'wrap', gap: '12px' }}>
          <div>
            <h1 style={{ fontSize: '24px', fontWeight: 800, color: '#1A1A2E' }}>Orders</h1>
            <p style={{ fontSize: '14px', color: '#666' }}>{filtered.length} orders</p>
          </div>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            style={{ padding: '10px 16px', border: '1.5px solid #E5E5E5', borderRadius: '8px', fontSize: '14px', outline: 'none', background: 'white' }}
          >
            <option value="ALL">All Statuses</option>
            {ALL_STATUSES.map((s) => <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>)}
          </select>
        </div>

        {loading ? <LoadingSpinner /> : (
          <div style={{ background: 'white', borderRadius: '16px', boxShadow: '0 2px 12px rgba(0,0,0,0.06)', overflow: 'hidden' }}>
            <div style={{ overflowX: 'auto' }}>
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Order ID</th>
                    <th>Restaurant</th>
                    <th>Total</th>
                    <th>Status</th>
                    <th>Date</th>
                    <th>Update Status</th>
                  </tr>
                </thead>
                <tbody>
                  {filtered.length === 0 ? (
                    <tr><td colSpan={6} style={{ textAlign: 'center', padding: '40px', color: '#999' }}>No orders found</td></tr>
                  ) : filtered.map((order) => (
                    <tr key={order.id}>
                      <td style={{ fontFamily: 'monospace', fontSize: '12px' }}>#{order.orderId.slice(0, 12)}...</td>
                      <td style={{ fontWeight: 600 }}>{order.restaurantName}</td>
                      <td style={{ fontWeight: 600 }}>₹{order.total.toFixed(2)}</td>
                      <td><span className={`status-badge status-${order.status}`}>{order.status.replace(/_/g, ' ')}</span></td>
                      <td style={{ color: '#999', fontSize: '13px' }}>{new Date(order.createdAt).toLocaleDateString('en-IN')}</td>
                      <td>
                        <select
                          value={order.status}
                          onChange={(e) => handleStatusUpdate(order.orderId, e.target.value)}
                          disabled={updatingId === order.orderId}
                          style={{ padding: '6px 10px', border: '1.5px solid #E5E5E5', borderRadius: '6px', fontSize: '12px', outline: 'none', background: 'white', cursor: 'pointer' }}
                        >
                          {ALL_STATUSES.map((s) => <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>)}
                        </select>
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

export default AdminOrders;

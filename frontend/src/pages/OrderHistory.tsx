import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Order } from '../types';
import { orderService } from '../services/orderService';
import LoadingSpinner from '../components/common/LoadingSpinner';

const OrderHistory: React.FC = () => {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    orderService.getMyOrders()
      .then((data) => setOrders(data.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('en-IN', {
      day: 'numeric', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });
  };

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto', padding: '32px 24px' }}>
      <h1 style={{ fontSize: '28px', fontWeight: 800, color: '#1A1A2E', marginBottom: '8px' }}>My Orders</h1>
      <p style={{ fontSize: '15px', color: '#666', marginBottom: '32px' }}>Track and review your past orders</p>

      {loading ? (
        <LoadingSpinner />
      ) : orders.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">📦</div>
          <h3>No orders yet</h3>
          <p>You haven't placed any orders. Let's fix that!</p>
          <Link to="/restaurants" className="btn-primary" style={{ display: 'inline-flex', marginTop: '16px' }}>
            Order Now
          </Link>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {orders.map((order) => (
            <div key={order.id} style={{
              background: 'white',
              borderRadius: '16px',
              padding: '20px 24px',
              boxShadow: '0 2px 12px rgba(0,0,0,0.06)',
              display: 'flex',
              gap: '20px',
              flexWrap: 'wrap',
              alignItems: 'flex-start',
            }}>
              <div style={{ flex: 1, minWidth: '200px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px', flexWrap: 'wrap' }}>
                  <h3 style={{ fontSize: '16px', fontWeight: 700, color: '#1A1A2E', margin: 0 }}>{order.restaurantName}</h3>
                  <span className={`status-badge status-${order.status}`}>{order.status.replace(/_/g, ' ')}</span>
                </div>
                <p style={{ fontSize: '13px', color: '#999', marginBottom: '10px' }}>
                  #{order.orderId} · {formatDate(order.createdAt)}
                </p>
                <p style={{ fontSize: '14px', color: '#666', marginBottom: '4px' }}>
                  {order.items.slice(0, 2).map((i) => `${i.name} ×${i.quantity}`).join(', ')}
                  {order.items.length > 2 && ` +${order.items.length - 2} more`}
                </p>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '12px', flexShrink: 0 }}>
                <span style={{ fontSize: '20px', fontWeight: 700, color: '#1A1A2E' }}>₹{order.total.toFixed(2)}</span>
                <div style={{ display: 'flex', gap: '10px' }}>
                  <button
                    onClick={() => navigate(`/orders/${order.orderId}/track`)}
                    className="btn-primary"
                    style={{ padding: '8px 18px', fontSize: '13px' }}
                  >
                    {order.status === 'DELIVERED' ? 'View Details' : 'Track Order'}
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default OrderHistory;

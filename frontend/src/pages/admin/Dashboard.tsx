import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import AdminSidebar from '../../components/admin/AdminSidebar';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { Order } from '../../types';
import { orderService } from '../../services/orderService';
import api from '../../services/api';

interface Stats {
  totalUsers: number;
  totalRestaurants: number;
  totalOrders: number;
  totalRevenue: number;
}

const Dashboard: React.FC = () => {
  const [stats, setStats] = useState<Stats>({ totalUsers: 0, totalRestaurants: 0, totalOrders: 0, totalRevenue: 0 });
  const [recentOrders, setRecentOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [statsResult, ordersResult] = await Promise.allSettled([
          api.get<Stats>('/admin/stats'),
          orderService.getAllOrders(),
        ]);
        if (statsResult.status === 'fulfilled') setStats(statsResult.value.data);
        if (ordersResult.status === 'fulfilled') {
          setRecentOrders(
            ordersResult.value
              .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
              .slice(0, 5)
          );
        }
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const statCards = [
    { label: 'Total Users', value: stats.totalUsers, icon: '👥', color: '#3498db', bg: '#EBF5FB' },
    { label: 'Restaurants', value: stats.totalRestaurants, icon: '🏪', color: '#27AE60', bg: '#EAFAF1' },
    { label: 'Total Orders', value: stats.totalOrders, icon: '📦', color: '#E67E22', bg: '#FEF5E7' },
    { label: 'Total Revenue', value: `₹${stats.totalRevenue.toLocaleString('en-IN')}`, icon: '💰', color: '#8E44AD', bg: '#F4ECF7' },
  ];

  return (
    <div style={{ display: 'flex', gap: '28px', maxWidth: '1200px', margin: '0 auto', padding: '32px 24px', alignItems: 'flex-start' }}>
      <AdminSidebar />
      <div style={{ flex: 1, minWidth: 0 }}>
        <h1 style={{ fontSize: '26px', fontWeight: 800, color: '#1A1A2E', marginBottom: '8px' }}>Dashboard</h1>
        <p style={{ fontSize: '15px', color: '#666', marginBottom: '32px' }}>Welcome to BhookBuddy admin panel</p>

        {loading ? <LoadingSpinner /> : (
          <>
            {/* Stat cards */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px', marginBottom: '32px' }}>
              {statCards.map((card) => (
                <div key={card.label} style={{ background: 'white', borderRadius: '14px', padding: '20px', boxShadow: '0 2px 12px rgba(0,0,0,0.06)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px' }}>
                    <div style={{ width: '44px', height: '44px', borderRadius: '12px', background: card.bg, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '22px' }}>
                      {card.icon}
                    </div>
                  </div>
                  <p style={{ fontSize: '28px', fontWeight: 800, color: card.color, marginBottom: '4px' }}>{card.value}</p>
                  <p style={{ fontSize: '14px', color: '#666' }}>{card.label}</p>
                </div>
              ))}
            </div>

            {/* Quick links */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '12px', marginBottom: '32px' }}>
              {[
                { to: '/admin/users', label: 'Manage Users', icon: '👥' },
                { to: '/admin/restaurants', label: 'Restaurants', icon: '🏪' },
                { to: '/admin/orders', label: 'Orders', icon: '📦' },
                { to: '/admin/payments', label: 'Payments', icon: '💳' },
                { to: '/admin/deliveries', label: 'Deliveries', icon: '🛵' },
              ].map(({ to, label, icon }) => (
                <Link key={to} to={to} style={{
                  display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px',
                  padding: '16px', background: 'white', borderRadius: '12px',
                  boxShadow: '0 2px 8px rgba(0,0,0,0.06)', textDecoration: 'none',
                  color: '#1A1A2E', fontSize: '13px', fontWeight: 600, textAlign: 'center',
                  transition: 'box-shadow 0.2s',
                }}
                  onMouseEnter={(e) => (e.currentTarget.style.boxShadow = '0 4px 16px rgba(255,107,53,0.15)')}
                  onMouseLeave={(e) => (e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.06)')}
                >
                  <span style={{ fontSize: '28px' }}>{icon}</span>
                  {label}
                </Link>
              ))}
            </div>

            {/* Recent orders */}
            <div style={{ background: 'white', borderRadius: '16px', padding: '24px', boxShadow: '0 2px 12px rgba(0,0,0,0.06)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <h2 style={{ fontSize: '17px', fontWeight: 700, color: '#1A1A2E' }}>Recent Orders</h2>
                <Link to="/admin/orders" style={{ fontSize: '13px', color: '#FF6B35', fontWeight: 600, textDecoration: 'none' }}>View all →</Link>
              </div>
              {recentOrders.length === 0 ? (
                <p style={{ color: '#999', fontSize: '14px', textAlign: 'center', padding: '20px 0' }}>No orders yet</p>
              ) : (
                <div style={{ overflowX: 'auto' }}>
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Order ID</th>
                        <th>Restaurant</th>
                        <th>Total</th>
                        <th>Status</th>
                        <th>Date</th>
                      </tr>
                    </thead>
                    <tbody>
                      {recentOrders.map((order) => (
                        <tr key={order.id}>
                          <td style={{ fontFamily: 'monospace', fontSize: '12px' }}>#{order.orderId.slice(0, 8)}...</td>
                          <td>{order.restaurantName}</td>
                          <td style={{ fontWeight: 600 }}>₹{order.total.toFixed(2)}</td>
                          <td><span className={`status-badge status-${order.status}`}>{order.status.replace(/_/g, ' ')}</span></td>
                          <td style={{ color: '#999', fontSize: '13px' }}>{new Date(order.createdAt).toLocaleDateString('en-IN')}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default Dashboard;

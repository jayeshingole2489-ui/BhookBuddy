import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../contexts/CartContext';
import { useToast } from '../App';
import CartSidebar from '../components/cart/CartSidebar';

const Cart: React.FC = () => {
  const { cart, removeFromCart, updateQuantity, loading } = useCart();
  const { showToast } = useToast();
  const navigate = useNavigate();

  const handleQuantity = async (itemId: number, qty: number) => {
    if (qty < 1) {
      await handleRemove(itemId);
      return;
    }
    try {
      await updateQuantity(itemId, qty);
    } catch {
      showToast('Failed to update quantity', 'error');
    }
  };

  const handleRemove = async (itemId: number) => {
    try {
      await removeFromCart(itemId);
      showToast('Item removed', 'success');
    } catch {
      showToast('Failed to remove item', 'error');
    }
  };

  if (!cart || cart.items.length === 0) {
    return (
      <div style={{ minHeight: 'calc(100vh - 70px)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '40px 24px' }}>
        <div className="empty-state">
          <div className="empty-icon">🛒</div>
          <h3>Your cart is empty</h3>
          <p>Looks like you haven't added any items yet.</p>
          <Link to="/restaurants" className="btn-primary" style={{ marginTop: '16px', display: 'inline-flex' }}>
            Browse Restaurants
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '1000px', margin: '0 auto', padding: '32px 24px' }}>
      <CartSidebar />
      <h1 style={{ fontSize: '28px', fontWeight: 800, color: '#1A1A2E', marginBottom: '8px' }}>Your Cart</h1>
      <p style={{ fontSize: '15px', color: '#666', marginBottom: '32px' }}>
        From <strong>{cart.restaurantName}</strong> · {cart.itemCount} item{cart.itemCount !== 1 ? 's' : ''}
      </p>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 340px', gap: '32px', alignItems: 'start' }}>
        {/* Items */}
        <div>
          {cart.items.map((item) => (
            <div key={item.id} style={{
              display: 'flex',
              alignItems: 'center',
              gap: '16px',
              padding: '20px',
              background: 'white',
              borderRadius: '12px',
              marginBottom: '12px',
              boxShadow: '0 1px 6px rgba(0,0,0,0.06)',
            }}>
              {item.imageUrl && (
                <img src={item.imageUrl} alt={item.name} style={{ width: '70px', height: '70px', borderRadius: '8px', objectFit: 'cover', flexShrink: 0 }} />
              )}
              <div style={{ flex: 1 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                  <span>{item.vegetarian ? '🟢' : '🔴'}</span>
                  <span style={{ fontSize: '16px', fontWeight: 600, color: '#1A1A2E' }}>{item.name}</span>
                </div>
                <span style={{ fontSize: '14px', color: '#FF6B35', fontWeight: 600 }}>₹{item.price.toFixed(2)} each</span>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '12px' }}>
                <span style={{ fontSize: '16px', fontWeight: 700, color: '#1A1A2E' }}>₹{item.subtotal.toFixed(2)}</span>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <button
                    onClick={() => handleQuantity(item.id, item.quantity - 1)}
                    style={{
                      width: '30px', height: '30px', borderRadius: '50%',
                      border: '1.5px solid #E5E5E5', background: 'white',
                      cursor: 'pointer', fontWeight: 700, fontSize: '18px',
                      display: 'flex', alignItems: 'center', justifyContent: 'center',
                    }}
                  >−</button>
                  <span style={{ fontSize: '15px', fontWeight: 600, minWidth: '20px', textAlign: 'center' }}>{item.quantity}</span>
                  <button
                    onClick={() => handleQuantity(item.id, item.quantity + 1)}
                    style={{
                      width: '30px', height: '30px', borderRadius: '50%',
                      border: '1.5px solid #FF6B35', background: '#FF6B35',
                      cursor: 'pointer', color: 'white', fontWeight: 700, fontSize: '18px',
                      display: 'flex', alignItems: 'center', justifyContent: 'center',
                    }}
                  >+</button>
                  <button
                    onClick={() => handleRemove(item.id)}
                    style={{
                      background: 'none', border: 'none', cursor: 'pointer',
                      color: '#e74c3c', fontSize: '16px', padding: '4px',
                    }}
                  >
                    🗑
                  </button>
                </div>
              </div>
            </div>
          ))}

          <Link to="/restaurants" style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', color: '#FF6B35', fontWeight: 600, textDecoration: 'none', fontSize: '14px', marginTop: '8px' }}>
            ← Continue Shopping
          </Link>
        </div>

        {/* Summary */}
        <div style={{
          background: 'white',
          borderRadius: '16px',
          padding: '24px',
          boxShadow: '0 2px 12px rgba(0,0,0,0.08)',
          position: 'sticky',
          top: '90px',
        }}>
          <h2 style={{ fontSize: '18px', fontWeight: 700, marginBottom: '20px', color: '#1A1A2E' }}>Order Summary</h2>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginBottom: '20px', fontSize: '15px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: '#666' }}>Subtotal ({cart.itemCount} items)</span>
              <span>₹{cart.subtotal.toFixed(2)}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: '#666' }}>Delivery fee</span>
              <span>₹{cart.deliveryFee.toFixed(2)}</span>
            </div>
            <div style={{ height: '1px', background: '#E5E5E5' }} />
            <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 700, fontSize: '18px' }}>
              <span>Total</span>
              <span style={{ color: '#FF6B35' }}>₹{cart.total.toFixed(2)}</span>
            </div>
          </div>
          <button
            onClick={() => navigate('/checkout')}
            className="btn-primary"
            disabled={loading}
            style={{ width: '100%', justifyContent: 'center', padding: '14px', fontSize: '15px' }}
          >
            Proceed to Checkout →
          </button>
          <p style={{ textAlign: 'center', fontSize: '12px', color: '#999', marginTop: '12px' }}>
            🔒 Secure checkout
          </p>
        </div>
      </div>
    </div>
  );
};

export default Cart;

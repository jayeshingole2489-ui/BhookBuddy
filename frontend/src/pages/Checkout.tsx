import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart } from '../contexts/CartContext';
import { useAuth } from '../contexts/AuthContext';
import { useToast } from '../App';
import { orderService } from '../services/orderService';
import { Address } from '../types';
import LoadingSpinner from '../components/common/LoadingSpinner';

const Checkout: React.FC = () => {
  const { cart } = useCart();
  const { user } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();

  const [addresses, setAddresses] = useState<Address[]>([]);
  const [loadingAddresses, setLoadingAddresses] = useState(true);
  const [selectedAddressId, setSelectedAddressId] = useState<number | null>(null);
  const [showNewAddressForm, setShowNewAddressForm] = useState(false);
  const [notes, setNotes] = useState('');
  const [placing, setPlacing] = useState(false);

  const [newAddress, setNewAddress] = useState({
    label: 'Home',
    street: '',
    city: '',
    state: '',
    zipCode: '',
    country: 'India',
  });
  const [addressErrors, setAddressErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (!user) return;
    orderService.getUserAddresses(user.id)
      .then((addrs) => {
        setAddresses(addrs);
        const def = addrs.find((a) => a.isDefault);
        if (def) setSelectedAddressId(def.id);
        else if (addrs.length > 0) setSelectedAddressId(addrs[0].id);
        else setShowNewAddressForm(true);
      })
      .catch(() => setShowNewAddressForm(true))
      .finally(() => setLoadingAddresses(false));
  }, [user]);

  const validateNewAddress = (): boolean => {
    const errs: Record<string, string> = {};
    if (!newAddress.street.trim()) errs.street = 'Street address is required';
    if (!newAddress.city.trim()) errs.city = 'City is required';
    if (!newAddress.state.trim()) errs.state = 'State is required';
    if (!newAddress.zipCode.trim()) errs.zipCode = 'ZIP code is required';
    setAddressErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const getDeliveryAddressString = (): string => {
    if (!showNewAddressForm && selectedAddressId) {
      const addr = addresses.find((a) => a.id === selectedAddressId);
      if (addr) return `${addr.street}, ${addr.city}, ${addr.state} ${addr.zipCode}, ${addr.country}`;
    }
    if (showNewAddressForm) {
      return `${newAddress.street}, ${newAddress.city}, ${newAddress.state} ${newAddress.zipCode}, ${newAddress.country}`;
    }
    return '';
  };

  const handlePlaceOrder = async () => {
    if (!cart) return;
    if (showNewAddressForm && !validateNewAddress()) return;
    const deliveryAddress = getDeliveryAddressString();
    if (!deliveryAddress) {
      showToast('Please select or enter a delivery address', 'error');
      return;
    }
    try {
      setPlacing(true);
      // Save new address if needed
      if (showNewAddressForm && user) {
        await orderService.addAddress(user.id, { ...newAddress, isDefault: addresses.length === 0 });
      }
      const order = await orderService.create({ cartId: cart.id, deliveryAddress, notes: notes.trim() || undefined });
      navigate(`/payment/${order.orderId}`);
    } catch (error: unknown) {
      const msg = error instanceof Error ? error.message : 'Failed to place order';
      showToast(msg, 'error');
    } finally {
      setPlacing(false);
    }
  };

  if (!cart || cart.items.length === 0) {
    navigate('/cart');
    return null;
  }

  return (
    <div style={{ maxWidth: '1000px', margin: '0 auto', padding: '32px 24px' }}>
      <h1 style={{ fontSize: '28px', fontWeight: 800, color: '#1A1A2E', marginBottom: '32px' }}>Checkout</h1>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 340px', gap: '32px', alignItems: 'start' }}>
        {/* Left: Address */}
        <div>
          <div style={{ background: 'white', borderRadius: '16px', padding: '24px', boxShadow: '0 2px 12px rgba(0,0,0,0.06)', marginBottom: '20px' }}>
            <h2 style={{ fontSize: '18px', fontWeight: 700, marginBottom: '20px', color: '#1A1A2E' }}>📍 Delivery Address</h2>

            {loadingAddresses ? <LoadingSpinner /> : (
              <>
                {addresses.length > 0 && (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginBottom: '16px' }}>
                    {addresses.map((addr) => (
                      <label
                        key={addr.id}
                        style={{
                          display: 'flex',
                          alignItems: 'flex-start',
                          gap: '12px',
                          padding: '14px 16px',
                          border: '2px solid',
                          borderColor: selectedAddressId === addr.id && !showNewAddressForm ? '#FF6B35' : '#E5E5E5',
                          borderRadius: '10px',
                          cursor: 'pointer',
                          background: selectedAddressId === addr.id && !showNewAddressForm ? '#FFF3EE' : 'white',
                          transition: 'all 0.15s',
                        }}
                        onClick={() => { setSelectedAddressId(addr.id); setShowNewAddressForm(false); }}
                      >
                        <input
                          type="radio"
                          checked={selectedAddressId === addr.id && !showNewAddressForm}
                          onChange={() => { setSelectedAddressId(addr.id); setShowNewAddressForm(false); }}
                          style={{ marginTop: '2px', accentColor: '#FF6B35' }}
                        />
                        <div>
                          <div style={{ fontWeight: 600, fontSize: '14px', marginBottom: '4px', color: '#1A1A2E' }}>
                            {addr.label} {addr.isDefault && <span style={{ fontSize: '11px', background: '#FFF3EE', color: '#FF6B35', padding: '2px 6px', borderRadius: '8px', marginLeft: '4px' }}>Default</span>}
                          </div>
                          <div style={{ fontSize: '13px', color: '#666' }}>
                            {addr.street}, {addr.city}, {addr.state} {addr.zipCode}
                          </div>
                        </div>
                      </label>
                    ))}
                    <button
                      onClick={() => setShowNewAddressForm(!showNewAddressForm)}
                      style={{
                        padding: '10px 16px',
                        border: '2px dashed',
                        borderColor: showNewAddressForm ? '#FF6B35' : '#E5E5E5',
                        borderRadius: '10px',
                        background: 'white',
                        color: '#FF6B35',
                        fontWeight: 600,
                        fontSize: '14px',
                        cursor: 'pointer',
                      }}
                    >
                      + Add new address
                    </button>
                  </div>
                )}

                {(showNewAddressForm || addresses.length === 0) && (
                  <div>
                    {addresses.length > 0 && <h3 style={{ fontSize: '15px', fontWeight: 600, marginBottom: '16px', color: '#1A1A2E' }}>New Address</h3>}
                    <div className="form-group">
                      <label>Label</label>
                      <select value={newAddress.label} onChange={(e) => setNewAddress((p) => ({ ...p, label: e.target.value }))}>
                        <option>Home</option>
                        <option>Work</option>
                        <option>Other</option>
                      </select>
                    </div>
                    <div className="form-group">
                      <label>Street address</label>
                      <input
                        type="text"
                        value={newAddress.street}
                        onChange={(e) => setNewAddress((p) => ({ ...p, street: e.target.value }))}
                        className={addressErrors.street ? 'error' : ''}
                        placeholder="123 Main Street, Apt 4B"
                      />
                      {addressErrors.street && <span className="form-error">{addressErrors.street}</span>}
                    </div>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                      <div className="form-group">
                        <label>City</label>
                        <input type="text" value={newAddress.city} onChange={(e) => setNewAddress((p) => ({ ...p, city: e.target.value }))} className={addressErrors.city ? 'error' : ''} placeholder="Mumbai" />
                        {addressErrors.city && <span className="form-error">{addressErrors.city}</span>}
                      </div>
                      <div className="form-group">
                        <label>State</label>
                        <input type="text" value={newAddress.state} onChange={(e) => setNewAddress((p) => ({ ...p, state: e.target.value }))} className={addressErrors.state ? 'error' : ''} placeholder="Maharashtra" />
                        {addressErrors.state && <span className="form-error">{addressErrors.state}</span>}
                      </div>
                    </div>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                      <div className="form-group">
                        <label>ZIP code</label>
                        <input type="text" value={newAddress.zipCode} onChange={(e) => setNewAddress((p) => ({ ...p, zipCode: e.target.value }))} className={addressErrors.zipCode ? 'error' : ''} placeholder="400001" />
                        {addressErrors.zipCode && <span className="form-error">{addressErrors.zipCode}</span>}
                      </div>
                      <div className="form-group">
                        <label>Country</label>
                        <input type="text" value={newAddress.country} onChange={(e) => setNewAddress((p) => ({ ...p, country: e.target.value }))} placeholder="India" />
                      </div>
                    </div>
                  </div>
                )}
              </>
            )}
          </div>

          {/* Notes */}
          <div style={{ background: 'white', borderRadius: '16px', padding: '24px', boxShadow: '0 2px 12px rgba(0,0,0,0.06)' }}>
            <h2 style={{ fontSize: '18px', fontWeight: 700, marginBottom: '16px', color: '#1A1A2E' }}>📝 Order Notes</h2>
            <textarea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Special instructions, allergies, delivery notes... (optional)"
              style={{
                width: '100%',
                padding: '12px 16px',
                border: '1.5px solid #E5E5E5',
                borderRadius: '8px',
                fontSize: '14px',
                resize: 'vertical',
                minHeight: '90px',
                outline: 'none',
                boxSizing: 'border-box',
              }}
              onFocus={(e) => (e.target.style.borderColor = '#FF6B35')}
              onBlur={(e) => (e.target.style.borderColor = '#E5E5E5')}
            />
          </div>
        </div>

        {/* Right: Summary */}
        <div style={{ background: 'white', borderRadius: '16px', padding: '24px', boxShadow: '0 2px 12px rgba(0,0,0,0.08)', position: 'sticky', top: '90px' }}>
          <h2 style={{ fontSize: '18px', fontWeight: 700, marginBottom: '20px', color: '#1A1A2E' }}>Order Summary</h2>
          <div style={{ marginBottom: '16px' }}>
            {cart.items.map((item) => (
              <div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '10px', fontSize: '14px' }}>
                <span style={{ color: '#666' }}>{item.name} × {item.quantity}</span>
                <span>₹{item.subtotal.toFixed(2)}</span>
              </div>
            ))}
          </div>
          <div style={{ height: '1px', background: '#E5E5E5', marginBottom: '16px' }} />
          <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', fontSize: '14px', marginBottom: '20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: '#666' }}>Subtotal</span>
              <span>₹{cart.subtotal.toFixed(2)}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: '#666' }}>Delivery fee</span>
              <span>₹{cart.deliveryFee.toFixed(2)}</span>
            </div>
            <div style={{ height: '1px', background: '#E5E5E5' }} />
            <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 700, fontSize: '17px' }}>
              <span>Total</span>
              <span style={{ color: '#FF6B35' }}>₹{cart.total.toFixed(2)}</span>
            </div>
          </div>
          <button
            onClick={handlePlaceOrder}
            className="btn-primary"
            disabled={placing || loadingAddresses}
            style={{ width: '100%', justifyContent: 'center', padding: '14px', fontSize: '15px' }}
          >
            {placing ? 'Placing Order...' : 'Place Order →'}
          </button>
          <p style={{ textAlign: 'center', fontSize: '12px', color: '#999', marginTop: '12px' }}>
            🔒 Secure payment on next step
          </p>
        </div>
      </div>
    </div>
  );
};

export default Checkout;

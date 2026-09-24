import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Restaurant } from '../types';
import { restaurantService } from '../services/restaurantService';
import RestaurantCard from '../components/restaurant/RestaurantCard';
import LoadingSpinner from '../components/common/LoadingSpinner';
import CartSidebar from '../components/cart/CartSidebar';
import Footer from '../components/common/Footer';

const SearchResults: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  const query = searchParams.get('q') ?? '';
  const cuisineFilter = searchParams.get('cuisine') ?? '';

  const [results, setResults] = useState<Restaurant[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchInput, setSearchInput] = useState(query);

  useEffect(() => {
    setSearchInput(query);
    if (!query && !cuisineFilter) {
      setResults([]);
      return;
    }
    setLoading(true);
    restaurantService
      .search(query, cuisineFilter || undefined)
      .then(setResults)
      .catch(() => setResults([]))
      .finally(() => setLoading(false));
  }, [query, cuisineFilter]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchInput.trim()) {
      setSearchParams({ q: searchInput.trim() });
    }
  };

  const headingText = cuisineFilter
    ? `${cuisineFilter} Restaurants`
    : query
    ? `Results for "${query}"`
    : 'Search Results';

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <CartSidebar />

      <div style={{ flex: 1 }}>
        {/* Search bar */}
        <div
          style={{
            background: 'white',
            borderBottom: '1px solid #E5E5E5',
            padding: '20px 24px',
          }}
        >
          <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
            <form
              onSubmit={handleSearch}
              style={{ display: 'flex', gap: '12px', maxWidth: '560px' }}
            >
              <input
                type="text"
                value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
                placeholder="Search restaurants..."
                style={{
                  flex: 1,
                  padding: '12px 20px',
                  border: '1.5px solid #E5E5E5',
                  borderRadius: '24px',
                  fontSize: '15px',
                  outline: 'none',
                }}
                onFocus={(e) => (e.target.style.borderColor = '#FF6B35')}
                onBlur={(e) => (e.target.style.borderColor = '#E5E5E5')}
              />
              <button
                type="submit"
                className="btn-primary"
                style={{ whiteSpace: 'nowrap' }}
              >
                Search
              </button>
            </form>
          </div>
        </div>

        <div
          style={{ maxWidth: '1200px', margin: '0 auto', padding: '32px 24px' }}
        >
          <h1
            style={{
              fontSize: '22px',
              fontWeight: 700,
              color: '#1A1A2E',
              marginBottom: '6px',
            }}
          >
            {headingText}
          </h1>

          {!loading && (query || cuisineFilter) && (
            <p
              style={{
                fontSize: '14px',
                color: '#666',
                marginBottom: '24px',
              }}
            >
              {results.length} restaurant{results.length !== 1 ? 's' : ''}{' '}
              found
            </p>
          )}

          {loading ? (
            <LoadingSpinner />
          ) : !query && !cuisineFilter ? (
            <div className="empty-state">
              <div className="empty-icon">🔍</div>
              <h3>Start searching</h3>
              <p>Enter a restaurant name or cuisine to find results.</p>
            </div>
          ) : results.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">🔍</div>
              <h3>No results found</h3>
              <p>Try a different search term or browse all restaurants.</p>
              <button
                onClick={() => navigate('/restaurants')}
                className="btn-primary"
                style={{ marginTop: '16px' }}
              >
                Browse All Restaurants
              </button>
            </div>
          ) : (
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
                gap: '24px',
              }}
            >
              {results.map((r) => (
                <RestaurantCard key={r.id} restaurant={r} />
              ))}
            </div>
          )}
        </div>
      </div>

      <Footer />
    </div>
  );
};

export default SearchResults;

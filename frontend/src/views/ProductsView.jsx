import { useState, useEffect } from 'react';

export default function ProductsView() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [newProduct, setNewProduct] = useState({name: '', categoryId: 1, price: '', stockQuantity: '', supplierId: 1});

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      const res = await fetch('/api/products', {
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
      });
      const data = await res.json();
      setProducts(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleRestock = async (productId) => {
    const qty = prompt("Enter quantity to restock:");
    if (!qty || isNaN(qty) || qty <= 0) return;

    try {
      await fetch('/api/transactions/restock', {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        body: JSON.stringify({ productId, quantity: parseInt(qty) })
      });
      fetchProducts();
    } catch (err) {
      alert("Failed to restock");
    }
  };

  const handleAddProduct = async (e) => {
    e.preventDefault();
    try {
      await fetch('/api/products', {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        body: JSON.stringify({
          ...newProduct,
          price: parseFloat(newProduct.price),
          stockQuantity: parseInt(newProduct.stockQuantity)
        })
      });
      setShowForm(false);
      setNewProduct({name: '', categoryId: 1, price: '', stockQuantity: '', supplierId: 1});
      fetchProducts();
    } catch(err) { alert('Error adding product'); }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Products Inventory</h1>
        <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancel' : 'Add Product'}
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleAddProduct} className="glass-panel" style={{padding: '24px', marginTop: '24px', display: 'flex', gap: '12px', alignItems: 'flex-end', flexWrap: 'wrap'}}>
          <div className="input-group" style={{marginBottom: 0, flex: 1, minWidth: '200px'}}>
            <label>Name</label>
            <input type="text" required value={newProduct.name} onChange={e => setNewProduct({...newProduct, name: e.target.value})} />
          </div>
          <div className="input-group" style={{marginBottom: 0, width: '120px'}}>
            <label>Price</label>
            <input type="number" step="0.01" required value={newProduct.price} onChange={e => setNewProduct({...newProduct, price: e.target.value})} />
          </div>
          <div className="input-group" style={{marginBottom: 0, width: '120px'}}>
            <label>Stock</label>
            <input type="number" required value={newProduct.stockQuantity} onChange={e => setNewProduct({...newProduct, stockQuantity: e.target.value})} />
          </div>
          <button type="submit" className="btn btn-primary" style={{height: '46px'}}>Save</button>
        </form>
      )}

      {loading ? <p>Loading products...</p> : (
        <div className="table-container glass-panel" style={{marginTop: '24px'}}>
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Price</th>
                <th>Stock</th>
                <th>Rating</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {products.map(p => (
                <tr key={p.id}>
                  <td>#{p.id}</td>
                  <td>{p.name}</td>
                  <td>${p.price.toFixed(2)}</td>
                  <td>
                    <span className={`badge ${p.stockQuantity < 20 ? 'badge-danger' : 'badge-success'}`}>
                      {p.stockQuantity} in stock
                    </span>
                  </td>
                  <td>{p.rating} ⭐</td>
                  <td>
                    <button className="btn btn-primary" style={{padding: '6px 12px', fontSize: '0.8rem'}} onClick={() => handleRestock(p.id)}>
                      Restock
                    </button>
                  </td>
                </tr>
              ))}
              {products.length === 0 && (
                <tr>
                  <td colSpan="6" style={{textAlign: 'center'}}>No products found.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

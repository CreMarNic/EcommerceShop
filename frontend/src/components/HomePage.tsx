import { useEffect, useState } from 'react';
import {
    addCartItem,
    getApiErrorMessage,
    getCart,
    getCurrentUserId,
    getProducts,
    type Product,
    type AuthenticatedUser,
} from '../api/ecommerceApi';
import { Header } from './Header';
import './HomePage.css';

type HomePageProps = {
    user?: AuthenticatedUser | null;
    onLogout: () => void;
};

export function HomePage({ user, onLogout }: HomePageProps) {
    const [products, setProducts] = useState<Product[]>([]);
    const [quantities, setQuantities] = useState<Record<number, number>>({});
    const [cartQuantity, setCartQuantity] = useState(0);
    const [addedProductId, setAddedProductId] = useState<number | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        async function loadPageData() {
            try {
                const [loadedProducts, cart] = await Promise.all([
                    getProducts(),
                    getCart(getCurrentUserId()),
                ]);

                setProducts(loadedProducts);
                setCartQuantity(getCartQuantity(cart.items));
            } catch (err) {
                setError(getApiErrorMessage(err, 'Could not load products'));
            } finally {
                setLoading(false);
            }
        }

        loadPageData();
    }, []);

    async function handleAddToCart(productId: number) {
        try {
            const quantity = quantities[productId] ?? 1;
            const cart = await addCartItem(getCurrentUserId(), productId, quantity);

            setCartQuantity(getCartQuantity(cart.items));
            setAddedProductId(productId);
            setTimeout(() => setAddedProductId(null), 1500);
        } catch (err) {
            setError(getApiErrorMessage(err, 'Could not add product to cart'));
        }
    }

    return (
        <>
            <title>HomePage</title>

            <Header cartQuantity={cartQuantity} user={user} onLogout={onLogout} />

            <div className="home-page">
                {loading && <div className="products-message">Loading products...</div>}
                {error && <div className="products-message">{error}</div>}

                {!loading && !error && (
                    <div className="products-grid">
                        {products.map((product) => (
                            <div key={product.id} className="product-container">
                                <div className="product-image-container">
                                    <img className="product-image" src={product.imageUrl}/>
                                </div>

                                <div className="product-name limit-text-to-2-lines">
                                    {product.name}
                                </div>

                                <div className="product-price">
                                    €{Number(product.price).toFixed(2)}
                                </div>

                                <div className="product-quantity-container">
                                    <select
                                        value={quantities[product.id] ?? 1}
                                        onChange={(event) =>
                                            setQuantities({
                                                ...quantities,
                                                [product.id]: Number(event.target.value),
                                            })
                                        }
                                    >
                                        {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map((quantity) => (
                                            <option key={quantity} value={quantity}>
                                                {quantity}
                                            </option>
                                        ))}
                                    </select>
                                </div>

                                <div className="product-spacer"></div>

                                <div
                                    className="added-to-cart"
                                    style={{ opacity: addedProductId === product.id ? 1 : 0 }}
                                >
                                    <img src="images/icons/checkmark.png"/>
                                    Added
                                </div>

                                <button
                                    className="add-to-cart-button button-primary"
                                    onClick={() => handleAddToCart(product.id)}
                                >
                                    Add to Cart
                                </button>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </>
    );
}

function getCartQuantity(items: { quantity: number }[]) {
    return items.reduce((total, item) => total + item.quantity, 0);
}

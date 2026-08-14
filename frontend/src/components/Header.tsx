import { Link } from 'react-router';
import type { KeyboardEvent } from 'react';
import type { AuthenticatedUser } from '../api/ecommerceApi';
import './Header.css';

type HeaderProps = {
    cartQuantity?: number;
    user?: AuthenticatedUser | null;
    onLogout: () => void;
    onLogoClick?: () => void;
    searchText?: string;
    onSearchTextChange?: (value: string) => void;
    onSearchSubmit?: () => void;
};

export function Header({
    cartQuantity = 0,
    user,
    onLogout,
    onLogoClick,
    searchText = '',
    onSearchTextChange,
    onSearchSubmit,
}: HeaderProps) {
    function handleSearchKeyDown(event: KeyboardEvent<HTMLInputElement>) {
        if (event.key === 'Enter') {
            onSearchSubmit?.();
        }
    }

    return (

        <div className="header">
            <div className="left-section">
                <Link to="/" className="header-link" onClick={onLogoClick}>
                    <span className="logo">EcommerceShop</span>
                    <span className="mobile-logo">ES</span>
                </Link>
            </div>

            <div className="middle-section">
                <input
                    className="search-bar"
                    type="text"
                    placeholder="Search products"
                    value={searchText}
                    onChange={(event) => onSearchTextChange?.(event.target.value)}
                    onKeyDown={handleSearchKeyDown}
                />

                <button className="search-button" onClick={onSearchSubmit}>
                    <img className="search-icon" src="images/icons/search-icon.png"/>
                </button>
            </div>

            <div className="right-section">
                <div className="auth-summary">
                    <div className="auth-email">{user?.username ?? 'Unknown user'}</div>
                    <div className="auth-status">Authenticated</div>
                </div>

                <button className="logout-button" onClick={onLogout}>
                    Logout
                </button>

                <Link className="orders-link header-link" to="/orders">
                    <span className="orders-text">Orders</span>
                </Link>

                <Link className="cart-link header-link" to="/checkout">
                    <img className="cart-icon" src="images/icons/cart-icon.png"/>
                    <div className="cart-quantity">{cartQuantity}</div>
                    <div className="cart-text">Cart</div>
                </Link>
            </div>
        </div>


    )
}

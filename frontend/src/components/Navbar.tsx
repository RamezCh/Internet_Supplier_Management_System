import {Button} from "../shared/Button.tsx";
import {Link} from "../shared/Link.tsx";
import { useState } from "react";
import { FaBars, FaTimes } from "react-icons/fa";

export const Navbar = () => {
    const [isMenuOpen, setIsMenuOpen] = useState(false);

    function logout() {
        const host = window.location.host === 'localhost:5173' ? 'http://localhost:8080' : window.location.origin
        window.open(host + '/logout', '_self')
    }

    function toggleMenu() {
        setIsMenuOpen(!isMenuOpen);
    }

    function closeMenu() {
        setIsMenuOpen(false);
    }

    return (
        <nav className="shadow-sm w-full">
            {/* Desktop navbar */}
            <div className="hidden md:flex flex-row justify-between w-full p-6">
                <div className="flex flex-row flex-wrap">
                    <Link to="/">Customers</Link>
                    <Link to="/customer/add">Add Customer</Link>
                    <Link to="/internet-plans">Internet Plans</Link>
                    <Link to="/internet-plan/add">Add Internet Plan</Link>
                </div>
                <Button variant="red" handleClick={logout}>
                    Sign Out
                </Button>
            </div>

            {/* Mobile navbar */}
            <div className="md:hidden p-4">
                <div className="flex justify-between items-center">
                    <Link to="/">ISMS</Link>
                    <button 
                        onClick={toggleMenu}
                        className="p-2 text-gray-700 hover:text-blue-600 focus:outline-none"
                    >
                        {isMenuOpen ? <FaTimes size={24} /> : <FaBars size={24} />}
                    </button>
                </div>

                {/* Mobile menu dropdown */}
                {isMenuOpen && (
                    <div className="mt-4 flex flex-col space-y-2 bg-white rounded-lg shadow-lg p-4 absolute z-10 left-0 right-0 border border-gray-200">
                        <Link to="/" onClick={closeMenu}>Customers</Link>
                        <Link to="/customer/add" onClick={closeMenu}>Add Customer</Link>
                        <Link to="/internet-plans" onClick={closeMenu}>Internet Plans</Link>
                        <Link to="/internet-plan/add" onClick={closeMenu}>Add Internet Plan</Link>
                        <div className="pt-2 border-t border-gray-200 mt-2">
                            <Button variant="red" handleClick={logout} className="w-full">
                                Sign Out
                            </Button>
                        </div>
                    </div>
                )}
            </div>
        </nav>
    );
}

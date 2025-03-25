import {Button} from "./Button.tsx";
import {Link} from "./Link.tsx";

export const Navbar = () => {

    function logout() {
        const host = window.location.host === 'localhost:5173' ? 'http://localhost:8080' : window.location.origin
        window.open(host + '/logout', '_self')
    }

    return <nav className="flex flex-row justify-between w-full p-6 shadow-sm">
        <div>
            <Link to="/customers">Customers</Link>
            <Link to="/customer/add">Add Customer</Link>
        </div>
        <Button variant="red"  handleClick={logout}>
            Sign Out
        </Button>
    </nav>
}
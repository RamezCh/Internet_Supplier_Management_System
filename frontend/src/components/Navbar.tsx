import {Button} from "../shared/Button.tsx";
import {Link} from "../shared/Link.tsx";

export const Navbar = () => {

    function logout() {
        const host = window.location.host === 'localhost:5173' ? 'http://localhost:8080' : window.location.origin
        window.open(host + '/logout', '_self')
    }

    return <nav className="flex flex-row justify-between w-full p-6 shadow-sm">
        <div>
            <Link to="/">Customers</Link>
            <Link to="/customer/add">Add Customer</Link>
            <Link to="/internet-plans">Internet Plans</Link>
            <Link to="/internet-plan/add">Add Internet Plan</Link>
        </div>
        <Button variant="red"  handleClick={logout}>
            Sign Out
        </Button>
    </nav>
}
import {Button} from "./Button.tsx";

export const Navbar = () => {

    function logout() {
        const host = window.location.host === 'localhost:5173' ? 'http://localhost:8080' : window.location.origin
        window.open(host + '/logout', '_self')
    }

    return <nav>
        <Button variant="red"  handleClick={logout}>
            Sign Out
        </Button>
    </nav>
}
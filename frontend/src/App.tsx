import {useEffect, useState} from 'react'
import axios from 'axios';
import {AppUser} from "./types.ts";
import {WelcomePage} from "./pages/WelcomePage.tsx";
import {Route, Routes} from "react-router-dom";
import ProtectedRoutes from "./shared/ProtectedRoutes.tsx";
import { Navbar } from "./shared/Navbar.tsx";
import {Customers} from "./pages/Customers.tsx";

function App() {
  const [appUser, setAppUser] = useState<AppUser | undefined | null>(undefined);

  function login() {
    // window.location.host is whats after www. and the port
    // e.g. localhost:5173
    // window.location.origin is complete url with port
    // e.g. http://localhost:5173
    // if url is same as localhost then we are in production
    // then our backend is at localhost:8080
    // if url isnt same as localhost then we are in development
    // then our backend is at url:8080
    const host = window.location.host === 'localhost:5173' ? 'http://localhost:8080' : window.location.origin
    window.open(host + '/oauth2/authorization/github', '_self')
  }

  function getMe() {
    axios.get("/api/auth/me")
        .then((r) => setAppUser(r.data))
        .catch(e => {
          setAppUser(null);
          console.error(e);
        })
  }

  useEffect(getMe, []);


    return (
    <Routes>
      <Route path="/" element={appUser ? <Navbar/> : <WelcomePage onGoogleLogin={login} onGitHubLogin={login}/>} />
      <Route element={<ProtectedRoutes appUser={appUser} />}>
        <Route path="/customers" element={<Customers/>} />
      </Route>
    </Routes>
  )
}

export default App

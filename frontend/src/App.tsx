import {useEffect, useState} from 'react'
import axios from 'axios';
import {AppUser} from "./types.ts";
import {WelcomePage} from "./pages/WelcomePage.tsx";
import {Route, Routes, useNavigate} from "react-router-dom";
import ProtectedRoutes from "./shared/ProtectedRoutes.tsx";
import { Navbar } from "./shared/Navbar.tsx";
import {Customers} from "./pages/Customers.tsx";

function App() {
  const [appUser, setAppUser] = useState<AppUser | undefined | null>(undefined);
  const navigate = useNavigate();

  function getMe() {
    axios.get("/api/auth/me")
        .then((r) => setAppUser(r.data))
        .catch(e => {
          setAppUser(null);
          console.error(e);
        })
  }

  useEffect(() => {
    getMe();
  }, []);

  useEffect(() => {
    if (appUser) {
      navigate("/customers");
    }
  }, [appUser, navigate]);

  return (
      <>
        {appUser && <Navbar/>}
        <Routes>
          <Route path="/" element={<WelcomePage/>} />
          <Route element={<ProtectedRoutes appUser={appUser} />}>
            <Route path="/customers" element={<Customers/>} />
          </Route>
        </Routes>
      </>
  )
}

export default App
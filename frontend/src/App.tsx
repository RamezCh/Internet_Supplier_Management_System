import {useEffect, useState} from 'react'
import axios from 'axios';
import {AppUser} from "./types.ts";
import {WelcomePage} from "./pages/WelcomePage.tsx";
import {Route, Routes} from "react-router-dom";
import ProtectedRoutes from "./components/ProtectedRoutes.tsx";
import { Navbar } from "./components/Navbar.tsx";
import {Customers} from "./pages/Customers.tsx";
import {AddCustomer} from "./pages/AddCustomer.tsx";
import {ToastContainer} from "react-toastify";
import {EditCustomer} from "./pages/EditCustomer.tsx";

function App() {
  const [appUser, setAppUser] = useState<AppUser | undefined | null>(undefined);

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

  return (
      <>
        {appUser && <Navbar/>}
          <div className="my-10 mx-7">
            <Routes>
              <Route path="/" element={appUser ? <Customers/> : <WelcomePage/>} />
              <Route element={<ProtectedRoutes appUser={appUser} />}>
                <Route path="/customer/add" element={<AddCustomer/>} />
                <Route path="/customer/:id/edit" element={<EditCustomer/>} />
              </Route>
            </Routes>
          </div>
        <ToastContainer position="top-center" autoClose={3000} />
      </>
  )
}

export default App
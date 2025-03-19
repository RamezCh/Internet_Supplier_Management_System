import {useEffect, useState} from 'react'
import './App.css'
import axios from 'axios';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState<boolean>(false)

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

  function logout() {
    const host = window.location.host === 'localhost:5173' ? 'http://localhost:8080' : window.location.origin
    window.open(host + '/logout', '_self')
  }

  function getMe() {
    axios.get("/api/auth/me")
        .then(() => setIsLoggedIn(true))
        .catch(e => console.error(e))
  }

  useEffect(getMe, []);


    return (
    <>
    {isLoggedIn ? <button onClick={logout}>Logout</button>
        : <button onClick={login}>Login with Github</button>
    }
    </>
  )
}

export default App

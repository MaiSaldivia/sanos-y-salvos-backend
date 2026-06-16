import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ToastProvider } from './components/ToastProvider';
import Navbar       from './components/Navbar';
import Footer       from './components/Footer';
import Home         from './pages/Home';
import Mascotas     from './pages/Mascotas';
import DetalleMascota from './pages/DetalleMascota';
import Reportar     from './pages/Reportar';
import Mapa         from './pages/Mapa';
import Login        from './pages/Login';
import Perfil       from './pages/Perfil';
import Panel        from './pages/Panel';
import Admin        from './pages/Admin';
import './styles/main.css';

function App() {
  return (
    <BrowserRouter>
      <ToastProvider>
        <div className="app-layout">
          <Navbar />
          <main className="app-main">
            <Routes>
              <Route path="/"             element={<Home />} />
              <Route path="/mascotas"     element={<Mascotas />} />
              <Route path="/mascotas/:id" element={<DetalleMascota />} />
              <Route path="/reportar"     element={<Reportar />} />
              <Route path="/mapa"         element={<Mapa />} />
              <Route path="/login"        element={<Login />} />
              <Route path="/perfil"       element={<Perfil />} />
              <Route path="/panel"        element={<Panel />} />
              <Route path="/admin"        element={<Admin />} />
              <Route path="*" element={
                <div className="container" style={{padding:'80px 24px', textAlign:'center'}}>
                  <div style={{fontSize:'4rem'}}>🐾</div>
                  <h2 style={{marginTop:'16px'}}>Página no encontrada</h2>
                  <a href="/" className="btn btn-primary" style={{marginTop:20}}>Ir al inicio</a>
                </div>
              } />
            </Routes>
          </main>
          <Footer />
        </div>
      </ToastProvider>
    </BrowserRouter>
  );
}

export default App;

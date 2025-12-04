import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import DataFetcher from './DataFetchar.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <DataFetcher />
  </StrictMode>,
)

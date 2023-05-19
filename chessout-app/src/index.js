//import React from 'react';
//import ReactDOM from 'react-dom/client';
//import './index.css';
//import App from './App';
//import reportWebVitals from './reportWebVitals';
//import { theme, ThemeProvider, CSSReset, ColorModeProvider } from "@chakra-ui/core"; 


//const root = ReactDOM.createRoot(document.getElementById('root'));
//root.render(
 // <React.StrictMode>
  //  <App />
    
 // </React.StrictMode>
//);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
//reportWebVitals();

import React from 'react';  
import ReactDOM from 'react-dom';  
import './index.css';  
import App from './App';  
//import * as serviceWorker from './serviceWorker';  
import { theme, ThemeProvider, CSSReset, ColorModeProvider } from "@chakra-ui/core";  
  
  
ReactDOM.render(  
  <ThemeProvider theme={theme}>  
    <ColorModeProvider>  
      <CSSReset />  
      <React.StrictMode>  
        <App />  
      </React.StrictMode>  
    </ColorModeProvider>  
  </ThemeProvider>,  
  document.getElementById('root')  
);  
  
//serviceWorker.unregister();  

import React from 'react';
import ReactDOM from 'react-dom';
import { BrowserRouter, Route} from "react-router-dom";

import StartPage from './pages/StartPage';


import './styles/index.css';

ReactDOM.render(
  <BrowserRouter>
    <Route exact path="/" component={StartPage} />
  </BrowserRouter>,
  document.getElementById('root')
);

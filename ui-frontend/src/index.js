import React from 'react';
import ReactDOM from 'react-dom';
import { BrowserRouter, Route} from "react-router-dom";

import StartPage from './pages/StartPage';
import ProfilePage from './pages/ProfilePage';


import './styles/index.css';

ReactDOM.render(
  <BrowserRouter>
    <Route exact path="/" component={StartPage} />
    <Route exact path="/profile" component={ProfilePage} />
  </BrowserRouter>,
  document.getElementById('root')
);

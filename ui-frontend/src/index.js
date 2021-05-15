import React from 'react';
import ReactDOM from 'react-dom';
import { BrowserRouter, Route} from "react-router-dom";

import StartPage from './pages/StartPage';
import ProfilePage from './pages/ProfilePage';
import MainPage from './pages/MainPage';


import './styles/index.css';
import AboutPage from './pages/AboutPage';

ReactDOM.render(
  <BrowserRouter>
    <Route exact path="/" component={StartPage} />
    <Route exact path="/profile" component={ProfilePage} />
    <Route exact path="/home" component={MainPage} />
    <Route exact path="/about" component={AboutPage} />
  </BrowserRouter>,
  document.getElementById('root')
);

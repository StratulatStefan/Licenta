import React from 'react';
import ReactDOM from 'react-dom';
import { BrowserRouter, Route} from "react-router-dom";

import StartPage from './pages/StartPage';
import ProfilePage from './pages/ProfilePage';
import MainUserPage from './pages/MainUserPage';
import MainAdminPage from './pages/MainAdminPage';
import UploadPage from './pages/UploadPage';
import AboutPage from './pages/AboutPage';

import './styles/index.css';


ReactDOM.render(
  <BrowserRouter>
    <Route exact path="/" component={StartPage} />
    <Route exact path="/profile" component={ProfilePage} />
    <Route exact path="/uhome" component={MainUserPage} />
    <Route exact path="/ahome" component={MainAdminPage} />
    <Route exact path="/about" component={AboutPage} />
    <Route exact path="/upload" component={UploadPage}/>
  </BrowserRouter>,
  document.getElementById('root')
);

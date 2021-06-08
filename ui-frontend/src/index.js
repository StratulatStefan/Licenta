import React                   from 'react';
import ReactDOM                from 'react-dom';
import { BrowserRouter, Route} from "react-router-dom";

import StartPage       from './pages/StartPage';
import ProfilePage     from './pages/ProfilePage';
import MainUserPage    from './pages/MainUserPage';
import AdminMainPage   from './pages/AdminMainPage';
import UploadPage      from './pages/UploadPage';
import FileDetailsPage from './pages/FileDetailsPage';

import './styles/index.css';


ReactDOM.render(
  <BrowserRouter>
    <Route exact path="/"        component={StartPage} />
    <Route exact path="/profile" component={ProfilePage} />
    <Route exact path="/uhome"   component={MainUserPage} />
    <Route exact path="/ahome"   component={AdminMainPage} />
    <Route exact path="/upload"  component={UploadPage}/>
    <Route exact path="/details" component={FileDetailsPage}/>
  </BrowserRouter>,
  document.getElementById('root'),
);
import React, { Component } from 'react';
//import {UsersHandlerService} from '../services/UsersHandlerService';

import '../styles/pages-style.css';

class AboutPage extends Component {
    constructor(props){
        super(props)
        document.getElementById("page-name").innerHTML = "About Page";
        this.userData = localStorage.getItem('user_data')
        this.state = {
            isUserConnected : false,
            userType : "",
            accountAvailable : true,
            accountSuccessfullyCreated : false
        }
    }

    render() {
        return(
            <div className="App">

            </div>
        );
    }
}

export default AboutPage;

import React, { Component } from 'react';
import useStateWithCallback from 'use-state-with-callback';

import '../styles/pages-style.css';

class StartPage extends Component {
    constructor(props){
        super(props)
        document.getElementById("page-name").innerHTML = "Home Page";
        this.userData = localStorage.getItem('user_data')
        this.state = {
            isUserConnected : false,
            userType : "",
        }

    }

    componentDidMount = () => {
        this.checkForConnectedUser()
    }
    checkForConnectedUser = () => {
        this.userData = localStorage.getItem('user_data')
        var status = (this.userData !== null && this.userData !== '')
        this.setState({
            isUserConnected : status,
        });
        if(status === true){
            document.getElementById("log_data_uname").innerHTML = this.userData;
            document.getElementById("log_data_profile").style.visibility = "visible";
        }
        else{
            document.getElementById("log_data_uname").innerHTML = "";
            document.getElementById("log_data_profile").style.visibility = "hidden";
        }
    }

    login = () => {
        localStorage.setItem("user_data", "Stratulat Stefan")
        this.checkForConnectedUser()
        
    }

    logout = () => {
        localStorage.setItem("user_data", '')
        this.checkForConnectedUser()
    }
  
    render() {
      return (
        <div className="App">
          <p id="title">Home</p>
          {this.state.isUserConnected === false ?
          <button className="redirector" onClick={this.login}>Autentificare</button>:
          <button className="redirector" onClick={this.logout}>Logout</button>}
        </div>
      );
    }
  }
  
  export default StartPage;
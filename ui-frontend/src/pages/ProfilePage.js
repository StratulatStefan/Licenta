import React, { Component } from 'react';

import '../styles/pages-style.css';

class ProfilePage extends Component {
    constructor(props){
        super(props)
        document.getElementById("page-name").innerHTML = "Profile Page";
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
            this.userData = JSON.parse(this.userData)
            document.getElementById("log_data_uname").innerHTML = this.userData["name"];
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
          <p id="title">Profile Page</p>
          {this.state.isUserConnected === false ?
          <button className="redirector" onClick={this.login}>Autentificare</button>:
          <button className="redirector" onClick={this.logout}>Logout</button>}
        </div>
      );
    }
  }
  
  export default ProfilePage;
import React, { Component } from 'react';
import {UsersHandlerService} from '../services/UsersHandlerService';

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
        this.fetchUserType()
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

    fetchUserType = () => {
        if(this.userData !== null && this.userData !== ''){
            UsersHandlerService.getUserRole(this.userData["jwt"]).then(response => {
                if(response.code === 1){
                    console.log("role : " + response["content"])
                    this.setState({userType : response["content"]})
                }
                else if(response.code === 401){
                    this.logout()

                }
            })
        }
    }

    login = () => {
        let username = "stefanc.stratulat@gmail.com"
        let password = "parola.dropbox123"
        UsersHandlerService.login(username, password).then(response => {
            if(response.code === 1){
                localStorage.setItem("user_data", JSON.stringify(response.content))
                this.checkForConnectedUser()
                this.fetchUserType()
            }
            else{
                console.log(response.content)
            }
        })
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
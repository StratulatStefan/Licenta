import React, { Component } from 'react';
import {UsersHandlerService} from '../services/UsersHandlerService';

import '../styles/pages-style.css';
import '../styles/pages-home-style.css';

class MainPage extends Component {
    constructor(props){
        super(props)
        document.getElementById("page-name").innerHTML = "Home Page";
        this.userData = localStorage.getItem('user_data')
        this.state = {
            isUserConnected : false,
            userType : "",
            accountAvailable : true,
            accountSuccessfullyCreated : false
        }
    }

    componentDidMount = () => {
        this.checkForConnectedUser()
        this.fetchUserType()
    }

    fetchUserType = () => {
        try{
            this.setState({userType : this.props.location.state.detail["user_type"]})
        }
        catch(e){
            // am ajuns pe aceasta pagina din alta parte, prin click pe meniu, prin scriere directa in link
            if(this.userData !== null && this.userData !== ''){
                this.userData = JSON.parse(this.userData)
                UsersHandlerService.getUserRole(this.userData["jwt"]).then(response => {
                    if(response.code === 1){
                        console.log(`props fetch: ${response["content"]}`)
                        this.setState({userType : response["content"]})
                    }
                    else if(response.code === 401){
                        localStorage.setItem("user_data", '')
                    }
                })
            }
        }
    }

    checkForConnectedUser = () => {
        this.userData = localStorage.getItem('user_data')
        if(this.userData === null || this.userData === ''){
            this.redirect("")
        }
    }

    redirect = (destination) => {
        if(destination !== ""){
            this.props.history.push(destination)
        }
        else{
            this.props.history.push("/")
        }
    }

    render(){
        return(
            <div className="App">
                <div className="Home">
                    <div className="home_header">
                        <p>My files</p>
                        <div className="home_searchbar">
                            <input type="text" placeholder="Search file.."/>
                            <button>&#128269;</button>
                        </div>
                    </div>
                    <hr/>
                </div>
            </div>
        );
    }
}

export default MainPage;

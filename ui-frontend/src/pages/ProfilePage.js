import React, { Component } from 'react';
import {UsersHandlerService} from '../services/UsersHandlerService';

import '../styles/pages-style.css';

class ProfilePage extends Component {
    static userDetailsCategories = ["general_info", "storage_status", "plan"]
    static availableUserTypes = null
    
    constructor(props){
        super(props)
        document.getElementById("page-name").innerHTML = "Profile Page";
        this.userData = localStorage.getItem('user_data')
        this.state = {
            isUserConnected : false,
            userType : "",
            userDetailsCategory: ProfilePage.userDetailsCategories[0],
            additionalUserData : null
        }

    }

    componentDidUpdate = () => {
        ProfilePage.userDetailsCategories.forEach(category => {
            document.getElementById(`selector_${category}`).style.borderBottom = 
                (category === this.state.userDetailsCategory) ? "5px solid #23049d" : "none";
        })

        if(this.state.userDetailsCategory === ProfilePage.userDetailsCategories[0] && this.state.additionalUserData === null){
            this.fetchAdditionalUserData()
        }
    }

    componentDidMount = () => {
        this.fetchAvailableUserTypes()
        this.checkForConnectedUser()
    }

    fetchAvailableUserTypes = () => {
        UsersHandlerService.getAvailableUserTypes().then(response => {
            if(response.code === 1){
                ProfilePage.availableUserTypes = {}
                response.content.forEach(usertype => {
                    let utype = usertype["user_type"]
                    delete usertype["user_type"]
                    ProfilePage.availableUserTypes[utype] = usertype
                })
                console.log(response.content)
            }
            else{
                console.log(response.content)
            }
        })
    }

    fetchUserType = () => {
        if(this.userData !== null && this.userData !== ''){
            UsersHandlerService.getUserRole(this.userData["jwt"]).then(response => {
                if(response.code === 1){
                    console.log("role : " + response["content"])
                    this.setState({userType : response["content"]})
                }
                else if(response.code === 401){
                    localStorage.setItem("user_data", '')
                }
            })
        }
    }

    fetchAdditionalUserData = () => {
        console.log("Fetching additional user data..")
        UsersHandlerService.getAdditionalUserData(this.userData["jwt"]).then(response => {
            if(response.code === 1){
                this.setState({additionalUserData : response.content})
                console.log(response.content)
            }
            else{
                console.log(response.content)
            }
        })
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

    updatePlan = (newplan) => {
        document.getElementById("updatePlanStatus").innerHTML = `Plan updating to ${newplan}...`
        UsersHandlerService.updatePlan(this.userData["jwt"], newplan).then(response => {
            if(response.code === 1){
                document.getElementById("updatePlanStatus").innerHTML = `Plan successfully changed to ${newplan}.`
                console.log(response.content)
                this.fetchAdditionalUserData()
            }
            else{
                console.log(response.content)
            }
        })
    }

    render() {
        let userDetails = <div></div>
        if(this.state.additionalUserData !== null){
            switch(this.state.userDetailsCategory){
                case "general_info" : {
                    userDetails = 
                        <div className = "accountData">
                            <p className="accountDataField" >
                                Name
                                <span className = "accountDataValue">{this.state.additionalUserData["name"]}</span>
                            </p>
                            <p className="accountDataField" >
                                Email
                                <span className = "accountDataValue">{this.state.additionalUserData["email"]}</span>
                            </p>
                            <p className="accountDataField" >
                                Country
                                <span className = "accountDataValue">{this.state.additionalUserData["country"]}</span>
                            </p>
                            <p className="accountDataField" >
                                Account Plan
                                <span className = "accountDataValue">{this.state.additionalUserData["type"]}</span>
                            </p>
                        </div>
                    break
                }
                case "storage_status" : {
                    let total_storage = ProfilePage.availableUserTypes[this.state.additionalUserData["type"]]["available_storage"]
                    let available_storage = this.state.additionalUserData["storage_quantity"]
                    let used_storage = total_storage - available_storage
                    let number_of_files = 0
                    userDetails = 
                        <div className = "accountData">
                            <p className="accountDataField" >
                                Total Storage
                                <span className = "accountDataValue">
                                    {total_storage} KB ({total_storage >> 20} GB)
                                </span>
                            </p>
                            <p className="accountDataField" >
                                Used Storage
                                <span className = "accountDataValue">
                                    {used_storage} KB ({used_storage >> 20} GB)
                                </span>
                            </p>
                            <p className="accountDataField" >
                                Available Storage
                                <span className = "accountDataValue">
                                    {available_storage} KB ({available_storage >> 20} GB)
                                </span>
                            </p>
                            <p className="accountDataField" >
                                Number of files
                                <span className = "accountDataValue">{number_of_files}</span>
                            </p>
                        </div>
                    break
                }
                case "plan" : {
                    let businessPlans = []
                    Object.keys(ProfilePage.availableUserTypes).forEach(usertype => {
                        if(usertype !== this.state.additionalUserData["type"]){
                            businessPlans.push(
                                <div className="upgradeplan">
                                    <p>{usertype}</p>
                                    <p>{ProfilePage.availableUserTypes[usertype]["available_storage"]  >> 20} GB</p>
                                    <p>{ProfilePage.availableUserTypes[usertype]["price_dollars"]} $</p>
                                    <button className="redirector" onClick={() => this.updatePlan(usertype)}>Update to {usertype}</button>
                                    <br/>
                                    <br/>
                                </div>,
                            )
                        }
                    })
                    userDetails = 
                        <div className = "accountData">
                            <p>Do you want to update your business plan ? </p>
                            <p>Select one of the followings</p>
                            <br/><br/>
                            {businessPlans}
                            <p id="updatePlanStatus"></p>
                        </div>
                    break
                }
            }
        }
        else{
            userDetails = <p>Fetching user data..</p>
        }

        return (
            <div className="App">
                {this.state.isUserConnected === true ?
                    <div>
                        <img id="log_data_profile" src="images/user_logo.png" />
                        <p id="username">{this.userData.name}</p>
                        <ul>
                            <li><a id="selector_general_info" href="#" onClick={() => {
                                this.setState({userDetailsCategory: ProfilePage.userDetailsCategories[0]})
                            }}>General Info</a>
                            </li>
                            <li><a id="selector_storage_status"href="#" onClick={() => {
                                this.setState({userDetailsCategory: ProfilePage.userDetailsCategories[1]})
                                }}>Storage Status</a>
                            </li>
                            <li><a id="selector_plan" href="#" onClick={() => {
                                this.setState({userDetailsCategory: ProfilePage.userDetailsCategories[2]})
                                }}>Plan</a>
                            </li>
                        </ul>
                        {userDetails}
                    </div>: 
                    <p>Nu puteti accesa aceasta pagina daca utilizatorul nu este conectat</p>}
            </div>
      );
    }
  }
  
  export default ProfilePage;
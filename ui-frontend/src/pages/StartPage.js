import React, { Component } from 'react';
import '../styles/pages-style.css';

class StartPage extends Component {
    constructor(props){
      super(props)
      document.getElementById("page-name").innerHTML = "Main Menu";
    }
  
    render() {
      return (
        <div className="App">
          <p id="description">Main Menu</p>

        </div>
      );
    }
  }
  
  export default StartPage;
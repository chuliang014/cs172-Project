/* eslint jsx-a11y/anchor-is-valid: 0*/

import React from 'react';
import {
  Route
} from "react-router-dom";

class Search extends React.Component {
  constructor(props){
    super(props)
    this.state = {
        engine: "lucene",
        searchInput: "",
        loading: false
    }
  }

  performSearch(){
      this.props.history.push(`/search/${this.state.engine}/${this.state.searchInput}`)
  }

  render(){
    return (
        <div>
            <nav className="uk-navbar-container" uk-navbar="">
                <div className="uk-navbar-left">
                    <a className="uk-navbar-item uk-logo" href="/">Twitter Search</a>
                </div>
                <div className="uk-navbar-right">
                    <ul className="uk-navbar-nav">
                        <li className="uk-active"><a href="/">Engine: </a></li>
                        <li>
                            <a href="/">{this.state.engine}</a>
                            <div className="uk-navbar-dropdown">
                                <ul className="uk-nav uk-navbar-dropdown-nav">
                                    <li className={this.state.engine==="lucene"?"uk-active":""}><a onClick={()=>{
                                        this.setState({engine: 'lucene'})
                                    }}>Lucene</a></li>
                                    
                                </ul>
                            </div>
                        </li>
                    </ul>
                </div>
            </nav>
            <div style={{paddingTop: "109px"}}>
                <h1 className="uk-article-title uk-align-center uk-text-center">
                    Twitter Search
                </h1>
                <div className="uk-margin" style={{margin: "0 30px"}}>
                    <div style={{width: "100%"}} className="uk-search uk-search-default">
                        <span uk-search-icon=""></span>
                        <input className="uk-search-input uk-form-large" type="search" placeholder="Search..."
                            onChange={(e)=>this.setState({searchInput: e.target.value})} 
                            onKeyDown={(e)=>{
                                if(e.key === "Enter"){
                                    e.preventDefault()
                                    e.stopPropagation()
                                    this.performSearch()
                                }
                            }
                        }
                        />
                    </div>
                </div>
                <div className="uk-flex uk-flex-center uk-margin">
                    <button onClick={()=>{this.performSearch()}} to={"/search/"+this.state.searchInput} className="uk-button uk-button-primary uk-button-large">
                        {this.state.loading ? <div uk-spinner=""></div> : "Search"}
                    </button>
                </div>
            </div>
            <div style={{position: "absolute", bottom: "0px", right: "0px", overflow: "hidden"}}>
                <img style={{width: "300px"}} src="https://i.imgur.com/9ykNEPr.jpg" alt=""></img>
            </div>
        </div>
    )
  }
}

export default (props) => <Route {...props} component={Search}/>;

import React from 'react';
import './App.css';
import Search from './pages/Search';
import Results from './pages/Results';
import {
  BrowserRouter as Router,
  Switch,
  Route,
} from "react-router-dom";
import 'uikit/dist/css/uikit.min.css';
import 'uikit/dist/js/uikit';
import 'uikit/dist/js/uikit-icons';

class App extends React.Component {
  constructor(props){
    super(props)
    this.state = {

    }
  }

  render(){
    return (
      <Router>
        <div className="App">
          <Switch>
            <Route path="/search/:engine/:query" component={Results}/>
            <Route path="/" component={Search}/>
        </Switch>
        </div>
      </Router>
    );
  }
}

export default App;

import React from 'react';
import axios from 'axios'

function Result(props){
    let result = props.result
    return (
        <div className="uk-card uk-card-default uk-card-body uk-margin">
            <a href={result.url} target="_blank"><h3 className="uk-card-title">{result.text}</h3></a>
            <p>
                {result.body ? result.body.slice(0, 1000) : ""}
            </p>
            <span>
                {result.created_at} Score: {result.score}
            </span>
            

        </div>
    )
}

class Results extends React.Component {
  constructor(props){
    super(props)
    this.state = {
        searchInput: "",
        results: [],
        loading: true
    }
  }

  like(r, i){
    let results = this.state.results;
    results[i].likes++
    this.setState({
        results: [...results]
    })
  }

  dislike(r, i){
    let results = this.state.results;
    results[i].dislikes++
    this.setState({
        results: [...results]
    })
  }

  componentDidMount(){
      let {params} = this.props.match
      console.log(params.query)
      axios({
          method: 'get',
          url: `http://localhost:2019/api/search`,
          params: {
              query: params.query,
              engine: params.engine
          }
      }).then((res)=>{
        this.setState({
            loading: false,
            results: res.data.data
        })
      })
  }

  render(){
    let loading = (
        <div className="uk-flex uk-flex-center uk-margin uk-margin-xlarge-top">
            <div>
                <span uk-spinner="ratio: 3"></span>
            </div>
        </div>
    )
    let content = (
        <div style={{margin: "30px"}} uk-scrollspy="cls: uk-animation-slide-bottom; target: .uk-card; delay: 35">
                {/* {this.state.results.sort((a, b)=>{
                    let s1 = a.likes-a.dislikes;
                    let s2 = b.likes-b.dislikes;
                    if(s1<s2){
                        return 1
                    } else {
                        return -1
                    }
                }).map((r, i)=><Result key={r.id} result={r} like={()=>this.like(r, i)} dislike={()=>this.dislike(r, i)}/>)} */}
                {this.state.results.map((r, i)=><Result key={r.id} result={r} like={()=>this.like(r, i)} dislike={()=>this.dislike(r, i)}/>)}
            </div>
    )
    return (
        <div>
            <nav className="uk-navbar-container" uk-navbar="">
                <div className="uk-navbar-left">
                    <a className="uk-navbar-item uk-logo" href="/">Twitter Search</a>
                </div>
            </nav>
            
            {this.state.loading ? loading : content}
        </div>
    );
  }
}

export default Results;

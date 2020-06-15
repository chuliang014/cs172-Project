const Koa = require('koa');
const bodyParser = require('koa-bodyparser');
const Router = require('koa-router');
const cors = require('@koa/cors');
const axios = require('axios');

const router = new Router();

var app = new Koa();
app.use(bodyParser());
app.use(cors())

const config = {
    luceneServerAddr: '172.16.153.45:2019',
    hadoopServerAddr: '172.16.153.225:19010',
}


router.get('/api/search', async (ctx)=>{
    let {query, engine} = ctx.query
    if(engine==='hadoop'){
        await axios.get(`http://${config.hadoopServerAddr}/hadoop/search?query=${query}`).then((data)=>{            
            data.data.data = data.data.data.map((d, i)=>{
                d = JSON.parse(d)
                d.title = d.header
                d.url = d.url.replace("\"", "");
                return Object.assign({
                    id: i,
                    title: "",
                    content: "",
                    url: "",
                    imageUrls: [],
                    likes: 0,
                    dislikes: 0
                }, d)
            })
            ctx.body = data.data
            
        })
    } else {
        await axios.get(`http://${config.luceneServerAddr}/lucene/search?query="${query}"`).then((data)=>{
            data.data.data = data.data.data.map((d, i)=>{
                return Object.assign({
                    id: i,
                    title: "",
                    content: "",
                    url: "",
                    imageUrls: [],
                    likes: 0,
                    dislikes: 0
                }, d)
            })
            ctx.body = data.data
        })
    }
})

app
  .use(router.routes())
  .use(router.allowedMethods());

app.listen(3001)
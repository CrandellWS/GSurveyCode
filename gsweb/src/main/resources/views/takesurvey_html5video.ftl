<#-- @ftlvariable name="" type="views.SurveyView" -->
<!DOCTYPE html>
<meta charset="utf-8" />

<html xmlns="http://www.w3.org/1999/html">
<head>
    <title>Gamesurvey 2.0</title>
    <!-- Chang URLs to wherever Video.js files will be hosted -->
    <!-- Default URLs assume the examples folder is included alongside video.js -->
    <link href="../assets/lib/videojs/video-js.min.css" rel="stylesheet" type="text/css">
    <!-- Bootstrap 3.3.5 -->
    <link rel="stylesheet" href="${basePath}assets/lib/bootstrap/css/bootstrap.min.css">

    <!-- Include ES5 shim, sham and html5 shiv for ie8 support  -->
    <!-- Exclude this if you don't need ie8 support -->
    <script src="../assets/lib/videojs/ie8/videojs-ie8.min.js"></script>

    <!-- video.js must be in the <head> for older IEs to work. -->
    <script src="../assets/lib/videojs/video.min.js"></script>
    <script src="../assets/lib/dom/dom.js"></script>
    <script src="../assets/lib/showdown/dist/showdown.js"></script>
    <script src="../assets/lib/knockout-3.4.0.js"></script>
    <script src="../assets/lib/survey/survey.bootstrap.js"></script>

    <script src="../assets/lib/Youtube.js"></script>
    <script src="../assets/lib/videojs-overlay.js"></script>
    <link href="../assets/lib/videojs-overlay.css" rel="stylesheet">
    <link href="../assets/survey.css" rel="stylesheet">
</head>
<body>
<!-- Navigation -->
<nav class="navbar navbar-inverse navbar-fixed-top" role="navigation">
    <div class="container">
        <!-- Brand and toggle get grouped for better mobile display -->
        <div class="navbar-header">
            <button type="button" class="navbar-toggle" data-toggle="collapse"
                    data-target="#bs-example-navbar-collapse-1">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <img class="navbar-brand"  src="${basePath}assets/Gamesurvey-Logo-Transparent-1.png" style="padding:0px;"></img>

        </div>
        <!-- Collect the nav links, forms, and other content for toggling -->
        <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
            <ul class="nav navbar-nav">
                <li>
                    <a href="#">About</a>
                </li>
                <li>
                    <a href="#">Services</a>
                </li>
                <li>
                    <a href="#">Contact</a>
                </li>
            </ul>
        </div>
        <!-- /.navbar-collapse -->
    </div>
    <!-- /.container -->
</nav>
<!-- Page Content -->
<div class="container">

    <!-- Portfolio Item Row -->
    <div class="row">
        <div class="col-md-8">
            <p>
                <div id="videoContent" hidden style="width: 640px;height: 480px;padding: 0px;margin: 0px">
                <video
                    id="video"
                    class="video-js vjs-default-skin"
                    controls
                    width="640" height="480"
                    data-setup='{ "techOrder": ["html5","youtube"]}'>
                </video>
                </div>
                <div id="textContent" hidden>
                    <form  role="form">
                        <div id="surveyContainer"></div>
                    </form>
                </div>
            </p>
        </div>
        <div class="col-md-4" id="divDescription">
        </div>

    </div>
    <!-- /.row -->
    <hr>

    <!-- Footer -->
    <footer>
        <div class="row">
            <div class="col-lg-12">
                <p>Copyright &copy; GameSurvey 2016</p>
            </div>
        </div>
        <!-- /.row -->
    </footer>

</div>
<script>
    var survey = JSON.parse("${survey.jsonData?json_string}");
    var node;
    var response=new Response();
    var next=Dom.id("next");
    var btnSubmit=Dom.id('btnSubmit');
    var converter = new showdown.Converter();
    converter.setOption('tables', 'true');

    var divDescription=Dom.id('divDescription');
    divDescription.innerHTML=converter.makeHtml(survey.description);

    play(survey.startNode.id);

    function Response()
    {
        this.values=[];
    }

    Response.prototype.add = function(id, val){
        for(var i=0;i<this.values.length;i++)
        {
            if(this.values[i][0]==id)
                    this.values.splice(i,1);
        }
        this.values.push({id:id, value:val});
    };

    function play(id)
    {
        this.node=getNode(id);
        if(this.node.id==survey.endNode.id){
            fetch('/submitResponse/${getSurvey().id}', {
                method: 'post',
                headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
                },
                body: JSON.stringify(response)
            });
        }
        if(this.node.resultType=="videoTab") {
            playVideo(this.node.id);
        }
        else if(this.node.resultType=="textTab"){
            var questions=[];
            while(true) {
                var type="text";
                var name=node.name;
//                var title= this.converter.makeHtml(this.node.markDown);
                var title= this.node.markDown;
                var validators=[];
                if(node.validationType=="email")
                    validators.push({type:"email"});
                if(node.validationType=="number10")
                    validators.push({
                        type: "numeric",
                        maxValue: "10",
                        minValue: "1"
                    });
                if(node.validationType=="number")
                    validators.push({
                        type: "numeric"
                    });

                questions.push({type: type, name: name, title: title, validators: validators});

                var textContent = Dom.id('textContent');
                textContent.removeAttribute('hidden');
                var videoContent = Dom.id('videoContent');
                videoContent.setAttribute('hidden','');
                var tNext=getNode(this.node.actionText);


                //batch text nodes together
                if(tNext!=undefined&&tNext.id!=survey.endNode.id) {
                    if(tNext.resultType=="textTab") {
                        this.node=tNext;
                        continue;
                    }
                }
                break;
            }

//            //draw next button
//            if(this.node.id!=survey.endNode.id)
//                html+="<div class=\"form-group col-xs-6\">"+
//                    "<button id=\"next\" type=\"button\" " +
//                    "onclick='textClick(event)' "+
//                    "class=\"btn btn-default\">Next</button>"+
//                    "</div>";

            var surveyjs = new Survey.Survey({questions: questions},
                "surveyContainer");
        }
    }
    
    function responseChange(event, nodeID) {
        this.response.add(node.id, event.value);
    }

    function playVideo(id) {
        var textContent = Dom.id('textContent');
        textContent.setAttribute('hidden','');
        var videoContent = Dom.id('videoContent');
        videoContent.removeAttribute('hidden');
        var srcurl = '/stream/' + node.uuid;
        var myPlayer = videojs('video');
        videojs("video").ready(function(){
            myPlayer=this;
            myPlayer.src({type: "video/mp4", src: srcurl});
            myPlayer.play();
        });
    }

    function getNode(id) {
        if(id==survey.startNode.id)
                return survey.startNode;
        if(id==survey.endNode.id)
            return survey.endNode;
        for (var i = 0; i < survey.nodeList.length; i++) {
            var node = survey.nodeList[i];
            if (node.id == id) {
               return node;
            }
        }
    }
    //myPlayer.src({ type: 'video/youtube', src: 'https://www.youtube.com/watch?v=y6Sxv-sUYtM' });
    //myPlayer.src({ type: "video/youtube", src: "http://www.youtube.com/watch?v=Vm1TrA2AGyk" });

    videojs('video').overlay({
        content: '<div style="width: 100%;height: 100%;visibility: hidden;background-color: rgba(255,255,255,0)" id="v_overlay">' +
                    '' +
                '</div>',
        align: 'full',
    });

    var videoContent=Dom.id('videoContent');
    videoContent.addEventListener('click', function(event){videoClick(event);}, false);


    function textClick(event) {
        var val=node.actionText;
        this.response.add(node.id, val);
        console.log("play id:"+val);
        if(val)
            play(val);
    }

    function videoClick(event)
    {
        var x = event.offsetX;
        var y = event.offsetY;
        console.log(x+" "+y);
        var xField = Math.floor(x/videoContent.offsetWidth*32);
        var yField = Math.floor(y/videoContent.offsetHeight*32);
        var color_id=node.area[xField][yField];
        var id=node.action[color_id];
        var val=node.result[color_id];
        this.response.add(node.id, val);
        console.log("x:"+xField+", y:"+yField+", play id:"+id);
        if(id)
            play(id);
    }
</script>
</body>

</html>

<#-- @ftlvariable name="" type="views.SurveyView" -->
<!DOCTYPE html>
<meta charset="utf-8" />

<html xmlns="http://www.w3.org/1999/html">
<head id="head">
    <title>Gamesurvey 2.0</title>
    <link rel="stylesheet" href="../assets/survey.css">

    <!-- Bootstrap 3.3.5 -->
    <link rel="stylesheet" href="${basePath}assets/lib/bootstrap/css/bootstrap.min.css">

    <!-- Include ES5 shim, sham and html5 shiv for ie8 support  -->
    <!-- Exclude this if you don't need ie8 support -->

    <script src="../assets/lib/dom/dom.js"></script>
    <script src="../assets/lib/showdown/dist/showdown.js"></script>
    <script src="../assets/lib/knockout-3.4.0.js"></script>
    <script src="../assets/lib/survey/survey.bootstrap.js"></script>
    <script src="../assets/lib/jsmpg.js"></script>

</head>
<body>
<#if online==true>
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
                <div id="videoContent" hidden style="width: 640px; height: 480px; padding: 10px;margin: 10px">
                    <canvas id="videoCanvas" style="width:640px; height:480px; position: absolute; top: 0; right: 0; bottom: 0; left: 0;z-index: 10;">
                        <p>
                            Please use a browser that supports the Canvas Element, like
                            <a href="http://www.google.com/chrome">Chrome</a>,
                            <a href="http://www.mozilla.com/firefox/">Firefox</a>,
                            <a href="http://www.apple.com/safari/">Safari</a> or Internet Explorer 10
                        </p>
                    </canvas>
                    <div id="videoCanvasOverlay" style="width:640px; height:480px; position: absolute; top: 0; right: 0; bottom: 0; left: 0;z-index: 10; background-size: contain; background-repeat: no-repeat">
                    </div>
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
    response=new Response();
    var next=Dom.id("next");
    var btnSubmit=Dom.id('btnSubmit');
    var converter = new showdown.Converter();
    converter.setOption('tables', 'true');

    var divDescription=Dom.id('divDescription');
    divDescription.innerHTML=converter.makeHtml(survey.description);

    play(survey.startNode.id);
    var nextNode;

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
        node=getNode(id);
        if(node.id==survey.endNode.id){
            fetch('/submitResponse/${getSurvey().id}', {
                method: 'post',
                headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
                },
                body: JSON.stringify(response)
            });
        }
        if(node.resultType=="videoTab") {
            playVideo(node.id);
        }
        else if(node.resultType=="textTab"){
            var questions=[];
            while(true) {
                var type="text";
                var id=node.id.toString();
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

                questions.push({type: type, name: id, title: title, validators: validators});

                var textContent = Dom.id('textContent');
                textContent.removeAttribute('hidden');
                var videoContent = Dom.id('videoContent');
                videoContent.setAttribute('hidden','');
                var tNext=getNode(node.actionText);


                //batch text nodes together
                if(tNext!=undefined&&tNext.id!=survey.endNode.id) {
                    if(tNext.resultType=="textTab") {
                        node=tNext;
                        continue;
                    }
                }
                nextNode=tNext;
                break;
            }


            var surveyjs = new Survey.Survey({
                //cookieName: "gamesurveyID",
                questions: questions},
                "surveyContainer");
            surveyjs.completeText = "next";
            surveyjs.render();
            surveyjs.onComplete.add(function (sender) {
                textClick(sender);
            });
        }
    }
    
    function responseChange(event, nodeID) {
        response.add(node.id, event.value);
    }

    function playVideo(id) {
        var textContent = Dom.id('textContent');
        textContent.setAttribute('hidden','');
        var videoContent = Dom.id('videoContent');
        videoContent.removeAttribute('hidden');
        var srcurl = '/stream_mpeg1/' + node.uuid;
        var canvas = document.getElementById('videoCanvas');
        var myPlayer = new jsmpeg(srcurl, {
            canvas:canvas,
            onfinished:onFinished,
            autoplay:true,
            loop:false,
            progressive:true,
            onload:onLoad
        });
        loadImage('/snapshot_first/' + node.uuid)

        myPlayer.play();
    }

    function hideImage() {
        var overlay = document.getElementById('videoCanvasOverlay');
        overlay.style.visibility="hidden";
    }

    function loadImage(strDataURI) {
        var overlay = document.getElementById('videoCanvasOverlay');
        var img = new Image;
        if(strDataURI!=""){
            img.onload = function () {
                overlay.style.backgroundImage = "url('"+strDataURI+"')";
                overlay.style.visibility="visible";
            };
            img.src = strDataURI;
        }
        else{
            img.src = "";
        }
    }

    function onLoad() {
        hideImage();
        console.log("onLoad");
    }

    function onFinished() {
        loadImage('/snapshot_last/' + node.uuid)
        var videoContent=Dom.id('videoContent');
        videoContent.addEventListener('click', videoClick, false);
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

    function textClick(sender) {
        console.log(sender);
        var responseData=sender.data;


        Object.keys(responseData).forEach(function(key,index) {
            // key: the name of the object key
            // index: the ordinal position of the key within the object
            response.add(key,responseData[key]);
        });

//        this.response.add(node.id, val);
  //      console.log("play id:"+val);
        if(nextNode)
            play(nextNode.id);
    }

    function videoClick(event)
    {
        var videoContent = Dom.id('videoContent');
        var x = event.offsetX;
        var y = event.offsetY;
        console.log(x+" "+y);
        var xField = Math.floor(x/videoContent.offsetWidth*32);
        var yField = Math.floor(y/videoContent.offsetHeight*32);
        var color_id=node.area[xField][yField];
        var id=node.action[color_id];
        var val=node.result[color_id];
        response.add(node.id, val);
        console.log("x:"+xField+", y:"+yField+", play id:"+id);
        if(id){
            videoContent.removeEventListener('click', videoClick, false);
            play(id);
        }
    }
</script>
<#else >
    This survey is currently not available.
</#if>
</body>

</html>

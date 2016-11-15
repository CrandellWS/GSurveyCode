<#-- @ftlvariable name="" type="views.EditorView" -->
<#import "adminlayout.ftl" as layout>
<@layout.layout>
<link rel="stylesheet" href="${basePath}assets/editor/default.css">
<link rel="stylesheet" href="${basePath}assets/editor/demo.css">
<link rel="stylesheet" href="${basePath}assets/editor/app.css">
<link rel="stylesheet" href="${basePath}assets/editor/gollum.css">
<link rel="stylesheet" href="${basePath}assets/lib/goo.js/style/style.css">
<link rel="stylesheet" href="${basePath}assets/adminlte/plugins/daterangepicker/daterangepicker.css">



<script src="${basePath}assets/lib/goo.js/src/goo.js"></script>
<script src="${basePath}assets/lib/jsPlumb/jsPlumb-2.0.5.js"></script>
<script src="${basePath}assets/editor/js/survey.js"></script>
<script src="${basePath}assets/editor/js/editor.js"></script>
<script src="${basePath}assets/adminlte/plugins/daterangepicker/moment.min.js"></script>
<script src="${basePath}assets/adminlte/plugins/daterangepicker/daterangepicker.js"></script>

<script>
    survey = JSON.parse("${survey.jsonData?json_string}");
    console.log(Object.keys(survey));
    if (Object.keys(survey).length == 0)
        survey = new Survey();
    console.log(survey);
</script>
<style>
    .datepicker{z-index:1151 !important;}
</style>
<div class="row">
    <div class="col-xs-12 nopadding">
        <div class="box">
            <div class="box-header">
                <h3 class="box-title" id="surveyName" ></h3>
                <a href="${basePath}preview/${survey.id}" target="_blank" style="display: inline">(preview)</a>
                <div id="saving" style="display: inline;visibility: hidden">&nbsp;saving..</div>
                <div class="box-tools">
                    <div class="input-group input-group-sm" style="width: 150px;">
                        <div class="input-group-btn">
                            <button type="button" class="btn btn-default" onclick="settings();">Settings</button>
                            <button type="button" class="btn btn-default" onclick="add();">add</button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="jtk-demo-canvas canvas-wide flowchart-demo jtk-surface jtk-surface-nopan" id="editorCanvas">
            </div>
            <!-- Survey Settings -->
            <template id="templateSettings">
                <form role="form">
                    <div class="form-group">
                        <div class="form-group">
                            <label for="pwd">Survey Name:</label>
                            <input type="text" class="form-control" id="textName">
                        </div>
                        <div class="form-group">
                            <label for="pwd">Contact:</label>
                            <input type="text" class="form-control" id="textContact">
                        </div>
                        <label for="comment">Description:</label>
                        <textarea class="form-control" rows="5" id="textDescription"></textarea>

                        <div class="form-group">
                            <label>Status</label>
                            <select id="status" class="form-control select2 select2-hidden-accessible" style="width: 100%;" tabindex="-1" aria-hidden="true">
                                <option value="offline">Offline</option>
                                <option value="online">Online</option>
                                <option value="planned">Planned</option>
                            </select>
                        </div>

                        <div class="form-group" id="datepicker">
                            <label>Date and time range:</label>
                                <div class="input-group">
                                    <div class="input-group-addon">
                                        <i class="fa fa-clock-o"></i>
                                    </div>
                                    <input type="text" class="form-control pull-right" id="reservationtime">
                                </div>
                            <!-- /.input group -->
                            </div>
                    </div>

                </form>
            </template>

            <template id="templateNodeDialogHeader">
                <div class="row">
                <div class="col-lg-7 nopadding" style="height: 47px; border-bottom: 1px solid #dddddd;">
                    <input type="text" class="form-control" id="modal_title" style="font-size: 18px; width: auto; height: auto;"
                           placeholder="Name">
                </div>
                <div class="col-lg-4 nopadding">
                    <ul class="nav nav-tabs" id="nodeTabs">
                        <li><a data-toggle="tab" href="#videoTab">Video</a></li>
                        <li><a data-toggle="tab" href="#textTab">Text</a></li>
                    </ul>
                </div>
                </div>
            </template>

            <template id="templateNodeDialogBody">
                <div class="tab-content">
                    <div id="videoTab" class="tab-pane fade in">
                        <select class="form-control input-medium" id="selectVideo">
                            <#list videos as video>
                                <option value="${video.UUID}">${video.name}</option>
                            </#list>
                        </select>
                        <div id="gooCanvas" style="width: 600px; height: 400px; cursor: crosshair;">
                        </div>
                        <div class="row">
                            <div class="col-md-12">
                                <form class="form-inline">
                                    <#list colors as color>
                                    <div class="form-group">
                                        <input type="radio" class="" name="radioColor"
                                               value="${color?index}">

                                        <#if color?index == 0>
                                                <div class="form-control"
                                                style="background-image: url('/assets/eraser_43.png');height:34px;width:34px;"></div>
                                        <div style="width:450px"></div>

                                        <#else >

                                            <div class="form-control"
                                                 style="background-color: ${color}">${color?index}</div>
                                            <input type="text" class="form-control"
                                                   id="result_value_${color?index}" placeholder="Value">
                                            <select class="form-control" id="result_action_${color?index}">
                                                <option value="0">(Action)</option>
                                            </select>
                                        </#if>
                                    </div>
                                    </#list>
                                </form>
                            </div>
                        </div>
                    </div>
                    <div id="textTab" class="tab-pane fade">
                        <div class="form-group">
                            <label for="comment"><a href="https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet">Markdown</a> <a href="http://showdownjs.github.io/demo/">(showdown)</a>:</label>
                            <textarea class="form-control" rows="4" id="textMarkDown"></textarea>
                            <div class="btn-group">
                                Validate as:
                                <select class="form-control" id="selectValidation">
                                    <option value="text">Text</option>
                                    <option value="number">Number</option>
                                    <option value="number10">Number [0-10]</option>
                                    <option value="email">email</option>
                                    <option value="url">URL</option>
                                    <option value="regex">Regex</option>
                                </select>
                                Next:<br/>
                                <select class="form-control" id="result_action_text">
                                    <option value="0">(Action)</option>
                                </select>
                            </div>
                        </div>
                    </div>
                </div>
        </template>
</div>
<script>
    $("#surveyName").html(survey.name);
    var raster = 32;
    function setPixel(g, node, value) {
        console.log(g.mouseY)
        var xField = Math.floor(g.mouseX / g.width * 32);
        var yField = Math.floor(g.mouseY / g.height * 32);
        if(xField<32&&yField<32)
            node.area[xField][yField] = value;
    }
    function drawRaster(node) {
        var g = new Goo({
            width: 500,
            height: 375,
            node: node,
            selColor: 0,
            onMouseDrag: function (g) {
                setPixel(g, this.node, this.selColor);
                console.log(this.selColor);
                //addSign(g.mouseX, g.mouseY, "g.onMouseDrag()", "black", "white");
            },
            onMouseDown: function (g) {
                var selected= Dom.find('[name=radioColor]:checked')[0];
                if(selected==undefined)
                    return;
                this.selColor =selected.value;
                setPixel(g, this.node, this.selColor);
                this.onDraw(g)
            },
            onMouseUp: function (g) {
                //addSign(g.mouseX, g.mouseY, "g.onMouseUp()", "#E30B5D", "white", 1.5, 10, true);
            },
            onKeyDown: function (g) {
                //addBall(g.keyCode);
            },
            onMouseMove: function (g) {
                //addSign(g.mouseX, g.mouseY, "g.onMouseMove()", "white", "black");
            },
            onDraw: function (g, t) {
                g.ctx.clearRect(0, 0, g.width, g.height);
                var ctx = g.ctx;
                {
                    ctx.beginPath();
                    for (var i = 1; i < raster; i++) {
                        ctx.moveTo(g.width / raster * i, 0);
                        ctx.lineTo(g.width / raster * i, g.height);
                    }
                    for (var i = 1; i < raster; i++) {
                        ctx.moveTo(0, g.height / raster * i);
                        ctx.lineTo(g.width, g.height / raster * i);
                    }

                    ctx.strokeStyle = '#444455';
                    ctx.lineWidth=.3;
                    ctx.stroke();
                    ctx.closePath();
                    for (var i = 0; i < raster; i++) {
                        for (var j = 0; j < raster; j++) {
                            var value = this.node.area[i][j];
                            if (value > 0) {
                                var rWidth = g.width / raster;
                                var rHeight = g.height / raster;
                                var xCoor = rWidth * i;
                                var yCoor = rHeight * j;
                                ctx.fillStyle = kelly_colors_hex[value];
                                ctx.fillRect(xCoor, yCoor, rWidth - 1, rHeight - 1);
                            }
                        }
                    }
                }
            }
        });
        Dom.id('gooCanvas').appendChild(g.canvas);
    }

    function addOptions(action, _survey) {
        while (action.options.length > 0)
            action.remove(0);

        var option = document.createElement('option');
        option.label = '-'
        option.value = '';
        action.add(option);
        for (var j = 0; j < _survey.nodeList.length; j++) {
            var opt = document.createElement("option");
            opt.value = _survey.nodeList[j].id;
            opt.label = _survey.nodeList[j].name;
            action.add(opt);
        }
        var startOption = document.createElement('option');
        startOption.label = 'Start'
        startOption.value = 1;
        action.add(startOption);
        var endOption = document.createElement('option');
        endOption.label = 'End'
        endOption.value = 2;
        action.add(endOption);
    }

    kelly_colors_hex = [
        <#list colors as color>
            '${color}',
        </#list>]

    function saveNodeDialog() {
        console.log('saveNodeDialog ' + node.id);
        node.name = $('#modal_title').val();
        var value=$("#selectValidation").val();
        node.validationType=value;
        node.markDown=$("#textMarkDown").val();
        node.actionText=$("#result_action_text").val();

        var connections = instance.getConnections({source: 'vnode_' + node.id});
        for (var i = 0; i < connections.length; i++)
            instance.detach(connections[i]);
        var sv = Dom.id('selectVideo');
        if (sv.selectedIndex >= 0)
            node.uuid = sv.options[sv.selectedIndex].value;
        if(node.resultType=='textTab') {
            instance.connect({
                uuids: ['vnode_' + node.id + '_OUT', 'vnode_' + node.actionText + '_IN'],
                editable: false
            });
        }
        else {
            for (var i = 1; i < kelly_colors_hex.length; i++) { //skip zero/eraser
                var action = Dom.id('result_action_' + i);
                var strAction = action.options[action.selectedIndex].value;
                var text = Dom.id('result_value_' + i).value;
                node.action[i] = strAction;
                node.result[i] = text;
                if(strAction.trim().length==0)
                    continue;
                console.log('ep: ' + node.id + '_OUT' + ',   ep: ' + strAction + '_IN');
                instance.connect({
                    uuids: ['vnode_' + node.id + '_OUT', 'vnode_' + strAction + '_IN'],
                    editable: false
                });
            }
            }
        $('#vnode_'+node.id+' > div > div').html(node.name); //set name in title bar
        if(node.resultType=='videoTab')
            $('#vnode_'+node.id+' > button').css('background-image', 'url(/snapshot_first/' + node.uuid + ')'); //set background image
        else
            $('#vnode_'+node.id+' > button').css('background-image', 'url(/assets/editor/text.png)'); //set background image

    }

            function save() {
                var saving=$('#saving');
                saving.css('visibility','visible');
                survey.dateModified=(new Date).getTime();
                var startDomNode = $('#vnode_' + survey.startNode.id);
                survey.startNode.xpos = startDomNode.position().left;
                survey.startNode.ypos = startDomNode.position().top;

                var endDomNode = $('#vnode_' + survey.endNode.id);
                survey.endNode.xpos = endDomNode.position().left;
                survey.endNode.ypos = endDomNode.position().top;

                for (var i = 0; i < survey.nodeList.length; i++) {
                    var node = survey.nodeList[i];
                    var domNode = $('#vnode_' + node.id);
                    node.xpos = domNode.position().left;
                    node.ypos = domNode.position().top;
                }
                fetch('${basePath}saveSurvey/' +${survey.id}, {
                    method: 'put',
                    credentials: 'same-origin',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(survey),
                })
                        .then(function (response) {
                            console.log(response);
                            if(response.ok==true)
                                saving.css('visibility','hidden');

                        })
            }


            function add() {
                var n = new VNode(survey.seq++);
                n.name = 'Node (' + n.id + ')';
                n.xpos = 200;
                n.ypos = 200;
                survey.nodeList.push(n);
                addNode(n, function (n) {
                    editDialog(n, survey)
                }, n.xpos, n.ypos);
            }
            function  settings() {
                var t = document.querySelector('#templateSettings');



                var clone = document.importNode(t.content, true);
                var box=bootbox.dialog({
                    message: clone,
                    title: 'Survey settings',
                    buttons: {
                        success: {
                            label: "OK",
                            className: "btn-success",
                            callback: function() {
                                var name=$('#textName').val();
                                survey.name=name;

                                var desc=$('#textDescription').val();
                                survey.description=desc;

                                var contact=$('#textContact').val();
                                survey.contact=contact;

                                var status=$('#status').val();
                                survey.status=status;

                                var startDate=$('#reservationtime').data('daterangepicker').startDate.utc().unix();
                                survey.startDate=startDate;

                                var endDate=$('#reservationtime').data('daterangepicker').endDate.utc().unix();
                                survey.endDate=endDate;

                                $("#surveyName").html(survey.name);
                                save();
                            }
                        },
                    }
                });
                box.on("shown.bs.modal", function() {
                    var startDate=survey.startDate;
                    if(startDate==null||startDate<10)
                        startDate=moment().unix();
                    var endDate=survey.startDate;
                    if(endDate==null||endDate<10)
                        endDate=moment().unix();
                    $('#reservationtime').daterangepicker({timePicker: true,
                        timePicker24Hour: true,
                        timePickerIncrement: 30,
                        locale: {format: 'YYYY-MM-DD HH:mm'},
                        startDate: moment.unix(startDate),
                        endDate: moment.unix(endDate),
                    });
                    $('#status').change(function () {
                        if($('#status').val()=='planned')
                            $('#datepicker').show();
                        else
                            $('#datepicker').hide();
                    })

                    $('#status').val(survey.status);
                    $('#status').change();
                    $('#textDescription').val(survey.description);
                    $('#textName').val(survey.name);
                    $('#textContact').val(survey.contact);

                });
                box.on("hidden.bs.modal", function() {
                });
            }

            function editDialog(_node, _survey) {
                var tBody = document.querySelector('#templateNodeDialogBody');
                var cloneBody = document.importNode(tBody.content, true);
                var tHeader = document.querySelector('#templateNodeDialogHeader');
                var cloneHeader = document.importNode(tHeader.content, true);
                var dialog=bootbox.dialog({
                    message: cloneBody,
                    title: cloneHeader,
                    buttons: {
                        success: {
                            label: "OK",
                            className: "btn-success",

                        },
                    },
                    show: false
                });

                dialog.on('hide.bs.modal', function (e) {
                    saveNodeDialog();
                });

                dialog.on('show.bs.modal', function(e){
                    node=_node;
                    console.log('beforeOpen ' + node.id);
                    if(node.resultType==undefined)
                        node.resultType="videoTab";
                    if(node.resultType=="videoTab")
                        $('#nodeTabs a[href="#videoTab"]').tab('show');
                    else
                        $('#nodeTabs a[href="#textTab"]').tab('show');
                    Dom.id('gooCanvas').innerHTML = '';
                    drawRaster(node);
                    Dom.id('modal_title').value = node.name;
                    for (var i = 1; i < kelly_colors_hex.length; i++) {
                        var action = Dom.id('result_action_' + i);
                        addOptions(action, _survey);
                        for (var j = 0; j < action.options.length; j++) {
                            if (_node.action[i] == action.options[j].value)
                                action.selectedIndex = j;
                        }
                        Dom.id('result_value_' + i).value = node.result[i] || '';
                    }
                    var actionText = Dom.id('result_action_text');
                    addOptions(actionText, _survey);
                    for (var j = 0; j < actionText.options.length; j++) {
                        if (_node.actionText == actionText.options[j].value)
                            actionText.selectedIndex = j;
                    }

                    $("#selectValidation").val(node.validationType);
                    $("#textMarkDown").val(node.markDown);

                    var sv = Dom.id('selectVideo');
                    sv.value = node.uuid;
                    var canvas = Dom.id('gooCanvas').firstElementChild;
                    var uuid = "";
                    if (sv.selectedIndex >= 0)
                        uuid = sv.options[sv.selectedIndex].value;

                    canvas.style.background = 'url(/snapshot_last/' + uuid + ')';
                    canvas.style.backgroundSize = 'contain';
                    canvas.style.objectFit = 'fill';
                    canvas.style.backgroundRepeat = 'no-repeat';

                    sv.addEventListener("change", function () {
                        var uuid = sv.options[sv.selectedIndex].value;
                        canvas.style.background = 'url(/snapshot_last/' + uuid + ')';
                        canvas.style.backgroundSize = 'contain';
                        canvas.style.objectFit = 'fill';
                        canvas.style.backgroundRepeat = 'no-repeat';
                    });

                    $( 'a[data-toggle="tab"]' ).on( 'shown.bs.tab', function( evt ) {
                        // Read the a href of the anchor that was clicked
                        var anchor = $( evt.target ).attr( 'href' );
                        // Trim the leading '#' from the href attribute
                        anchor = anchor.substr( 1, anchor.length );
                        // We'll see this in the next gist
                        node.resultType=anchor;
                    });
                });
                dialog.on('hidden.bs.modal', function(e){
                    save();
                });
                dialog.modal('show');
            }
        </script>
</@layout.layout>
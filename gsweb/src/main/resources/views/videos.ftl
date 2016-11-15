<#-- @ftlvariable name="" type="views.EditorView" -->
<#import "adminlayout.ftl" as layout>
<@layout.layout>
<link rel="stylesheet" href="/assets/app.css">

    <div class="row">
        <div class="col-xs-12">
            <div class="box">
                <div class="box-header">
                    <h3 class="box-title">Videos</h3>
                    <div class="box-tools">
                        <#--<div class="input-group input-group-sm" style="width: 150px;">-->
                            <#--<input type="text" name="table_search" class="form-control pull-right" placeholder="Search">-->

                            <#--<div class="input-group-btn">-->
                                <#--<button type="submit" class="btn btn-default"><i class="fa fa-search"></i></button>-->
                            <#--</div>-->
                        <#--</div>-->
                    </div>
                </div>
                <!-- /.box-header -->
                <div class="box-body table-responsive no-padding">
                    <table id="tableVideo" class="table table-hover">
                        <tbody>
                            <tr>
                                <th style="display:none;">UUID</th>
                                <th>User</th>
                                <th>Date</th>
                                <th>Status</th>
                                <th>Progress</th>
                                <th></th>
                            </tr>
                            <template id="videoRow">
                                <tr>
                                    <td style="display:none;"></td>
                                    <td></td>
                                    <td></td>
                                    <td><span class="label label-success">Approved</span></td>
                                    <td>
                                        <div class="progress progress-xs progress-striped active">
                                            <div class="progress-bar progress-bar-primary" style="width: 0%"></div>
                                        </div>
                                    </td>
                                    <td>
                                        <div>
                                            <button id="btnDelete" class="btn-default btn" onclick="deleteVideo(this);">
                                            <i class="fa fa-trash"></i>
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            </template>
                        </tbody>
                    </table>
                </div>
                <!-- /.box-body -->
            </div>
            <!-- /.box -->
            <div>
                <label for="fileselect">Files to upload:</label>
                <input type="file" id="fileselect" name="fileselect[]" multiple="multiple" />
                <div id="filedrag">or drop files here</div>
            </div>
        </div>
    </div>

<script>
    function onDropFile(ev) {
        ev.preventDefault();
        var data = ev.dataTransfer.getData("text");
        ev.target.appendChild(document.getElementById(data));
    }

    function InitDrop() {
        var fileselect = document.getElementById("fileselect");
        var filedrag = document.getElementById("filedrag");

        // file select
        fileselect.addEventListener("change", FileSelectHandler, false);
        filedrag.addEventListener("dragover", FileDragHover, false);
        filedrag.addEventListener("dragleave", FileDragHover, false);
        filedrag.addEventListener("drop", FileSelectHandler, false);
        filedrag.style.display = "block";

    }
    // file drag hover
    function FileDragHover(e) {
        e.stopPropagation();
        e.preventDefault();
        e.target.className = (e.type == "dragover" ? "hover" : "");
    }
    function ParseFile(file) {
        fetch('/getServerURL/'+file.name,{
            credentials: 'same-origin'

        })
        .then(function(response) {
            return response.text();
        }).then(function(url){
            var data = new FormData();
            data.append('file', file);
            data.append('entryID', -1);
            fetch(url, {
                method: 'post',
                mode: 'no-cors',
                body: data
            }).then(function (data) {
                //this.loadFileNames(-1);
                console.log('request succeeded with JSON response', data)
            }).catch(function (error) {
                //this.loadFileNames(-1);
                console.log('request failed', error)
            });
        });
    }
    // file selection
    function FileSelectHandler(e) {
        // cancel event and hover styling
        FileDragHover(e);
        // fetch FileList object
        var files = e.target.files || e.dataTransfer.files;
        // process all File objects
        for (var i = 0, f; f = files[i]; i++) {
            ParseFile(f);
        }
    }
    function updateVideos()
    {
        fetch('/videoListJSON', {
            method: 'get',
            credentials: 'same-origin'
        }).then(function(response) {
            return response.json()
        }).then(function(json) {
            updateVideoTable(json);
            //console.log('parsed json', json)
        }).catch(function(ex) {
            console.log('parsing failed', ex)
        });
    }

    function updateVideoTable(data) {
        var uuids=[];
        var table=document.querySelector('#tableVideo');
        for (var i = 0, row; row = table.rows[i]; i++){
            var uuid=row.cells[0].textContent;
            uuids.push(uuid);
        }
        var template = document.querySelector('#videoRow');
        for (var i = 0; i < data.length; i += 1) {
            var cat = data[i];
            if(uuids.indexOf(cat.uuid)!=-1)
            {
                var cells=table.rows[uuids.indexOf(cat.uuid)].cells;
                cells[3].textContent = cat.videoStatus;
                if(cat.progressPercent==100)
                    cells[4].firstElementChild.style.visibility='hidden'
                cells[4].firstElementChild.firstElementChild.style.width = cat.progressPercent + '%';
                continue;
            }
            var clone = template.content.cloneNode(true);
            var cells = clone.querySelectorAll('td');
            cells[0].textContent = cat.uuid;
            cells[1].textContent = cat.name;
            cells[2].textContent = cat.id;
            cells[3].textContent = cat.videoStatus;
            if(cat.progressPercent==100)
                cells[4].firstElementChild.style.visibility='hidden'
            cells[4].firstElementChild.firstElementChild.style.width = cat.progressPercent + '%';
            template.parentNode.appendChild(clone);
        }
    }

    function deleteVideo(data)
    {
        var trow=data.parentNode.parentNode.parentNode;
        var uuid=trow.firstElementChild.textContent;
        var name=trow.children[1].textContent;
        bootbox.confirm("Delete video \""+name+"\"?", function(result) {
            if(result==true){
                fetch('/deleteVideo/'+uuid, {
                    credentials: 'same-origin',
                    method: 'put',
                }).catch(function(ex) {
                    console.log('failed', ex)
                }).then(function (data) {
                    trow.parentNode.removeChild(trow);
                });
            }
        });
    }

    window.onload=function()
    {
        this.InitDrop();
        updateVideos();
        setInterval(function(){ updateVideos(); }, 1000);
    }



</script>

</@layout.layout>
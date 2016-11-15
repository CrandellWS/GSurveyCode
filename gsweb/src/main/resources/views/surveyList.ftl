<#-- @ftlvariable name="" type="views.SurveyListView" -->
<#import "adminlayout.ftl" as layout>
<@layout.layout>
<script src="${basePath}assets/editor/js/survey.js"></script>
    <div class="row">
        <div class="col-xs-12">
            <div class="box">
                <div class="box-header">
                    <h3 class="box-title">Surveys</h3>

                    <div class="box-tools">
                        <div class="input-group input-group-sm" style="width: 150px;">
                            <input type="text" id="create_survey"  name="create_survey" class="form-control pull-right" placeholder="New Survey">
                                <script>
                                    function parseJSON(response) {
                                        return response.json()
                                    }
                                    function createSurvey(ev)
                                    {
                                        var s=new Survey();
                                        s.name=document.getElementById('create_survey').value;
                                        fetch('${basePath}createEmptySurvey', {
                                            method: 'POST',
                                            credentials: 'same-origin',
                                            headers: {
                                                'Accept': 'application/json',
                                                'Content-Type': 'application/json'
                                            },
                                            body: JSON.stringify(s),
                                        })
                                        .then(checkStatus)
                                                .then(parseJSON)
                                                .then(function(data)
                                        {
                                            console.log(data);
                                            location.replace("${basePath}edit/"+data);
                                        });
                                    }

                                    function checkStatus(response) {
                                        if (response.status >= 200 && response.status < 300) {
                                            return response
                                        } else {
                                            var error = new Error(response.statusText)
                                            error.response = response
                                            throw error
                                        }
                                    }

                                    function deleteSurvey(data)
                                    {
                                        var trow=data.parentNode.parentNode;
                                        var id=trow.firstElementChild.textContent;
                                        var name=trow.children[1].textContent;
                                        bootbox.confirm("Delete survey \""+name+"\"?", function(result) {
                                            if(result==true){
                                                fetch('/survey/'+id, {
                                                    credentials: 'same-origin',
                                                    method: 'delete',
                                                }).catch(function(ex) {
                                                    console.log('failed', ex)
                                                }).then(function (data) {
                                                    trow.parentNode.removeChild(trow);
                                                });
                                            }
                                        });
                                    }
                                </script>
                            <div class="input-group-btn">
                                <button onclick="createSurvey(event);" class="btn btn-default"><i class="fa fa-plus"></i></button>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- /.box-header -->
                <div class="box-body table-responsive no-padding">
                    <table class="table table-hover">
                        <tbody><tr>
                            <th style="display:none;">UUID</th>
                            <th>Name</th>
                            <th>Created</th>
                            <th>Modified</th>
                            <th>Status</th>
                            <th>Public URL</th>
                        </tr>
                        <#list surveys as s>
                            <tr>
                                <td style="display:none;">${s.id}</td>
                                <td><a href="${basePath}edit/${s.id}">${s.name}&nbsp;</a></td>
                                <td>${s.dateCreated?number_to_date}</td>
                                <td>${s.dateModified?number_to_date}</td>
                                <td><span class="label label-success">${s.surveyData.status}</span></td>
                                <td><a href="${basePath}survey/${s.id}" target="_blank">${basePath}survey/${s.id}</a></td>
                                <td>
                                    <button id="btnDelete" class="btn-default btn" onclick="deleteSurvey(this);">
                                        <i class="fa fa-trash"></i>
                                    </button>
                                </td>
                            </tr>
                        </#list>
                        </tbody>
                    </table>
                </div>
                <!-- /.box-body -->
            </div>
            <!-- /.box -->
        </div>
    </div>

</@layout.layout>
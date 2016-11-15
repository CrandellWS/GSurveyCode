<#-- @ftlvariable name="" type="views.MediaList" -->
<#import "adminlayout.ftl" as layout>
<@layout.layout>
<div class="row">
    <div class="col-xs-12">
        <div class="box">
            <div class="box-header">
                <h3 class="box-title">Media</h3>

                <div class="box-tools">
                    <div class="input-group input-group-sm" style="width: 150px;">
                        <input type="text" id="create_survey"  name="create_survey" class="form-control pull-right" placeholder="New Survey">
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
                        <th>ID</th>
                        <th>Name</th>
                        <th>Date</th>
                        <th>Status</th>
                        <th>Reason</th>
                    </tr>
                        <#list surveys as s>
                        <tr>
                            <td><a href="${basePath}edit/${s.id}">${s.id}</a></td>
                            <td>${s.name}</td>
                            <td>11-7-2014</td>
                            <td><span class="label label-success">OK</span></td>
                            <td>Bacon ipsum dolor sit amet salami venison chicken flank fatback döner.</td>
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
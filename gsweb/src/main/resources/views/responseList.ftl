<#-- @ftlvariable name="" type="views.ResponseListView" -->
<#import "adminlayout.ftl" as layout>
<@layout.layout>
    <div class="row">
        <div class="col-xs-12">
            <div class="box">
                <div class="box-header">
                    <h3 class="box-title">Results</h3>
                </div>
                <!-- /.box-header -->
                <div class="box-body table-responsive no-padding">
                    <table class="table table-hover">
                        <tbody><tr>
                            <th>Survey Name</th>
                            <th>#results</th>
                            <th>Survey status</th>
                        </tr>
                        <#list surveys as s>
                            <tr>
                                <td><a href="${basePath}getXLS/${s.id}">${s.name}&nbsp;</a></td>
                                <td>${s.responsecount}</td>
                                <td><span class="label label-success">LIVE</span></td>
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
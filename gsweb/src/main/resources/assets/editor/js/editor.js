var instance;

jsPlumb.ready(function () {
    instance = window.jsp = jsPlumb.getInstance({
        // default drag options
        DragOptions: {cursor: 'pointer', zIndex: 2000},
        // the overlays to decorate each connection with.  note that the label overlay uses a function to generate the label text; in this
        // case it returns the 'labelText' member that we set on each connection in the 'init' method below.
        ConnectionOverlays: [
            ["Arrow", {location: 1}],
            // [ "Label", {
            //     location: 0.2,
            //     id: "label",
            //     cssClass: "aLabel"
            // }]
        ],
        Container: "editorCanvas"
    });

    var basicType = {
        connector: ["Bezier", {curviness: 63}],
        //connector: "StateMachine",
        paintStyle: {strokeStyle: "red", lineWidth: 4},
        hoverPaintStyle: {strokeStyle: "blue"},
        overlays: [
            "Arrow"
        ]
    };
    instance.registerConnectionType("basic", basicType);

    // this is the paint style for the connecting lines..
    var connectorPaintStyle = {
            lineWidth: 4,
            strokeStyle: "#61B7CF",
            joinstyle: "round",
            outlineColor: "white",
            outlineWidth: 2
        },
        // .. and this is the hover style.
        connectorHoverStyle = {
            lineWidth: 4,
            strokeStyle: "#216477",
            outlineWidth: 2,
            outlineColor: "white"
        },
        endpointHoverStyle = {
            fillStyle: "#216477",
            strokeStyle: "#216477"
        },
        // the definition of source endpoints (the small blue ones)
        //https://jsplumbtoolkit.com/community/doc/endpoints.html
        sourceEndpoint = {
            endpoint: "Rectangle",
            paintStyle: {
                strokeStyle: "#0000C0",
                fillStyle: "transparent",
                width: 10,
                height: 8,
                lineWidth: 3
            },
            isSource: true,
            connector: ["Bezier", {curviness: 63}],
            connectorStyle: connectorPaintStyle,
            hoverPaintStyle: endpointHoverStyle,
            connectorHoverStyle: connectorHoverStyle,
            maxConnections: -1,
            dragOptions: {},
            overlays: [
//                [ "Label", {location: [0.5, 1.5], label: "", cssClass: "endpointSourceLabel"} ]
            ]
        },
        // the definition of target endpoints (will appear when the user drags a connection)
        targetEndpoint = {
            endpoint: "Rectangle",
            paintStyle: {fillStyle: "#0000C0", width: 10, height: 8},
            hoverPaintStyle: endpointHoverStyle,
            maxConnections: -1,
            dropOptions: {hoverClass: "hover", activeClass: "active"},
            isTarget: true,
            overlays: [
                //              [ "Label", { location: [0.5, -0.5], label: "", cssClass: "endpointTargetLabel" } ]
            ]
        },
        init = function (connection) {
            connection.getOverlay("label").setLabel(connection.sourceId.substring(15) + "-" + connection.targetId.substring(15));
        };

    var _addEndpoints = function (toId, sourceAnchors, targetAnchors) {
        for (var i = 0; i < sourceAnchors.length; i++) {
            var sourceUUID = toId + sourceAnchors[i];
            instance.addEndpoint("flowchart" + toId, sourceEndpoint, {
                anchor: sourceAnchors[i], uuid: sourceUUID
            });
        }
        for (var j = 0; j < targetAnchors.length; j++) {
            var targetUUID = toId + targetAnchors[j];
            instance.addEndpoint("flowchart" + toId, targetEndpoint, {anchor: targetAnchors[j], uuid: targetUUID});
        }
    };


    addNode = function (vnode, onclick, xpos, ypos) {
        var canvas = document.getElementById("editorCanvas");
        var div = document.createElement("div");
        div.appendChild(Dom.create('<div class="nodeHeader">' +
            '<div class="nodeHeaderText">' + vnode.name + '</div>' +
            '<button class="closeBtn btn btn-default">x</button>' +
            '</div>' +
            '<button  class="editBtn btn btn-default" style="background-image:url(/assets/editor/text.png);  background-color: Transparent; background-size: contain; width: 214px;height:160px;background-position-x: center;background-position-y: center;background-repeat: no-repeat"/></button>'
        ));
        var editBtn = div.getElementsByClassName('editBtn')[0];
        editBtn.addEventListener('click', function (ev) {
            ev.preventDefault();
            onclick(vnode)
        });
        var closeBtn = div.getElementsByClassName('closeBtn')[0];
        closeBtn.addEventListener('click', function (ev) {
            ev.preventDefault();
            bootbox.confirm("Are you sure?", function (result) {
                if (result == true) {
                    console.log('DELETE ' + vnode.id);
                    for (var i = 0; i < survey.nodeList.length; i++) {
                        var node = survey.nodeList[i];
                        if (node.id == vnode.id) {
                            survey.nodeList.splice(i, 1);
                            break;
                        }
                    }
                    instance.remove('vnode_' + vnode.id);
                    save();
                }
                else
                    console.log('Cancel');
            });
        });

        div.setAttribute('id', 'vnode_' + vnode.id);
        div.setAttribute('class', 'window vnode');
        div.style.position = "absolute";
        div.style.left = xpos + 'px';
        div.style.top = ypos + 'px';
        if (vnode.resultType == 'videoTab')
            div.getElementsByTagName('button')[1].style.backgroundImage = 'url(/snapshot_first/' + vnode.uuid + ')';

        canvas.appendChild(div);
        instance.draggable(div,{
            grid: [20, 20],
            stop: function () {
                save();
            }
        });

        instance.addEndpoint(div.id, targetEndpoint, {anchor: 'TopLeft', uuid: div.id + '_IN'});
        console.log('ep: ' + div.id + '_IN' + '   ,ep:' + div.id + '_OUT');
        instance.addEndpoint(div.id, sourceEndpoint, {anchor: 'BottomRight', uuid: div.id + '_OUT'});
    }

    // suspend drawing and initialise.
    instance.batch(function () {

        // listen for new connections; initialise them the same way we initialise the connections at startup.
        instance.bind("connection", function (connInfo, originalEvent) {
            init(connInfo.connection);
        });

        addNode(survey.startNode, function (n) {
            editDialog(n, survey)
        }, survey.startNode.xpos, survey.startNode.ypos);
        addNode(survey.endNode, function (n) {
            editDialog(n, survey)
        }, survey.endNode.xpos, survey.endNode.ypos);

        for (var i = 0; i < survey.nodeList.length; i++) {
            var n = survey.nodeList[i];
            addNode(n, function (n) {
                editDialog(n, survey)
            }, n.xpos, n.ypos);
        }
        for (var i = 0; i < survey.nodeList.length; i++) {
            node = survey.nodeList[i];
            connect(node);
        }
        connect(survey.startNode);
        connect(survey.endNode);

        function connect(node) {
            if (node.resultType == 'textTab') {
                instance.connect({
                    uuids: ['vnode_' + node.id + '_OUT', 'vnode_' + node.actionText + '_IN'],
                    editable: false
                });
            }
            else {
                for (var j = 0; j < node.action.length; j++) {
                    var val = node.action[j];
                    instance.connect({uuids: ['vnode_' + node.id + '_OUT', 'vnode_' + val + '_IN'], editable: false});
                }
            }
        }

        // make all the window divs draggable
        // instance.draggable(jsPlumb.getSelector(".vnode"), {
        //     grid: [20, 20],
        //     drag: function() {
        //         console.log("drag");
        //         //jsPlumb.repaint($(this));
        //     }
        //     });
        //instance.connect()
        // THIS DEMO ONLY USES getSelector FOR CONVENIENCE. Use your library's appropriate selector
        // method, or document.querySelectorAll:
        //jsPlumb.draggable(document.querySelectorAll(".window"), { grid: [20, 20] });
        // connect a few up
        //instance.connect({uuids: ["1_OUT", "2_IN"], editable: false});
        //instance.connect({uuids: ["Window2BottomCenter", "Window3TopCenter"], editable: true});
        //instance.connect({uuids: ["Window2LeftMiddle", "Window4LeftMiddle"], editable: true});
        //instance.connect({uuids: ["Window4TopCenter", "Window4RightMiddle"], editable: true});
        //instance.connect({uuids: ["Window3RightMiddle", "Window2RightMiddle"], editable: true});
        //instance.connect({uuids: ["Window4BottomCenter", "Window1TopCenter"], editable: true});
        //instance.connect({uuids: ["Window3BottomCenter", "Window1BottomCenter"], editable: true});
        //

        //
        // listen for clicks on connections, and offer to delete connections on click.
        //
        instance.bind("click", function (conn, originalEvent) {
            // if (confirm("Delete connection from " + conn.sourceId + " to " + conn.targetId + "?"))
            //   instance.detach(conn);
            //conn.toggleType("basic");
        });

        instance.bind("connectionDrag", function (connection) {
            console.log("connection " + connection.id + " is being dragged. suspendedElement is ", connection.suspendedElement, " of type ", connection.suspendedElementType);
        });

        instance.bind("connectionDragStop", function (connection) {
            console.log("connection " + connection.id + " was dragged");
        });

        instance.bind("connectionMoved", function (params) {
            console.log("connection " + params.connection.id + " was moved");
        });
    });

    jsPlumb.fire("jsPlumbDemoLoaded", instance);

});

